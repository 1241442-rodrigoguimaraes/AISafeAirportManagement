package eapli.alsafe.flightPlan.application;

import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.aircraft.repositories.AircraftRepository;
import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.airinfrastructure.repositories.FlightRouteRepository;
import eapli.alsafe.airports.domain.Airport;
import eapli.alsafe.antlr.FlightDSLProcessor;
import eapli.alsafe.antlr.domain.FlightPlanDSL;
import eapli.alsafe.antlr.domain.FuelInfo;
import eapli.alsafe.antlr.domain.Leg;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.flightPlan.domain.*;
import eapli.alsafe.flightPlan.infrastructure.FlightPlanTestRunner;
import eapli.alsafe.flightPlan.infrastructure.SCOMPFlightPlanTestRunner;
import eapli.alsafe.flightPlan.repositories.FlightPlanRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.pilotmanagement.repositories.PilotRepository;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static eapli.alsafe.flightPlan.domain.FuelQuantity.*;

public class FlightPlanService {

    private static final double METERS_PER_LITER = 125.0;// 125 m/l is the median fuel consumption of an aircraft(8 l/km)
    private static final double JET_A1_KG_PER_LITER = 0.804;
    private static final double FUEL_TOLERANCE = 0.001;

    private final FlightPlanRepository flightPlanRepository;
    private final FlightRouteRepository flightRouteRepository;
    private final AircraftRepository aircraftRepository;
    private final PilotRepository pilotRepository;
    private final FlightPlanTestRunner testRunner;

    public FlightPlanService() {
        this.flightPlanRepository = PersistenceContext.repositories().flightPlan();
        this.flightRouteRepository = PersistenceContext.repositories().flightRoutes();
        this.aircraftRepository = PersistenceContext.repositories().aircraft();
        this.pilotRepository = PersistenceContext.repositories().pilots();
        this.testRunner = new SCOMPFlightPlanTestRunner();
    }

    public FlightPlanService(FlightPlanRepository flightPlanRepository, FlightRouteRepository flightRouteRepository, AircraftRepository aircraftRepository, PilotRepository pilotRepository, FlightPlanTestRunner testRunner) {
        this.flightPlanRepository = flightPlanRepository;
        this.flightRouteRepository = flightRouteRepository;
        this.aircraftRepository = aircraftRepository;
        this.pilotRepository = pilotRepository;
        this.testRunner = testRunner;
    }

    public FlightPlan createFlightPlan(FlightPlanID id, FlightRoute route,
                                       Aircraft aircraft, Pilot pilot,
                                       LocalDateTime departureDateTime,
                                       FuelQuantity fuelQuantity, String content) {
        FlightPlan flightPlan = FlightPlan.create(id, route, aircraft, pilot, departureDateTime, fuelQuantity,  content);
        flightPlanRepository.save(flightPlan);
        return flightPlan;
    }

    public Iterable<FlightRoute> getRoutesFromACompany(AirTransportCompany company) {
        return flightRouteRepository.findByCompany(company);
    }

    public Pilot getPilotCompanyFromAuthenticatedUser(SystemUser user) {
        return pilotRepository.findBySystemUser(user).orElseThrow(() -> new IllegalStateException("Authenticated user is not a pilot"));
    }

    public Iterable<Aircraft> getAircraftFromACompany(AirTransportCompany company) {
        return aircraftRepository.findByCompany(company);
    }

    public void submitPlan (FlightPlan plan) {
        plan.submit();
        flightPlanRepository.save(plan);
    }

    public void cancelPlan (FlightPlan plan) {
        plan.cancel();
        flightPlanRepository.save(plan);
    }

    public FlightPlanStatus validate(FlightPlan plan) throws IOException {
        if (!hasSufficientFuel(plan)) {
            plan.reject();
            flightPlanRepository.save(plan);
            throw new IllegalArgumentException("The flight plan does not have sufficient fuel for the route.");
        }
        if (!certifyPilotAircraft(plan.getPilot(), plan.getAircraft())) {
            plan.reject();
            flightPlanRepository.save(plan);
            throw new IllegalArgumentException("The pilot is not certified to pilot the aircraft.");
        }

        List<String> dslErrors = new java.util.ArrayList<>();
        if (!validateDSL(plan, dslErrors)) {
            plan.reject();
            flightPlanRepository.save(plan);
            String detailedMessage = "The flight plan content is invalid: " + String.join(" | ", dslErrors);
            throw new IllegalArgumentException(detailedMessage);
        }

        FlightPlanTestRunner.TestResult test = testRunner.run(plan.getContent());
        if (!test.success()) {
            plan.reject();
            flightPlanRepository.save(plan);
            throw new IllegalArgumentException("The flight plan failed the C test component: " + test.message());
        }

        plan.markAsTested();
        plan.approve();
        return flightPlanRepository.save(plan).getStatus();
    }

    private boolean hasSufficientFuel(FlightPlan plan) {
        double distance = calculateDistance(plan.getRoute().getStartingAirport(), plan.getRoute().getDestinationAirport());
        double fuelNeeded = distance/METERS_PER_LITER;
        boolean result;
        switch (plan.getFuelQuantity().getFuelUnit()){
            case L -> result = plan.getFuelQuantity().getFuelQuantity() >= fuelNeeded;
            case KG, LBS -> result = toLiters(plan.getFuelQuantity()).getFuelQuantity() >= fuelNeeded;
            default -> result = false;
        }
        return result;
    }

