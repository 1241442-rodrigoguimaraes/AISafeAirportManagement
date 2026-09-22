package domain.flightPlan;

import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.aircraft.domain.AircraftCountry;
import eapli.alsafe.aircraft.domain.CabinConfiguration;
import eapli.alsafe.aircraft.domain.CrewElement;
import eapli.alsafe.aircraft.domain.RegistrationID;
import eapli.alsafe.aircraftModelMagnement.domain.*;
import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.airinfrastructure.domain.FlightRouteName;
import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaBoundaries;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaMinimumFuel;
import eapli.alsafe.airports.domain.Airport;
import eapli.alsafe.airports.domain.IATAAirportCode;
import eapli.alsafe.airports.domain.ICAOAirportCode;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.IATACompanyCode;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.alsafe.utils.nodes.domain.Altitude;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.alsafe.utils.nodes.domain.Node;
import eapli.alsafe.utils.nodes.domain.NodeId;
import eapli.alsafe.engineModelMagnement.Domain.*;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.flightPlan.domain.FlightPlanID;
import eapli.alsafe.flightPlan.domain.FlightPlanStatus;
import eapli.alsafe.flightPlan.domain.FuelQuantity;
import eapli.alsafe.flightPlan.domain.FuelUnit;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.alsafe.weather.domain.WeatherDate;
import eapli.alsafe.weather.domain.WindCondition;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanTest {

    private static SystemUser pilotUser() {
        return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with("pilot@tap.pt", "Password1", "John", "Pilot", "pilot@tap.pt")
                .withRoles(Roles.PILOT)
                .build();
    }

    private static AirTransportCompany companyA() {
        return new AirTransportCompany(new IATACompanyCode("TP"), new ICAOCompanyCode("TAP"), "TAP Air");
    }

    private static AirTransportCompany companyB() {
        return new AirTransportCompany(new IATACompanyCode("FR"), new ICAOCompanyCode("RYR"), "Ryanair");
    }

    private static Maker dummyMaker() {
        return new Maker(new MakerName("MakerCo"), new MakerCountry("PT"));
    }

    private static engineModel dummyEngine() {
        return new engineModel(new engineModelName("E1"), dummyMaker(), engineType.TURBOFAN,
                new engineModelPower(1000), engineModelFuel.JET_A1, new engineModelEfficiency(0.9));
    }

    private static aircraftModel dummyModel() {
        final Set<engineModel> engines = new HashSet<>();
        engines.add(dummyEngine());
        return new aircraftModel(new aircraftModelName("A320"), dummyMaker(), aircraftModelType.PASSENGER,
                engineType.TURBOFAN, new maximumRange(1000),
                new aircraftModelPhysicsData(1000, 2000, 1500, 500, 30000, 800, 50, 0.02, 1.2),
                engines);
    }

    private static Aircraft operationalAircraft(AirTransportCompany company) {
        final aircraftModel model = dummyModel();
        final int max = model.maxPassengerSeats();
        return Aircraft.create(model, RegistrationID.valueOf("CS-TST"),
                new AircraftCountry("PT"), new CabinConfiguration(Math.min(10, max), 0, 0, max),
                List.of(new CrewElement("PILOT")), company);
    }

    private static Node dummyNode(long id, double lat, double lon) {
        return new Node(new NodeId((int) id), new Coordinate(lat, lon), new Altitude(20.0));
    }

    private static AirControlArea dummyArea(long id, String name) {
        final List<Coordinate> boundaries = new ArrayList<>();
        boundaries.add(new Coordinate(51.0, -5.0));
        boundaries.add(new Coordinate(51.0, 9.0));
        boundaries.add(new Coordinate(42.0, 9.0));
        boundaries.add(new Coordinate(42.0, -5.0));
        return new AirControlArea(new AirControlAreaID(id), name,
                new AirControlAreaBoundaries(boundaries),
                new AirControlAreaMinimumFuel(250.0));
    }

    private static Airport dummyAirport(String icao, String iata) {
        return new Airport(new ICAOAirportCode(icao), new IATAAirportCode(iata), "Test Airport",
                dummyNode(1, 38.7, -9.1), dummyArea(1, "TestArea"));
    }

    private static FlightRoute dummyRoute(AirTransportCompany company) {
        return new FlightRoute(new FlightRouteName("TP1234"),
                dummyAirport("LPPT", "LIS"), dummyAirport("LPMA", "FNC"), company);
    }

    private static Pilot dummyPilot(AirTransportCompany company, Set<aircraftModel> certifications) {
        return new Pilot("John Pilot", CollaboratorEmail.valueOf("pilot@tap.pt"),
                CollaboratorPhone.valueOf("912345678"), pilotUser(), company, certifications);
    }

    // --- Positive creation ---

    @Test
    void ensureFlightPlanIsCreatedWithDraftStatus() {
        final AirTransportCompany company = companyA();
        final FlightRoute route = dummyRoute(company);
        final Aircraft aircraft = operationalAircraft(company);
        final Pilot pilot = dummyPilot(company, Set.of(dummyModel()));

        final FlightPlan fp = FlightPlan.create(
                new FlightPlanID("TP123"), route, aircraft, pilot,
                LocalDateTime.now().plusDays(1), new FuelQuantity(5000, FuelUnit.L), "Test flight plan");

        assertEquals(FlightPlanStatus.DRAFT, fp.getStatus());
        assertNotNull(fp.identity());
        assertEquals(route, fp.getRoute());
        assertEquals(aircraft, fp.getAircraft());
        assertEquals(pilot, fp.getPilot());
    }

    // --- Negative creation: pilot from different company ---

    @Test
    void ensureFlightPlanWithPilotFromDifferentCompanyIsRejected() {
        final AirTransportCompany companyA = companyA();
        final AirTransportCompany companyB = companyB();
        final FlightRoute route = dummyRoute(companyA);
        final Aircraft aircraft = operationalAircraft(companyA);
        final Pilot pilot = dummyPilot(companyB, Set.of(dummyModel()));

        assertThrows(IllegalArgumentException.class, () ->
                FlightPlan.create(new FlightPlanID("TP123"), route, aircraft, pilot,
                        LocalDateTime.now().plusDays(1), new FuelQuantity(5000, FuelUnit.L), "Test flight plan"));
    }

    // --- Negative creation: decommissioned aircraft ---

    @Test
    void ensureFlightPlanWithDecommissionedAircraftIsRejected() {
        final AirTransportCompany company = companyA();
        final FlightRoute route = dummyRoute(company);

        final aircraftModel model = dummyModel();
        final int max = model.maxPassengerSeats();
        final Aircraft aircraft = Aircraft.create(model, RegistrationID.valueOf("CS-RET"),
                new AircraftCountry("PT"), new CabinConfiguration(Math.min(10, max), 0, 0, max),
                List.of(new CrewElement("PILOT")), company);
        aircraft.decommission();

        final Pilot pilot = dummyPilot(company, Set.of(model));

        assertThrows(IllegalStateException.class, () ->
                FlightPlan.create(new FlightPlanID("TP123"), route, aircraft, pilot,
                        LocalDateTime.now().plusDays(1), new FuelQuantity(5000, FuelUnit.L), "Test flight plan"));
    }

    // --- Negative creation: past departure ---

    @Test
    void ensureFlightPlanWithPastDepartureIsRejected() {
        final AirTransportCompany company = companyA();
        final FlightRoute route = dummyRoute(company);
        final Aircraft aircraft = operationalAircraft(company);
        final Pilot pilot = dummyPilot(company, Set.of(dummyModel()));

        assertThrows(IllegalArgumentException.class, () ->
                FlightPlan.create(new FlightPlanID("TP123"), route, aircraft, pilot,
                        LocalDateTime.now().minusDays(1), new FuelQuantity(5000, FuelUnit.L), "Test flight plan"));
    }

    // --- Negative creation: null params ---

    @Test
    void ensureNullFlightPlanIDIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                FlightPlan.create(null, dummyRoute(companyA()), operationalAircraft(companyA()),
                        dummyPilot(companyA(), Set.of(dummyModel())),
                        LocalDateTime.now().plusDays(1), new FuelQuantity(5000, FuelUnit.L), "Test flight plan"));
    }

    // --- State transitions: submit ---

    @Test
    void ensureDraftPlanCanBeSubmitted() {
        final FlightPlan fp = createValidFlightPlan();
        fp.submit();
        assertEquals(FlightPlanStatus.SUBMITTED, fp.getStatus());
    }

    @Test
    void ensureSubmittedPlanCannotBeSubmittedAgain() {
        final FlightPlan fp = createValidFlightPlan();
        fp.submit();
        assertThrows(IllegalStateException.class, fp::submit);
    }

    @Test
    void ensureDraftPlanCannotBeApproved() {
        final FlightPlan fp = createValidFlightPlan();
        assertThrows(IllegalStateException.class, fp::approve);
    }

    @Test
    void ensureDraftPlanCannotBeRejected() {
        final FlightPlan fp = createValidFlightPlan();
        assertThrows(IllegalStateException.class, fp::reject);
    }

    // --- State transitions: approve ---

    @Test
    void ensureSubmittedPlanCanBeApproved() {
        final FlightPlan fp = createValidFlightPlan();
        fp.submit();
        fp.approve();
        assertEquals(FlightPlanStatus.VALIDATED, fp.getStatus());
    }

    @Test
    void ensureValidatedPlanCannotBeApprovedAgain() {
        final FlightPlan fp = createValidFlightPlan();
        fp.submit();
        fp.approve();
        assertThrows(IllegalStateException.class, fp::approve);
    }

    // --- State transitions: reject ---

    @Test
    void ensureSubmittedPlanCanBeRejected() {
        final FlightPlan fp = createValidFlightPlan();
        fp.submit();
        fp.reject();
        assertEquals(FlightPlanStatus.REJECTED, fp.getStatus());
    }

    @Test
    void ensureValidatedPlanCannotBeRejected() {
        final FlightPlan fp = createValidFlightPlan();
        fp.submit();
        fp.approve();
        assertThrows(IllegalStateException.class, fp::reject);
    }

    // --- State transitions: cancel ---

    @Test
    void ensureDraftPlanCanBeCancelled() {
        final FlightPlan fp = createValidFlightPlan();
        fp.cancel();
        assertEquals(FlightPlanStatus.CANCELLED, fp.getStatus());
    }

    @Test
    void ensureSubmittedPlanCanBeCancelled() {
        final FlightPlan fp = createValidFlightPlan();
        fp.submit();
        fp.cancel();
        assertEquals(FlightPlanStatus.CANCELLED, fp.getStatus());
    }

    @Test
    void ensureValidatedPlanCannotBeCancelled() {
        final FlightPlan fp = createValidFlightPlan();
        fp.submit();
        fp.approve();
        assertThrows(IllegalStateException.class, fp::cancel);
    }

    @Test
    void ensureRejectedPlanCannotBeCancelled() {
        final FlightPlan fp = createValidFlightPlan();
        fp.submit();
        fp.reject();
        assertThrows(IllegalStateException.class, fp::cancel);
    }

    // --- State transitions: invalidate ---

    @Test
    void ensureValidatedPlanCanBeInvalidated() {
        final FlightPlan fp = createValidFlightPlan();
        fp.submit();
        fp.approve();
        fp.invalidate();
        assertEquals(FlightPlanStatus.DRAFT, fp.getStatus());
    }

    @Test
    void ensureDraftPlanCannotBeInvalidated() {
        final FlightPlan fp = createValidFlightPlan();
        assertThrows(IllegalStateException.class, fp::invalidate);
    }

    @Test
    void ensureRejectedPlanCannotBeInvalidated() {
        final FlightPlan fp = createValidFlightPlan();
        fp.submit();
        fp.reject();
        assertThrows(IllegalStateException.class, fp::invalidate);
    }

    // --- identity and sameAs ---

    @Test
    void ensureIdentityReturnsFlightPlanID() {
        final FlightPlan fp = createValidFlightPlan();
        assertEquals("TP123", fp.identity().toString());
    }

    @Test
    void ensureToStringContainsFlightPlanId() {
        final FlightPlan fp = createValidFlightPlan();
        assertTrue(fp.toString().contains("TP123"));
    }

    @Test
    void ensurePilotAccessorReturnsAssignedPilot() {
        final AirTransportCompany company = companyA();
        final Pilot pilot = dummyPilot(company, Set.of(dummyModel()));
        final FlightPlan fp = FlightPlan.create(
                new FlightPlanID("TP123"), dummyRoute(company), operationalAircraft(company),
                pilot, LocalDateTime.now().plusDays(1), new FuelQuantity(5000, FuelUnit.L), "Test flight plan");
        assertEquals(pilot, fp.pilot());
    }

    @Test
    void ensureSameAsRecognizesSameFlightPlan() {
        final FlightPlan fp = createValidFlightPlan();
        assertTrue(fp.sameAs(fp));
    }

    @Test
    void protectedConstructorInitializesUntestedFlightPlanForJpa() {
        assertFalse(new JpaFlightPlan().isTested());
    }

    // --- US082: insert weather data ---

    @Test
    void ensureWeatherDataCanBeInsertedIntoUntestedFlightPlan() {
        final FlightPlan fp = createValidFlightPlan();
        final WeatherData weatherData = dummyWeatherData();

        fp.insertWeatherData(weatherData);

        assertEquals(weatherData, fp.getWeatherData());
        assertFalse(fp.isTested());
    }

    @Test
    void ensureInsertingWeatherDataVoidsPreviousTestResult() {
        final FlightPlan fp = createValidFlightPlan();
        fp.markAsTested();
        assertTrue(fp.isTested());

        fp.insertWeatherData(dummyWeatherData());

        assertFalse(fp.isTested());
        assertNotNull(fp.getWeatherData());
    }

    @Test
    void ensureNullWeatherDataIsRejected() {
        final FlightPlan fp = createValidFlightPlan();
        assertThrows(IllegalArgumentException.class, () -> fp.insertWeatherData(null));
    }

    @Test
    void ensureMarkAsTestedSetsTestedFlag() {
        final FlightPlan fp = createValidFlightPlan();
        assertFalse(fp.isTested());
        fp.markAsTested();
        assertTrue(fp.isTested());
    }

    // --- helper ---

    private FlightPlan createValidFlightPlan() {
        final AirTransportCompany company = companyA();
        return FlightPlan.create(
                new FlightPlanID("TP123"),
                dummyRoute(company),
                operationalAircraft(company),
                dummyPilot(company, Set.of(dummyModel())),
                LocalDateTime.now().plusDays(1),
                new FuelQuantity(5000, FuelUnit.L),
                "Test flight plan");
    }

    private static WeatherData dummyWeatherData() {
        final Calendar date = Calendar.getInstance();
        date.add(Calendar.DAY_OF_MONTH, -1);
        return new WeatherData(dummyArea(2, "WeatherArea"),
                new WeatherDate(date), new WindCondition(180, 12.5));
    }

    private static final class JpaFlightPlan extends FlightPlan {
        private JpaFlightPlan() {
            super();
        }
    }
}
