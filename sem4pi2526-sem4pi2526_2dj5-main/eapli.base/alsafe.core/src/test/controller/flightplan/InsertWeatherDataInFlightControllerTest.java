package controller.flightplan;

import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.aircraft.domain.AircraftCountry;
import eapli.alsafe.aircraft.domain.CabinConfiguration;
import eapli.alsafe.aircraft.domain.CrewElement;
import eapli.alsafe.aircraft.domain.RegistrationID;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.domain.MakerCountry;
import eapli.alsafe.aircraftModelMagnement.domain.MakerName;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelName;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelPhysicsData;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelType;
import eapli.alsafe.aircraftModelMagnement.domain.maximumRange;
import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaBoundaries;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaMinimumFuel;
import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.airinfrastructure.domain.FlightRouteName;
import eapli.alsafe.airports.domain.Airport;
import eapli.alsafe.airports.domain.IATAAirportCode;
import eapli.alsafe.airports.domain.ICAOAirportCode;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.IATACompanyCode;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.alsafe.engineModelMagnement.Domain.engineModel;
import eapli.alsafe.engineModelMagnement.Domain.engineModelEfficiency;
import eapli.alsafe.engineModelMagnement.Domain.engineModelFuel;
import eapli.alsafe.engineModelMagnement.Domain.engineModelName;
import eapli.alsafe.engineModelMagnement.Domain.engineModelPower;
import eapli.alsafe.engineModelMagnement.Domain.engineType;
import eapli.alsafe.flightPlan.application.InsertWeatherDataInFlightController;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.flightPlan.domain.FlightPlanID;
import eapli.alsafe.flightPlan.domain.FuelQuantity;
import eapli.alsafe.flightPlan.domain.FuelUnit;
import eapli.alsafe.flightPlan.repositories.FlightPlanRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.infrastructure.persistence.RepositoryFactory;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.alsafe.utils.nodes.domain.Altitude;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.alsafe.utils.nodes.domain.Node;
import eapli.alsafe.utils.nodes.domain.NodeId;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.alsafe.weather.domain.WeatherDate;
import eapli.alsafe.weather.domain.WindCondition;
import eapli.alsafe.weather.repositories.WeatherDataRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InsertWeatherDataInFlightControllerTest {

    private AuthorizationService authz;
    private FlightPlanRepository flightPlanRepository;
    private WeatherDataRepository weatherDataRepository;
    private InsertWeatherDataInFlightController controller;

    private static SystemUser pilotUser(final String email) {
        return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with(email, "Password1", "John", "Pilot", email)
                .withRoles(Roles.PILOT)
                .build();
    }

    private static AirTransportCompany company() {
        return new AirTransportCompany(new IATACompanyCode("TP"), new ICAOCompanyCode("TAP"), "TAP Air");
    }

    private static aircraftModel aircraftModel() {
        final Maker maker = new Maker(new MakerName("MakerCo"), new MakerCountry("PT"));
        final engineModel engine = new engineModel(new engineModelName("E1"), maker, engineType.TURBOFAN,
                new engineModelPower(1000), engineModelFuel.JET_A1, new engineModelEfficiency(0.9));
        return new aircraftModel(new aircraftModelName("A320"), maker, aircraftModelType.PASSENGER,
                engineType.TURBOFAN, new maximumRange(1000),
                new aircraftModelPhysicsData(1000, 2000, 1500, 500, 30000, 800, 50, 0.02, 1.2),
                Set.of(engine));
    }

    private static Pilot pilot(final SystemUser user, final AirTransportCompany company) {
        return new Pilot("John Pilot", CollaboratorEmail.valueOf(user.email().toString()),
                CollaboratorPhone.valueOf("912345678"), user, company, Set.of(aircraftModel()));
    }

    private static FlightPlan flightPlanFor(final Pilot pilot) {
        final AirTransportCompany company = pilot.company();
        final aircraftModel model = aircraftModel();
        final int max = model.maxPassengerSeats();
        final Aircraft aircraft = Aircraft.create(model, RegistrationID.valueOf("CS-TST"),
                new AircraftCountry("PT"), new CabinConfiguration(Math.min(10, max), 0, 0, max),
                List.of(new CrewElement("PILOT")), company);
        final Node node = new Node(new NodeId(1), new Coordinate(38.7, -9.1), new Altitude(20.0));
        final List<Coordinate> boundaries = List.of(
                new Coordinate(51.0, -5.0), new Coordinate(51.0, 9.0),
                new Coordinate(42.0, 9.0), new Coordinate(42.0, -5.0));
        final AirControlArea area = new AirControlArea(new AirControlAreaID(1L), "Area",
                new AirControlAreaBoundaries(boundaries), new AirControlAreaMinimumFuel(250.0));
        final Airport origin = new Airport(new ICAOAirportCode("LPPT"), new IATAAirportCode("LIS"),
                "Lisbon", node, area);
        final Airport destination = new Airport(new ICAOAirportCode("LPMA"), new IATAAirportCode("FNC"),
                "Funchal", node, area);
        final FlightRoute route = new FlightRoute(new FlightRouteName("TP1234"), origin, destination, company);
        return FlightPlan.create(new FlightPlanID("TP123"), route, aircraft, pilot,
                LocalDateTime.now().plusDays(1), new FuelQuantity(5000, FuelUnit.L), "content");
    }

    private static WeatherData weatherData() {
        final Calendar date = Calendar.getInstance();
        date.add(Calendar.DAY_OF_MONTH, -1);
        final List<Coordinate> boundaries = List.of(
                new Coordinate(51.0, -5.0), new Coordinate(51.0, 9.0),
                new Coordinate(42.0, 9.0), new Coordinate(42.0, -5.0));
        final AirControlArea area = new AirControlArea(new AirControlAreaID(2L), "WeatherArea",
                new AirControlAreaBoundaries(boundaries), new AirControlAreaMinimumFuel(250.0));
        return new WeatherData(area, new WeatherDate(date), new WindCondition(90, 8.0));
    }

    private void stubAuthenticatedPilot(final SystemUser user) {
        final UserSession session = mock(UserSession.class);
        when(session.authenticatedUser()).thenReturn(user);
        when(authz.session()).thenReturn(Optional.of(session));
    }

    @BeforeEach
    void setUp() {
        authz = mock(AuthorizationService.class);
        flightPlanRepository = mock(FlightPlanRepository.class);
        weatherDataRepository = mock(WeatherDataRepository.class);
        controller = new InsertWeatherDataInFlightController(authz, flightPlanRepository, weatherDataRepository);
    }

    @Test
    void ensureConstructorRejectsNullDependencies() {
        assertThrows(IllegalArgumentException.class,
                () -> new InsertWeatherDataInFlightController(null, flightPlanRepository, weatherDataRepository));
        assertThrows(IllegalArgumentException.class,
                () -> new InsertWeatherDataInFlightController(authz, null, weatherDataRepository));
        assertThrows(IllegalArgumentException.class,
                () -> new InsertWeatherDataInFlightController(authz, flightPlanRepository, null));
    }

    @Test
    void defaultConstructorUsesApplicationServices() {
        final RepositoryFactory repositories = mock(RepositoryFactory.class);
        try (var authzRegistry = mockStatic(AuthzRegistry.class);
             var persistenceContext = mockStatic(PersistenceContext.class)) {
            authzRegistry.when(AuthzRegistry::authorizationService).thenReturn(authz);
            persistenceContext.when(PersistenceContext::repositories).thenReturn(repositories);
            when(repositories.flightPlan()).thenReturn(flightPlanRepository);
            when(repositories.weatherData()).thenReturn(weatherDataRepository);

            assertDoesNotThrow(() -> new InsertWeatherDataInFlightController());
        }
    }

    @Test
    void ensurePilotCanListOwnFlightPlans() {
        final SystemUser user = pilotUser("pilot@tap.pt");
        final Pilot pilotEntity = pilot(user, company());
        final FlightPlan plan = flightPlanFor(pilotEntity);
        stubAuthenticatedPilot(user);
        when(flightPlanRepository.findBySystemUser(user)).thenReturn(List.of(plan));

        final List<FlightPlan> plans = new ArrayList<>();
        controller.myFlightPlans().forEach(plans::add);

        assertEquals(1, plans.size());
        verify(authz).ensureAuthenticatedUserHasAnyOf(Roles.PILOT);
        verify(flightPlanRepository).findBySystemUser(user);
    }

    @Test
    void ensureUnauthorizedUserCannotListFlightPlans() {
        doThrow(new SecurityException("not authorized"))
                .when(authz).ensureAuthenticatedUserHasAnyOf(any(Role.class));

        assertThrows(SecurityException.class, () -> controller.myFlightPlans());
    }

    @Test
    void ensurePilotCanListAvailableWeatherData() {
        final SystemUser user = pilotUser("pilot@tap.pt");
        final WeatherData data = weatherData();
        stubAuthenticatedPilot(user);
        when(weatherDataRepository.findAll()).thenReturn(List.of(data));

        final List<WeatherData> result = new ArrayList<>();
        controller.availableWeatherData().forEach(result::add);

        assertEquals(1, result.size());
        verify(authz).ensureAuthenticatedUserHasAnyOf(Roles.PILOT);
        verify(weatherDataRepository).findAll();
    }

    @Test
    void ensurePilotCanInsertWeatherDataIntoOwnFlightPlan() {
        final SystemUser user = pilotUser("pilot@tap.pt");
        final Pilot pilotEntity = pilot(user, company());
        final FlightPlan plan = flightPlanFor(pilotEntity);
        final WeatherData data = weatherData();
        stubAuthenticatedPilot(user);
        when(flightPlanRepository.save(plan)).thenReturn(plan);

        final FlightPlan result = controller.insertWeatherData(plan, data);

        assertEquals(data, result.getWeatherData());
        assertFalse(result.isTested());
        verify(flightPlanRepository).save(plan);
        verify(authz, atLeastOnce()).ensureAuthenticatedUserHasAnyOf(Roles.PILOT);
    }

    @Test
    void ensureInsertingWeatherDataVoidsPreviousTestResult() {
        final SystemUser user = pilotUser("pilot@tap.pt");
        final Pilot pilotEntity = pilot(user, company());
        final FlightPlan plan = flightPlanFor(pilotEntity);
        plan.markAsTested();
        final WeatherData data = weatherData();
        stubAuthenticatedPilot(user);
        when(flightPlanRepository.save(plan)).thenReturn(plan);

        final FlightPlan result = controller.insertWeatherData(plan, data);

        assertFalse(result.isTested());
        assertEquals(data, result.getWeatherData());
    }

    @Test
    void ensurePilotCannotInsertWeatherDataIntoAnotherPilotsFlightPlan() {
        final SystemUser owner = pilotUser("owner@tap.pt");
        final SystemUser other = pilotUser("other@tap.pt");
        final FlightPlan plan = flightPlanFor(pilot(owner, company()));
        stubAuthenticatedPilot(other);

        assertThrows(IllegalArgumentException.class,
                () -> controller.insertWeatherData(plan, weatherData()));
        verify(flightPlanRepository, never()).save(any());
    }

    @Test
    void ensureUnauthorizedUserCannotInsertWeatherData() {
        final SystemUser user = pilotUser("pilot@tap.pt");
        final FlightPlan plan = flightPlanFor(pilot(user, company()));
        doThrow(new SecurityException("not authorized"))
                .when(authz).ensureAuthenticatedUserHasAnyOf(any(Role.class));

        assertThrows(SecurityException.class,
                () -> controller.insertWeatherData(plan, weatherData()));
        verify(flightPlanRepository, never()).save(any());
    }

    @Test
    void ensureMissingSessionIsRejectedWhenListingFlightPlans() {
        when(authz.session()).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> controller.myFlightPlans());
    }
}