    private double calculateDistance(Airport origin, Airport destination) {

        double lat1 = origin.getLocation().getCoordinates().latitude();
        double lon1 = origin.getLocation().getCoordinates().longitude();
        double alt1 = origin.getLocation().getAltitude().meters();
        double lat2 = destination.getLocation().getCoordinates().latitude();
        double lon2 = destination.getLocation().getCoordinates().longitude();
        double alt2 = destination.getLocation().getAltitude().meters();

        final int R = 6371000;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        double height = alt1 - alt2;

        return Math.sqrt(Math.pow(distance, 2) + Math.pow(height, 2));
    }

    private boolean certifyPilotAircraft(Pilot pilot, Aircraft aircraft) {
        return pilot.certifiedAircraftModels().contains(aircraft.model());
    }

    public Iterable<FlightPlan> getAllFlightPlansFromSystemUser(SystemUser user) {
        return flightPlanRepository.findBySystemUser(user);
    }

    public boolean validateDSL(FlightPlan plan, List<String> errors) throws IOException {
        if (plan.getContent() == null || plan.getContent().isBlank()) {
            errors.add("No flight plan DSL content was provided.");
            return false;
        }

        FlightDSLProcessor.Result result = FlightDSLProcessor.process(plan.getContent());
        if (!result.isValid()) {
            errors.addAll(result.allErrors());
            if (errors.isEmpty()) {
                errors.add("Syntax/Semantic errors found in DSL content.");
            }
            return false;
        }

        if (result.getFlightPlans().isEmpty()) {
            errors.add("No flight plan data found in the DSL content.");
            return false;
        }
        if (result.getFlightPlans().size() > 1) {
            errors.add("The DSL content must describe exactly one flight plan for this validation.");
            return false;
        }
        FlightPlanDSL flightPlanDSL = result.getFlightPlans().get(0);

        if (!flightPlanDSL.getFlightId().equalsIgnoreCase(plan.identity().getFlightId())) {
            errors.add(String.format("Flight ID does not match (DSL: %s vs Plan: %s).", flightPlanDSL.getFlightId(), plan.identity().getFlightId()));
        }
        if (!flightPlanDSL.getRouteId().equalsIgnoreCase(plan.getRoute().identity().getName())) {
            errors.add(String.format("Route ID does not match (DSL: %s vs Plan: %s).", flightPlanDSL.getRouteId(), plan.getRoute().identity().getName()));
        }
        if (!flightPlanDSL.getAircraft().equalsIgnoreCase(plan.getAircraft().identity().toString())) {
            errors.add(String.format("Aircraft does not match (DSL: %s vs Plan: %s).", flightPlanDSL.getAircraft(), plan.getAircraft().identity().toString()));
        }

        LocalDateTime dslDateTime = LocalDateTime.of(
                LocalDate.parse(flightPlanDSL.getDate()),
                LocalTime.parse(flightPlanDSL.getTime()));
        if (!dslDateTime.equals(plan.getDepartureDateTime())) {
            errors.add(String.format("Departure date/time does not match (DSL: %s vs Plan: %s).", dslDateTime, plan.getDepartureDateTime()));
        }

        if (!flightPlanDSL.getLegs().isEmpty()) {
            Leg firstLeg = flightPlanDSL.getLegs().get(0);
            Leg lastLeg = flightPlanDSL.getLegs().get(flightPlanDSL.getLegs().size() - 1);

            if (!firstLeg.getDepartureAirport().equalsIgnoreCase(plan.getRoute().getStartingAirport().getIataCode().getCode())) {
                errors.add(String.format("Departure airport does not match (DSL: %s vs Plan: %s).", firstLeg.getDepartureAirport(), plan.getRoute().getStartingAirport().getIataCode().getCode()));
            }
            if (!lastLeg.getArrivalAirport().equalsIgnoreCase(plan.getRoute().getDestinationAirport().getIataCode().getCode())) {
                errors.add(String.format("Arrival airport does not match (DSL: %s vs Plan: %s).", lastLeg.getArrivalAirport(), plan.getRoute().getDestinationAirport().getIataCode().getCode()));
            }
            if (!fuelMatchesPlan(flightPlanDSL.getLegs(), plan.getFuelQuantity())) {
                errors.add("Fuel quantity does not match the DSL content.");
            }
        } else {
            errors.add("No legs found in the flight plan DSL.");
        }
        return errors.isEmpty();
    }

    private boolean fuelMatchesPlan(List<Leg> legs, FuelQuantity planFuel) {
        double dslFuelKg = 0;
        for (Leg leg : legs) {
            FuelInfo fuel = leg.getFuel();
            if (fuel.getUnit() == FuelInfo.FuelUnit.KG) {
                dslFuelKg += fuel.getQuantity();
            } else if (fuel.getUnit() == FuelInfo.FuelUnit.L) {
                dslFuelKg += fuel.getQuantity() * JET_A1_KG_PER_LITER;
            } else {
                return false;
            }
        }
        return Math.abs(dslFuelKg - planFuelInKilograms(planFuel)) <= FUEL_TOLERANCE;
    }

    private double planFuelInKilograms(FuelQuantity fuelQuantity) {
        return switch (fuelQuantity.getFuelUnit()) {
            case KG -> fuelQuantity.getFuelQuantity();
            case L -> fuelQuantity.getFuelQuantity() * JET_A1_KG_PER_LITER;
            case LBS -> toKilograms(fuelQuantity).getFuelQuantity();
        };
    }
}
