package controller.flightplan;

import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.aircraft.domain.AircraftCountry;
import eapli.alsafe.aircraft.domain.CabinConfiguration;
import eapli.alsafe.aircraft.domain.CrewElement;
import eapli.alsafe.aircraft.domain.RegistrationID;
import eapli.alsafe.aircraft.repositories.AircraftRepository;
import eapli.alsafe.aircraftModelMagnement.domain.*;
import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.airinfrastructure.domain.FlightRouteName;
import eapli.alsafe.airinfrastructure.repositories.FlightRouteRepository;
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
import eapli.alsafe.flightPlan.infrastructure.FlightPlanTestRunner;
import eapli.alsafe.utils.nodes.domain.Altitude;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.alsafe.utils.nodes.domain.Node;
import eapli.alsafe.utils.nodes.domain.NodeId;
import eapli.alsafe.engineModelMagnement.Domain.*;
import eapli.alsafe.flightPlan.application.FlightPlanService;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.flightPlan.domain.FlightPlanID;
import eapli.alsafe.flightPlan.domain.FlightPlanStatus;
import eapli.alsafe.flightPlan.domain.FuelQuantity;
import eapli.alsafe.flightPlan.domain.FuelUnit;
import eapli.alsafe.flightPlan.repositories.FlightPlanRepository;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.pilotmanagement.repositories.PilotRepository;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FlightPlanServiceTest {

    private static Node node1() {
        return new Node(new NodeId(1), new Coordinate(48.72, 2.36), new Altitude(20.0));
    }

    private static Node node2() {
        return new Node(new NodeId(2), new Coordinate(47.37, 8.53), new Altitude(20.0));
    }

    private static AirControlArea area1() {
        final List<Coordinate> b = new ArrayList<>();
        b.add(new Coordinate(51.0, -5.0));
        b.add(new Coordinate(51.0, 9.0));
        b.add(new Coordinate(42.0, 9.0));
        b.add(new Coordinate(42.0, -5.0));
        return new AirControlArea(new AirControlAreaID(1L), "Area1",
                new AirControlAreaBoundaries(b), new AirControlAreaMinimumFuel(250.0));
    }

    private static AirControlArea area2() {
        final List<Coordinate> b = new ArrayList<>();
        b.add(new Coordinate(48.0, 8.0));
        b.add(new Coordinate(48.0, 11.0));
        b.add(new Coordinate(46.0, 11.0));
        b.add(new Coordinate(46.0, 8.0));
        return new AirControlArea(new AirControlAreaID(2L), "Area2",
                new AirControlAreaBoundaries(b), new AirControlAreaMinimumFuel(300.0));
    }

    private FlightPlanRepository flightPlanRepository;
    private FlightRouteRepository flightRouteRepository;
    private AircraftRepository aircraftRepository;
    private PilotRepository pilotRepository;
    private FlightPlanTestRunner testRunner;
    private FlightPlanService service;

    private AirTransportCompany company;
    private FlightRoute route;
    private Aircraft aircraft;
    private Pilot pilot;

    private static SystemUser pilotUser() {
        return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with("pilot@tap.pt", "Password1", "John", "Pilot", "pilot@tap.pt")
                .withRoles(Roles.PILOT)
                .build();
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

    private static AirTransportCompany dummyCompany() {
        return new AirTransportCompany(new IATACompanyCode("TP"), new ICAOCompanyCode("TAP"), "TAP Air");
    }

    @BeforeEach
    void setUp() throws IOException {
        flightPlanRepository = mock(FlightPlanRepository.class);
        flightRouteRepository = mock(FlightRouteRepository.class);
        aircraftRepository = mock(AircraftRepository.class);
        pilotRepository = mock(PilotRepository.class);
        testRunner = mock(FlightPlanTestRunner.class);

        // por omissão o componente de teste C aprova o plano; os testes que
        // exercitam a reprovação ou falha de ambiente redefinem este stub
        when(testRunner.run(any())).thenReturn(
                new FlightPlanTestRunner.TestResult(true, 0, "All safety checks passed."));

        service = new FlightPlanService(flightPlanRepository, flightRouteRepository,
                aircraftRepository, pilotRepository, testRunner);

        company = dummyCompany();
        final aircraftModel model = dummyModel();
        final int max = model.maxPassengerSeats();
        aircraft = Aircraft.create(model, RegistrationID.valueOf("CS-TUA"),
                new AircraftCountry("PT"), new CabinConfiguration(Math.min(10, max), 0, 0, max),
                List.of(new CrewElement("PILOT")), company);
        route = new FlightRoute(new FlightRouteName("TP123"),
                new Airport(new ICAOAirportCode("LPPT"), new IATAAirportCode("OPO"), "Override",
                        node1(), area1()),
                new Airport(new ICAOAirportCode("LPMA"), new IATAAirportCode("MAD"), "Madrid",
                        node2(), area2()),
                company);
        pilot = new Pilot("John Pilot", CollaboratorEmail.valueOf("pilot@tap.pt"),
                CollaboratorPhone.valueOf("912345678"), pilotUser(), company, Set.of(model));
    }

    @Test
    void createFlightPlan_savesAndReturnsPlan() {
        final FlightPlanID id = new FlightPlanID("TP123");
        final FuelQuantity fuel = new FuelQuantity(5000, FuelUnit.L);

        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final FlightPlan result = service.createFlightPlan(id, route, aircraft, pilot,
                LocalDateTime.now().plusDays(1), fuel, null);

        assertNotNull(result);
        assertEquals(FlightPlanStatus.DRAFT, result.getStatus());
        verify(flightPlanRepository).save(any(FlightPlan.class));
    }

    @Test
    void getRoutesFromACompany_returnsRoutes() {
        when(flightRouteRepository.findByCompany(company)).thenReturn(List.of(route));

        final Iterable<FlightRoute> routes = service.getRoutesFromACompany(company);

        assertTrue(routes.iterator().hasNext());
        verify(flightRouteRepository).findByCompany(company);
    }

    @Test
    void getAircraftFromACompany_returnsAircraft() {
        when(aircraftRepository.findByCompany(company)).thenReturn(List.of(aircraft));

        final Iterable<Aircraft> result = service.getAircraftFromACompany(company);

        assertTrue(result.iterator().hasNext());
        verify(aircraftRepository).findByCompany(company);
    }

    @Test
    void getPilotCompanyFromAuthenticatedUser_returnsPilot() {
        final SystemUser user = pilotUser();
        when(pilotRepository.findBySystemUser(user)).thenReturn(Optional.of(pilot));

        final Pilot result = service.getPilotCompanyFromAuthenticatedUser(user);

        assertNotNull(result);
        assertEquals(pilot, result);
        verify(pilotRepository).findBySystemUser(user);
    }

    @Test
    void getPilotCompanyFromAuthenticatedUser_nonPilotThrows() {
        final SystemUser user = pilotUser();
        when(pilotRepository.findBySystemUser(user)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> service.getPilotCompanyFromAuthenticatedUser(user));
    }

    @Test
    void submitPlan_submitsAndSaves() {
        final FlightPlan fp = createFlightPlan();
        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.submitPlan(fp);

        assertEquals(FlightPlanStatus.SUBMITTED, fp.getStatus());
        verify(flightPlanRepository).save(fp);
    }

    @Test
    void cancelPlan_cancelsAndSaves() {
        final FlightPlan fp = createFlightPlan();
        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.cancelPlan(fp);

        assertEquals(FlightPlanStatus.CANCELLED, fp.getStatus());
        verify(flightPlanRepository).save(fp);
    }

    @Test
    void validatePlan_withSufficientFuelAndCertifiedPilot_approves() throws IOException {
        final FlightPlan fp = createFlightPlan();
        fp.submit();
        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final FlightPlanStatus status = service.validate(fp);

        assertEquals(FlightPlanStatus.VALIDATED, status);
        assertTrue(fp.isTested(), "a plan that passes the C test must be marked as tested (US085)");
        verify(flightPlanRepository).save(fp);
    }

    @Test
    void validatePlan_whenCTestReproves_doesNotMarkAsTested() throws IOException {
        when(testRunner.run(any())).thenReturn(
                new FlightPlanTestRunner.TestResult(false, 1, "Safety violation detected"));
        final FlightPlan fp = createFlightPlan();
        fp.submit();
        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(IllegalArgumentException.class, () -> service.validate(fp));

        assertFalse(fp.isTested(), "a rejected plan must not be marked as tested");
    }

    @Test
    void validatePlan_withSyntaxError_rejectsWithSyntaxDetails() {
        final String invalidDsl = validDslFor(LocalDateTime.now().plusDays(1).withSecond(0).withNano(0))
                .replace("type: regular", "");
        final FlightPlan fp = createFlightPlanWithContent(invalidDsl, new FuelQuantity(50000, FuelUnit.KG));
        fp.submit();
        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validate(fp));

        assertEquals(FlightPlanStatus.REJECTED, fp.getStatus());
        assertTrue(ex.getMessage().contains("[SYNTAX ERROR]"));
        verify(flightPlanRepository).save(fp);
    }

    @Test
    void validatePlan_withSemanticError_rejectsWithSemanticDetails() {
        final String invalidDsl = validDslFor(LocalDateTime.now().plusDays(1).withSecond(0).withNano(0))
                .replace("fuel { quantity: 50000 unit: kg }", "fuel { quantity: 0 unit: kg }");
        final FlightPlan fp = createFlightPlanWithContent(invalidDsl, new FuelQuantity(50000, FuelUnit.KG));
        fp.submit();
        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validate(fp));

        assertEquals(FlightPlanStatus.REJECTED, fp.getStatus());
        assertTrue(ex.getMessage().contains("strictly positive"));
        verify(flightPlanRepository).save(fp);
    }

    @Test
    void validatePlan_withFlightIdMismatch_rejects() {
        final String dsl = validDslFor(LocalDateTime.now().plusDays(1).withSecond(0).withNano(0))
                .replace("flight TP123", "flight TP124");
        final FlightPlan fp = createFlightPlanWithContent(dsl, new FuelQuantity(50000, FuelUnit.KG));
        fp.submit();
        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validate(fp));

        assertTrue(ex.getMessage().contains("Flight ID does not match"));
        assertEquals(FlightPlanStatus.REJECTED, fp.getStatus());
    }

    @Test
    void validatePlan_withRouteMismatch_rejects() {
        final String dsl = validDslFor(LocalDateTime.now().plusDays(1).withSecond(0).withNano(0))
                .replace("route: TP123", "route: TP124");
        final FlightPlan fp = createFlightPlanWithContent(dsl, new FuelQuantity(50000, FuelUnit.KG));
        fp.submit();
        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validate(fp));

        assertTrue(ex.getMessage().contains("Route ID does not match"));
        assertEquals(FlightPlanStatus.REJECTED, fp.getStatus());
    }

    @Test
    void validatePlan_withAircraftMismatch_rejects() {
        final String dsl = validDslFor(LocalDateTime.now().plusDays(1).withSecond(0).withNano(0))
                .replace("aircraft: CS-TUA", "aircraft: CS-TUB");
        final FlightPlan fp = createFlightPlanWithContent(dsl, new FuelQuantity(50000, FuelUnit.KG));
        fp.submit();
        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validate(fp));

        assertTrue(ex.getMessage().contains("Aircraft does not match"));
        assertEquals(FlightPlanStatus.REJECTED, fp.getStatus());
    }

    @Test
    void validatePlan_withDateTimeMismatch_rejects() {
        final LocalDateTime departure = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
        final String dsl = validDslFor(departure.plusDays(1));
        final FlightPlan fp = createFlightPlanWithContent(dsl, new FuelQuantity(50000, FuelUnit.KG), departure);
        fp.submit();
        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validate(fp));

        assertTrue(ex.getMessage().contains("Departure date/time does not match"));
        assertEquals(FlightPlanStatus.REJECTED, fp.getStatus());
    }

    @Test
    void validatePlan_withDepartureAirportMismatch_rejects() {
        final String dsl = validDslFor(LocalDateTime.now().plusDays(1).withSecond(0).withNano(0))
                .replace("departure: OPO", "departure: LIS");
        final FlightPlan fp = createFlightPlanWithContent(dsl, new FuelQuantity(50000, FuelUnit.KG));
        fp.submit();
        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validate(fp));

        assertTrue(ex.getMessage().contains("Departure airport does not match"));
        assertEquals(FlightPlanStatus.REJECTED, fp.getStatus());
    }

    @Test
    void validatePlan_withFuelMismatch_rejects() {
        final FlightPlan fp = createFlightPlanWithContent(
                validDslFor(LocalDateTime.now().plusDays(1).withSecond(0).withNano(0)),
                new FuelQuantity(60000, FuelUnit.KG));
        fp.submit();
        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validate(fp));

        assertTrue(ex.getMessage().contains("Fuel quantity does not match"));
        assertEquals(FlightPlanStatus.REJECTED, fp.getStatus());
    }

    @Test
    void validatePlan_withDslLitersAndPlanKilograms_usesJetA1Density() throws IOException {
        final String dsl = validDslFor(LocalDateTime.now().plusDays(1).withSecond(0).withNano(0))
                .replace("fuel { quantity: 50000 unit: kg }", "fuel { quantity: 10000 unit: l }");
        final FlightPlan fp = createFlightPlanWithContent(dsl, new FuelQuantity(8040, FuelUnit.KG));
        fp.submit();
        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final FlightPlanStatus status = service.validate(fp);

        assertEquals(FlightPlanStatus.VALIDATED, status);
    }

    @Test
    void validatePlan_withMultipleLegs_sumsDslFuel() throws IOException {
        final LocalDateTime departure = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
        final FlightPlan fp = createFlightPlanWithContent(validTwoLegDslFor(departure),
                new FuelQuantity(50000, FuelUnit.KG), departure);
        fp.submit();
        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final FlightPlanStatus status = service.validate(fp);

        assertEquals(FlightPlanStatus.VALIDATED, status);
    }

    @Test
    void validatePlan_whenCTestReprovesPlan_rejectsWithCTestMessage() throws IOException {
        when(testRunner.run(any())).thenReturn(
                new FlightPlanTestRunner.TestResult(false, 1, "Safety violation detected in segment 1"));
        final FlightPlan fp = createFlightPlan();
        fp.submit();
        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validate(fp));

        assertEquals(FlightPlanStatus.REJECTED, fp.getStatus());
        assertTrue(ex.getMessage().contains("failed the C test component"));
        assertTrue(ex.getMessage().contains("Safety violation detected"));
        verify(flightPlanRepository).save(fp);
    }

    @Test
    void validatePlan_whenCTestPasses_runnerReceivesThePlanContent() throws IOException {
        final FlightPlan fp = createFlightPlan();
        fp.submit();
        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.validate(fp);

        verify(testRunner).run(fp.getContent());
    }

    @Test
    void validatePlan_withInvalidDsl_doesNotInvokeCTest() throws IOException {
        final String invalidDsl = validDslFor(LocalDateTime.now().plusDays(1).withSecond(0).withNano(0))
                .replace("type: regular", "");
        final FlightPlan fp = createFlightPlanWithContent(invalidDsl, new FuelQuantity(50000, FuelUnit.KG));
        fp.submit();
        when(flightPlanRepository.save(any(FlightPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(IllegalArgumentException.class, () -> service.validate(fp));

        verify(testRunner, never()).run(any());
    }

    @Test
    void validatePlan_whenCTestEnvironmentFails_propagatesAndKeepsPlanSubmitted() throws IOException {
        when(testRunner.run(any())).thenThrow(new IOException("wsl: command not found"));
        final FlightPlan fp = createFlightPlan();
        fp.submit();

        assertThrows(IOException.class, () -> service.validate(fp));

        // falha de ambiente não pode rejeitar o plano: fica SUBMITTED e nada é persistido
        assertEquals(FlightPlanStatus.SUBMITTED, fp.getStatus());
        verify(flightPlanRepository, never()).save(any());
    }

    @Test
    void getAllFlightPlansFromSystemUser_returnsPlans() {
        final SystemUser user = pilotUser();
        final FlightPlan fp = createFlightPlan();
        when(flightPlanRepository.findBySystemUser(user)).thenReturn(List.of(fp));

        final Iterable<FlightPlan> result = service.getAllFlightPlansFromSystemUser(user);

        assertTrue(result.iterator().hasNext());
        verify(flightPlanRepository).findBySystemUser(user);
    }

    private FlightPlan createFlightPlan() {
        final LocalDateTime departure = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
        return createFlightPlanWithContent(validDslFor(departure), new FuelQuantity(50000, FuelUnit.KG), departure);
    }

    private FlightPlan createFlightPlanWithContent(String content, FuelQuantity fuelQuantity) {
        return createFlightPlanWithContent(content, fuelQuantity,
                LocalDateTime.now().plusDays(1).withSecond(0).withNano(0));
    }

    private FlightPlan createFlightPlanWithContent(String content, FuelQuantity fuelQuantity,
                                                   LocalDateTime departure) {
        return FlightPlan.create(
                new FlightPlanID("TP123"), route, aircraft, pilot,
                departure, fuelQuantity, content);
    }

    private String validDslFor(LocalDateTime departure) {
        return String.format("""
                flight TP123 {
                    type: regular
                    route: TP123
                    date: %1$tY-%1$tm-%1$td
                    time: %1$tH:%1$tM
                    aircraft: CS-TUA
                    leg {
                        departure: OPO
                        arrival: MAD
                        fuel { quantity: 50000 unit: kg }
                        profile {
                            climb {
                                altitude { quantity: 0 unit: m }
                                speed { quantity: 210 unit: knots }
                                altitude { quantity: 12000 unit: m }
                                speed { quantity: 300 unit: knots }
                            }
                            cruise {
                                speed { quantity: 460 unit: knots }
                            }
                            descend {
                                altitude { quantity: 12000 unit: m }
                                speed { quantity: 300 unit: knots }
                                rate_descent { quantity: -10 unit: m/s }
                                altitude { quantity: 0 unit: m }
                                speed { quantity: 140 unit: knots }
                                rate_descent { quantity: -5 unit: m/s }
                            }
                        }
                        segment {
                            mode: climb
                            start: { latitude: 41.262891 longitude: -8.68522 altitude: 69 m }
                            end: { latitude: 42.0 longitude: -8.01 altitude: 9249 m }
                            altitude_slots: [3000, 6000, 9000]
                            width: 50 m
                            wind: 270 deg 15 m/s
                        }
                    }
                }
                """, departure);
    }

    private String validTwoLegDslFor(LocalDateTime departure) {
        return String.format("""
                flight TP123 {
                    type: regular
                    route: TP123
                    date: %1$tY-%1$tm-%1$td
                    time: %1$tH:%1$tM
                    aircraft: CS-TUA
                    leg {
                        departure: OPO
                        arrival: LIS
                        fuel { quantity: 25000 unit: kg }
                        profile {
                            climb {
                                altitude { quantity: 0 unit: m }
                                speed { quantity: 210 unit: knots }
                                altitude { quantity: 12000 unit: m }
                                speed { quantity: 300 unit: knots }
                            }
                            cruise {
                                speed { quantity: 460 unit: knots }
                            }
                            descend {
                                altitude { quantity: 12000 unit: m }
                                speed { quantity: 300 unit: knots }
                                rate_descent { quantity: -10 unit: m/s }
                                altitude { quantity: 0 unit: m }
                                speed { quantity: 140 unit: knots }
                                rate_descent { quantity: -5 unit: m/s }
                            }
                        }
                        segment {
                            mode: climb
                            start: { latitude: 41.262891 longitude: -8.68522 altitude: 69 m }
                            end: { latitude: 42.0 longitude: -8.01 altitude: 9249 m }
                            altitude_slots: [3000, 6000, 9000]
                            width: 50 m
                            wind: 270 deg 15 m/s
                        }
                    }
                    leg {
                        departure: LIS
                        arrival: MAD
                        fuel { quantity: 25000 unit: kg }
                        profile {
                            climb {
                                altitude { quantity: 0 unit: m }
                                speed { quantity: 210 unit: knots }
                                altitude { quantity: 12000 unit: m }
                                speed { quantity: 300 unit: knots }
                            }
                            cruise {
                                speed { quantity: 460 unit: knots }
                            }
                            descend {
                                altitude { quantity: 12000 unit: m }
                                speed { quantity: 300 unit: knots }
                                rate_descent { quantity: -10 unit: m/s }
                                altitude { quantity: 0 unit: m }
                                speed { quantity: 140 unit: knots }
                                rate_descent { quantity: -5 unit: m/s }
                            }
                        }
                        segment {
                            mode: descend
                            start: { latitude: 42.0 longitude: -8.01 altitude: 9249 m }
                            end: { latitude: 40.472 longitude: -3.560 altitude: 609 m }
                            altitude_slots: [3000, 6000, 9000]
                            width: 50 m
                            wind: 270 deg 15 m/s
                        }
                    }
                }
                """, departure);
    }
}
