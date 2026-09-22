package controller.airport;

import eapli.alsafe.airinfrastructure.domain.*;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.airports.application.RegisterAirportController;
import eapli.alsafe.airports.domain.Airport;
import eapli.alsafe.airports.domain.ICAOAirportCode;
import eapli.alsafe.airports.repository.AirportRepository;
import eapli.alsafe.utils.nodes.application.NodeService;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.alsafe.utils.nodes.domain.Node;
import eapli.alsafe.utils.nodes.domain.NodeId;
import eapli.alsafe.utils.nodes.repository.NodeRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RegisterAirportControllerTest {

    private RegisterAirportController controller;
    private AirportRepository airportRepository;
    private AirControlAreaRepository airControlAreaRepository;
    private NodeRepository nodeRepository;
    private NodeService nodeService;
    private AuthorizationService authz;

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
            return Optional.empty();
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

    private static class FakeAirControlAreaRepository implements AirControlAreaRepository {
        private final List<AirControlArea> areas = new ArrayList<>();

        @Override
        public <S extends AirControlArea> S save(S entity) {
            areas.add(entity);
            return entity;
        }

        @Override
        public Iterable<AirControlArea> findAll() {
            return areas;
        }

        @Override
        public Optional<AirControlArea> ofIdentity(AirControlAreaID id) {
            return areas.stream().filter(a -> a.identity().equals(id)).findFirst();
        }

        @Override
        public void delete(AirControlArea entity) {}

        @Override
        public void deleteOfIdentity(AirControlAreaID id) {}

        @Override
        public long count() {
            return areas.size();
        }

        @Override
        public Optional<AirControlArea> findById(AirControlAreaID id) {
            return ofIdentity(id);
        }

        @Override
        public Optional<AirControlArea> findByCode(String code) {
            return areas.stream().filter(a -> a.identity().toString().equals(code)).findFirst();
        }
    }

    private static class FakeNodeRepository implements NodeRepository {
        private final List<Node> nodes = new ArrayList<>();

        @Override
        public Optional<Node> findLastNode() {
            if (nodes.isEmpty()) return Optional.empty();
            return Optional.of(nodes.get(nodes.size() - 1));
        }

        @Override
        public <S extends Node> S save(S entity) {
            nodes.add(entity);
            return entity;
        }

        @Override
        public Iterable<Node> findAll() {
            return nodes;
        }

        @Override
        public Optional<Node> ofIdentity(NodeId id) {
            return nodes.stream().filter(n -> n.identity().equals(id)).findFirst();
        }

        @Override
        public void delete(Node entity) {}

        @Override
        public void deleteOfIdentity(NodeId id) {}

        @Override
        public long count() {
            return nodes.size();
        }
    }

    private static class FakeAuthzService extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(Role... actions) {}
    }

    @BeforeEach
    void setUp() {
        airportRepository = new FakeAirportRepository();
        airControlAreaRepository = new FakeAirControlAreaRepository();
        nodeRepository = new FakeNodeRepository();
        nodeService = new NodeService(nodeRepository);
        authz = new FakeAuthzService();
        controller = new RegisterAirportController(airportRepository, airControlAreaRepository, nodeService, authz);

        // Pre-populate an AirControlArea since it's needed for airport registration
        AirControlArea area = new AirControlArea(new AirControlAreaID(1L), "Portugal",
                new AirControlAreaBoundaries(List.of(
                        new Coordinate(42.15, -9.50),
                        new Coordinate(42.15, -7.50),
                        new Coordinate(36.95, -7.50),
                        new Coordinate(36.95, -9.50)
                )), new AirControlAreaMinimumFuel(150.0));
        airControlAreaRepository.save(area);
    }

    @Test
    void ensureRegisterAirportSavesAirport() {
        Airport result = controller.registerAirport("LPPR", "OPO", "Porto Airport",
                41.2481, -8.6814, 69.0, new AirControlAreaID(1L));

        assertNotNull(result);
        assertEquals("Porto Airport", result.getAirportName());
        assertTrue(airportRepository.findAll().iterator().hasNext());
    }

    @Test
    void ensureIcaoAlreadyExistsWorks() {
        controller.registerAirport("LPPR", "OPO", "Porto Airport",
                41.2481, -8.6814, 69.0, new AirControlAreaID(1L));

        assertTrue(controller.icaoAlreadyExists("LPPR"));
        assertFalse(controller.icaoAlreadyExists("LPPT"));
    }

    @Test
    void ensureIataAlreadyExistsWorks() {
        controller.registerAirport("LPPR", "OPO", "Porto Airport",
                41.2481, -8.6814, 69.0, new AirControlAreaID(1L));

        assertTrue(controller.iataAlreadyExists("OPO"));
        assertFalse(controller.iataAlreadyExists("LIS"));
    }

    @Test
    void ensureCoordinatesAlreadyExistWorks() {
        controller.registerAirport("LPPR", "OPO", "Porto Airport",
                41.2481, -8.6814, 69.0, new AirControlAreaID(1L));

        assertTrue(controller.coordinatesAlreadyExist(41.2481, -8.6814));
        assertFalse(controller.coordinatesAlreadyExist(38.7742, -9.1342));
    }

    @Test
    void ensureAllNodesReturnsNodes() {
        controller.registerAirport("LPPR", "OPO", "Porto Airport",
                41.2481, -8.6814, 69.0, new AirControlAreaID(1L));

        assertTrue(controller.allNodes().iterator().hasNext());
    }

    @Test
    void ensureAllAirportsReturnsAirports() {
        controller.registerAirport("LPPR", "OPO", "Porto Airport",
                41.2481, -8.6814, 69.0, new AirControlAreaID(1L));

        assertTrue(controller.allAirports().iterator().hasNext());
    }

    @Test
    void ensureAllAirControlAreasReturnsAreas() {
        assertTrue(controller.allAirControlAreas().iterator().hasNext());
    }

    @Test
    void ensureRegisterAirportFailsWhenAreaNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.registerAirport("LPPR", "OPO", "Porto Airport",
                        41.2481, -8.6814, 69.0, new AirControlAreaID(999L))
        );
    }
}
