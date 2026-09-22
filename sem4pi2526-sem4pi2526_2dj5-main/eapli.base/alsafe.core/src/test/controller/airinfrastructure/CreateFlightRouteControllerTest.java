package controller.airinfrastructure;

import eapli.alsafe.airinfrastructure.application.CreateFlightRouteController;
import eapli.alsafe.airinfrastructure.domain.*;
import eapli.alsafe.airinfrastructure.repositories.FlightRouteRepository;
import eapli.alsafe.airports.domain.Airport;
import eapli.alsafe.airports.domain.IATAAirportCode;
import eapli.alsafe.airports.domain.ICAOAirportCode;
import eapli.alsafe.airports.repository.AirportRepository;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.utils.nodes.domain.Altitude;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.alsafe.utils.nodes.domain.Node;
import eapli.alsafe.utils.nodes.domain.NodeId;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.domain.repositories.IntegrityViolationException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CreateFlightRouteControllerTest {

    @Setter
    private static class FakeAuthorizationService extends AuthorizationService {
        private UserSession session;

        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... actions) {
        }

        @Override
        public Optional<UserSession> session() {
            return Optional.ofNullable(session);
        }
    }

    private static class FakeFlightRouteRepository implements FlightRouteRepository {
        private final List<FlightRoute> routes = new ArrayList<>();

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
            return routes.stream().filter(a -> a.identity().equals(name)).findFirst();
        }

        @Override
        public void delete(FlightRoute entity) {}

        @Override
        public void deleteOfIdentity(FlightRouteName name) {}

        @Override
        public long count() {
            return routes.size();
        }
    }

    private static class FakeAirportRepository implements AirportRepository {
        private final List<Airport> airports = new ArrayList<>();

        @Override
        public boolean existsByIATACode(String iataCode) {
            return airports.stream().anyMatch(a -> a.getIataCode().toString().equals(iataCode));
        }

        @Override
        public boolean existsByICAOCode(String icaoCode) {
            return airports.stream().anyMatch(a -> a.getIcaoCode().toString().equals(icaoCode));
        }

        @Override
        public boolean sameCoordinates(Coordinate coordinates) {
            return airports.stream().anyMatch(a -> a.getLocation().getCoordinates().equals(coordinates));
        }

        @Override
        public Optional<Airport> findByName(String name) {
            return airports.stream()
                    .filter(a -> a.getAirportName().equalsIgnoreCase(name))
                    .findFirst();
        }

        @Override
        public <S extends Airport> S save(S entity) {
            airports.add(entity);
            return entity;
        }

        @Override
        public Iterable<Airport> findAll() {
            return airports;
        }

        @Override
        public Optional<Airport> ofIdentity(ICAOAirportCode id) {
            return airports.stream().filter(a -> a.identity().equals(id)).findFirst();
        }

        @Override
        public void delete(Airport entity) {}

        @Override
        public void deleteOfIdentity(ICAOAirportCode id) {}

        @Override
        public long count() {
            return airports.size();
        }
    }

    private static class FakeCollaboratorATCCRepository implements CollaboratorRepositoryATCC {
        public final List<CollaboratorATCC> store = new ArrayList<>();

        @SuppressWarnings("unchecked")
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
        public Iterable<CollaboratorATCC> findAll() {
            return store;
        }

        @Override
        public Optional<CollaboratorATCC> ofIdentity(final Long id) {
            return Optional.empty();
        }

        @Override
        public void delete(final CollaboratorATCC atcc) {
            store.remove(atcc);
        }

        @Override
        public void deleteOfIdentity(final Long id) {}

        @Override
        public long count() {
            return store.size();
        }

        @Override
        public boolean containsOfIdentity(final Long id) {
            return false;
        }
    }

    private FlightRouteBuilder routeBuilder;
    private FakeFlightRouteRepository frRepository;
    private FakeAirportRepository airportRepository;
    private FakeCollaboratorATCCRepository atccRepository;
    private CreateFlightRouteController controller;
    private FakeAuthorizationService authz;

    @BeforeEach
    void setUp() {
        routeBuilder = new FlightRouteBuilder();
        frRepository = new FakeFlightRouteRepository();
        airportRepository = new FakeAirportRepository();
        atccRepository = new FakeCollaboratorATCCRepository();
        authz = new FakeAuthorizationService();

        SystemUser user = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with("tester", "Password1", "Tester", "User", "tester@alsafe.com")
                .withRoles(Set.of(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR).toArray(new Role[0]))
                .build();

        authz.setSession(new UserSession(user));

        controller = new CreateFlightRouteController(authz, routeBuilder, frRepository, airportRepository, atccRepository);

        AirTransportCompany company = new AirTransportCompany(
                new eapli.alsafe.companies.domain.IATACompanyCode("TP"),
                new eapli.alsafe.companies.domain.ICAOCompanyCode("TAP"),
                "TAP Portugal"
        );

        CollaboratorATCC collab = new CollaboratorATCC(
                "John Doe",
                new CollaboratorEmail("john@tap.com"),
                new eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone("912345678"),
                user,
                company
        );
        atccRepository.save(collab);

        ICAOAirportCode icao1 = new ICAOAirportCode("LISB");
        ICAOAirportCode icao2 = new ICAOAirportCode("OPOR");

        IATAAirportCode iata1 = new IATAAirportCode("LIS");
        IATAAirportCode iata2 = new IATAAirportCode("OPO");

        String airportName1 = "Lisbon Airport";
        String airportName2 = "Porto Airport";

        Node location1 = new Node(new NodeId(1), new Coordinate(48.7275, 2.3636), new Altitude(20.0));
        Node location2 = new Node(new NodeId(2), new Coordinate(47.3769, 8.5392), new Altitude(20.0));

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

        Airport airport1 = new Airport(icao1, iata1, airportName1, location1, area1);
        Airport airport2 = new Airport(icao2, iata2, airportName2, location2, area2);

        airportRepository.save(airport1);
        airportRepository.save(airport2);
    }

    @Test
    void ensureCreateFlightRouteControllerCannotBeCreatedWithNullAuthz() {
        assertThrows(IllegalArgumentException.class, () -> new CreateFlightRouteController(null, routeBuilder, frRepository, airportRepository, atccRepository));

    }

    @Test
    void ensureCreateFlightRouteControllerCannotBeCreatedWithNullRouteBuilder() {
        assertThrows(IllegalArgumentException.class, () -> new CreateFlightRouteController(authz, null, frRepository, airportRepository, atccRepository));
    }

    @Test
    void ensureCreateFlightRouteControllerCannotBeCreatedWithNullFlightRouteRepository() {
        assertThrows(IllegalArgumentException.class, () -> new CreateFlightRouteController(authz, routeBuilder, null, airportRepository, atccRepository));
    }

    @Test
    void ensureCreateFlightRouteControllerCannotBeCreatedWithNullAirportRepository() {
        assertThrows(IllegalArgumentException.class, () -> new CreateFlightRouteController(authz, routeBuilder, frRepository, null, atccRepository));
    }

    @Test
    void ensureCreateFlightRouteControllerCannotBeCreatedWithNullAirCompanyRepository() {
        assertThrows(IllegalArgumentException.class, () -> new CreateFlightRouteController(authz, routeBuilder, frRepository, airportRepository, null));
    }

    @Test
    void ensureCheckNameThrowsExceptionWhenFlightRouteAlreadyExists() {
        controller.createFlightRoute("123", "Lisbon Airport", "Porto Airport");

        assertThrows(IntegrityViolationException.class, () -> controller.checkName("TP123"));
    }

    @Test
    void ensureCreateFlightRouteThrowsExceptionWhenStartingAirportNotFound() {
        assertThrows(IllegalArgumentException.class, () -> controller.createFlightRoute("123", "Nonexistent Airport", "Porto Airport"));
    }

    @Test
    void ensureCreateFlightRouteThrowsExceptionWhenDestinationAirportNotFound() {
        assertThrows(IllegalArgumentException.class, () -> controller.createFlightRoute("123", "Lisbon Airport", "Nonexistent Airport"));
    }

    @Test
    void ensureCreateFlightRouteThrowsExceptionWhenFlightRouteNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> controller.createFlightRoute("", "Lisbon Airport", "Porto Airport"));
    }

    @Test
    void ensureCreateFlightRouteSavesAndReturnsFlightRoute() {
        FlightRoute flightRoute = controller.createFlightRoute("123", "Lisbon Airport", "Porto Airport");

        assertNotNull(flightRoute);
        assertEquals(1, frRepository.routes.size());
        assertEquals("TP123", flightRoute.identity().getName());
    }
}
