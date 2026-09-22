package eapli.alsafe.flightPlan.application;

import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.flightPlan.domain.*;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.io.IOException;
import java.time.LocalDateTime;

@UseCaseController
public class FlightPlanController {

    private final AuthorizationService authz;
    private final FlightPlanService flightPlanService;

    public FlightPlanController() {
        this(AuthzRegistry.authorizationService(),
                new FlightPlanService());
    }

    public FlightPlanController(final AuthorizationService authz,
                                final FlightPlanService flightPlanService) {
        if (authz == null || flightPlanService == null ) {
            throw new IllegalArgumentException("All dependencies are required");
        }
        this.authz = authz;
        this.flightPlanService = flightPlanService;
    }

    public Iterable<FlightRoute> getRoutesForPilot(AirTransportCompany company) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.PILOT);
        return flightPlanService.getRoutesFromACompany(company);
    }

    public Iterable<Aircraft> getAircraftForCompany(AirTransportCompany company) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.PILOT);
        return flightPlanService.getAircraftFromACompany(company);
    }

    public Pilot getAuthenticatedPilot() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.PILOT);
        final var session = authz.session().orElseThrow(
                () -> new IllegalStateException("No authenticated session"));
        return flightPlanService.getPilotCompanyFromAuthenticatedUser(session.authenticatedUser());
    }

    public FlightPlan createFlightPlan(final String designator,
                                       final FlightRoute route,
                                       final Aircraft aircraft,
                                       final Pilot pilot,
                                       final LocalDateTime departureDateTime,
                                       final double fuelAmount,
                                       final FuelUnit fuelUnit,
                                       final String content) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.PILOT);

        final FlightPlanID flightPlanID = new FlightPlanID(designator);
        final FuelQuantity fuelQuantity = new FuelQuantity(fuelAmount, fuelUnit);

        return flightPlanService.createFlightPlan(flightPlanID, route, aircraft, pilot,
                departureDateTime, fuelQuantity, content);
    }

    public Iterable<FlightPlan> getFlightPlans() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.PILOT);
        final var session = authz.session().orElseThrow(
                () -> new IllegalStateException("No authenticated session"));
        return flightPlanService.getAllFlightPlansFromSystemUser(session.authenticatedUser());
    }

    public void cancelPlan(FlightPlan plan) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.PILOT);
        flightPlanService.cancelPlan(plan);
    }

    public void submitPlan(FlightPlan plan) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.PILOT);
        flightPlanService.submitPlan(plan);
    }

    public FlightPlanStatus validatePlan(FlightPlan plan) throws IOException {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.PILOT);
        return flightPlanService.validate(plan);
    }
}
