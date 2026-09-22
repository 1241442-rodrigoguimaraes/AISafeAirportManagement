package controller.airinfrastructure;

import eapli.alsafe.airinfrastructure.application.DeleteFlightRouteController;
import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.airinfrastructure.domain.FlightRouteName;
import eapli.alsafe.airinfrastructure.repositories.FlightRouteRepository;
import eapli.alsafe.airports.domain.Airport;
import eapli.alsafe.airports.domain.IATAAirportCode;
import eapli.alsafe.airports.domain.ICAOAirportCode;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.IATACompanyCode;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.flightPlan.domain.FlightPlanID;
import eapli.alsafe.flightPlan.repositories.FlightPlanRepository;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.alsafe.utils.nodes.domain.Altitude;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.alsafe.utils.nodes.domain.Node;
import eapli.alsafe.utils.nodes.domain.NodeId;
import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaBoundaries;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaMinimumFuel;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DeleteFlightRouteControllerTest {

    @Setter
    private static class FakeAuthorizationService extends AuthorizationService {
        private UserSession session;

        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... actions) {}

        @Override
        public Optional<UserSession> session() {
            return Optional.ofNullable(session);
        }
    }

    private static class FakeFlightRouteRepository implements FlightRouteRepository {
        final List<FlightRoute> routes = new ArrayList<>();

        @Override
        public Optional<FlightRoute> findByName(String name) {
            return routes.stream()
                    .filter(r -> r.identity().getName().equalsIgnoreCase(name))
                    .findFirst();
        }

        @Override
        public Iterable<FlightRoute> findByCompany(AirTransportCompany company) {
            return routes;
        }

        @Override
        public <S extends FlightRoute> S save(S entity) {
            routes.add(entity);
            return entity;
        }

        @Override
        public Iterable<FlightRoute> findAll() {
            return routes;
        }

        @Override
        public Optional<FlightRoute> ofIdentity(FlightRouteName name) {
            return routes.stream().filter(r -> r.identity().equals(name)).findFirst();
        }

        @Override
        public void delete(FlightRoute entity) {
            routes.remove(entity);
        }

        @Override
        public void deleteOfIdentity(FlightRouteName name) {
            routes.removeIf(r -> r.identity().equals(name));
        }

        @Override
        public long count() {
            return routes.size();
        }
    }

    private static class FakeFlightPlanRepository implements FlightPlanRepository {
        private final Set<FlightRoute> routesWithFuturePlans = new HashSet<>();

        void blockRoute(FlightRoute route) {
            routesWithFuturePlans.add(route);
        }

        @Override
        public Iterable<FlightPlan> findByFlightRouteAndAfterDate(FlightRoute flightRoute, String date) {
            if (routesWithFuturePlans.contains(flightRoute)) return Collections.singletonList(null);

            return List.of();
        }

        @Override
        public Iterable<FlightPlan> findBySystemUser(SystemUser user) {
            return List.of();
        }

        @Override
        public Iterable<FlightPlan> findByPilot(SystemUser pilot) {
            return List.of();
        }

        @Override
        public Iterable<FlightPlan> findAssignedFlightPlansByPilot(Pilot pilot) {
            return List.of();
        }

        @Override
        public <S extends FlightPlan> S save(S entity) { return entity; }

        @Override
        public Iterable<FlightPlan> findAll() { return List.of(); }

        @Override
        public Optional<FlightPlan> ofIdentity(FlightPlanID id) { return Optional.empty(); }

        @Override
        public void delete(FlightPlan entity) {}

        @Override
        public void deleteOfIdentity(FlightPlanID id) {}

        @Override
        public long count() { return 0; }
    }

    private static class FakeCollaboratorATCCRepository implements CollaboratorRepositoryATCC {
        final List<CollaboratorATCC> store = new ArrayList<>();

        @Override
        public CollaboratorATCC save(final CollaboratorATCC atcc) {
            store.add(atcc);
            return atcc;
        }

        @Override
        public Optional<CollaboratorATCC> findByEmail(CollaboratorEmail email) {
            return store.stream().filter(c -> c.email().equals(email)).findFirst();
        }

        @Override
        public Optional<CollaboratorATCC> findBySystemUser(SystemUser systemUser) {
            return store.stream().filter(c -> c.user().equals(systemUser)).findFirst();
        }

        @Override
        public Optional<AirTransportCompany> findCompanyByCollaborator(CollaboratorATCC collaborator) {
            return Optional.empty();
        }

        @Override
        public Iterable<CollaboratorATCC> findActiveByCompany(AirTransportCompany company) {
            return List.of();
        }

        @Override
        public Iterable<CollaboratorATCC> findAll() { return store; }

        @Override
        public Optional<CollaboratorATCC> ofIdentity(final Long id) { return Optional.empty(); }

        @Override
        public void delete(final CollaboratorATCC atcc) { store.remove(atcc); }

        @Override
        public void deleteOfIdentity(final Long id) {}

        @Override
        public long count() { return store.size(); }

        @Override
        public boolean containsOfIdentity(final Long id) { return false; }
    }

    private FakeFlightRouteRepository frRepository;
    private FakeFlightPlanRepository fpRepository;
    private FakeCollaboratorATCCRepository atccRepository;
    private FakeAuthorizationService authz;
    private DeleteFlightRouteController controller;

    private AirTransportCompany company;
    private FlightRoute routeA;
    private FlightRoute routeB;

    @BeforeEach
    void setUp() {
        frRepository = new FakeFlightRouteRepository();
        fpRepository = new FakeFlightPlanRepository();
        atccRepository = new FakeCollaboratorATCCRepository();
        authz = new FakeAuthorizationService();

        SystemUser user = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with("tester", "Password1", "Tester", "User", "tester@alsafe.com")
                .withRoles(Set.of(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR).toArray(new Role[0]))
                .build();

        authz.setSession(new UserSession(user));

        company = new AirTransportCompany(
                new IATACompanyCode("TP"),
                new ICAOCompanyCode("TAP"),
                "TAP Portugal"
        );

        CollaboratorATCC collab = new CollaboratorATCC(
                "John Doe",
                new CollaboratorEmail("john@tap.com"),
                new CollaboratorPhone("912345678"),
                user,
                company
        );
        atccRepository.save(collab);

        AirControlArea area1 = new AirControlArea(
                new AirControlAreaID(1L), "Area1",
                new AirControlAreaBoundaries(List.of(
                        new Coordinate(51.09, -4.79),
                        new Coordinate(51.09,  8.23),
                        new Coordinate(42.33,  8.23),
                        new Coordinate(42.33, -4.79)
                )),
                new AirControlAreaMinimumFuel(250.0)
        );
        AirControlArea area2 = new AirControlArea(
                new AirControlAreaID(2L), "Area2",
                new AirControlAreaBoundaries(List.of(
                        new Coordinate(47.81,  8.23),
                        new Coordinate(47.81, 10.49),
                        new Coordinate(45.82, 10.49),
                        new Coordinate(45.82,  8.23)
                )),
                new AirControlAreaMinimumFuel(300.0)
        );

        Airport airportLIS = new Airport(new ICAOAirportCode("LISB"), new IATAAirportCode("LIS"),
                "Lisbon Airport", new Node(new NodeId(1), new Coordinate(38.7, -9.1), new Altitude(20.0)), area1);
        Airport airportOPO = new Airport(new ICAOAirportCode("OPOR"), new IATAAirportCode("OPO"),
                "Porto Airport", new Node(new NodeId(2), new Coordinate(41.2, -8.6), new Altitude(20.0)), area2);
        Airport airportFAO = new Airport(new ICAOAirportCode("LPFR"), new IATAAirportCode("FAO"),
                "Faro Airport", new Node(new NodeId(3), new Coordinate(37.0, -7.9), new Altitude(20.0)), area1);

        routeA = new FlightRoute(new FlightRouteName("TP001"), airportLIS, airportOPO, company);
        routeB = new FlightRoute(new FlightRouteName("TP002"), airportLIS, airportFAO, company);

        frRepository.save(routeA);
        frRepository.save(routeB);

        controller = new DeleteFlightRouteController(authz, atccRepository, fpRepository, frRepository);
    }

    @Test
    void ensureDeleteFlightRouteControllerCannotBeCreatedWithNullAuthz() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeleteFlightRouteController(null, atccRepository, fpRepository, frRepository));
    }

    @Test
    void ensureDeleteFlightRouteControllerCannotBeCreatedWithNullCollaboratorRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeleteFlightRouteController(authz, null, fpRepository, frRepository));
    }

    @Test
    void ensureDeleteFlightRouteControllerCannotBeCreatedWithNullFlightPlanRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeleteFlightRouteController(authz, atccRepository, null, frRepository));
    }

    @Test
    void ensureDeleteFlightRouteControllerCannotBeCreatedWithNullFlightRouteRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeleteFlightRouteController(authz, atccRepository, fpRepository, null));
    }

    @Test
    void ensureGetCompanyRoutesReturnsAllRoutesWhenNoFuturePlansExist() {
        List<String> result = controller.getCompanyRoutes("01/01/2030");

        assertEquals(2, result.size());
        assertTrue(result.contains("TP001"));
        assertTrue(result.contains("TP002"));
    }

    @Test
    void ensureGetCompanyRoutesExcludesRoutesWithFuturePlans() {
        fpRepository.blockRoute(routeA);

        List<String> result = controller.getCompanyRoutes("01/01/2030");

        assertEquals(1, result.size());
        assertTrue(result.contains("TP002"));
        assertFalse(result.contains("TP001"));
    }

    @Test
    void ensureGetCompanyRoutesReturnsEmptyListWhenAllRoutesHaveFuturePlans() {
        fpRepository.blockRoute(routeA);
        fpRepository.blockRoute(routeB);

        List<String> result = controller.getCompanyRoutes("01/01/2030");

        assertTrue(result.isEmpty());
    }

    @Test
    void ensureDeleteFlightRouteSuccessfullyDeletesExistingRoute() {
        controller.deleteFlightRoute("TP001");

        assertEquals(1, frRepository.count());
        assertTrue(frRepository.findByName("TP001").isEmpty());
    }

    @Test
    void ensureDeleteFlightRouteThrowsWhenRouteDoesNotExist() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.deleteFlightRoute("TP999"));
    }
}
