package controller.aircraft;

import eapli.alsafe.aircraft.application.AircraftDTO;
import eapli.alsafe.aircraft.application.ListFleetController;
import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.aircraft.domain.AircraftCountry;
import eapli.alsafe.aircraft.domain.CabinConfiguration;
import eapli.alsafe.aircraft.domain.CrewElement;
import eapli.alsafe.aircraft.domain.RegistrationID;
import eapli.alsafe.aircraft.repositories.AircraftRepository;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelName;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelPhysicsData;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelType;
import eapli.alsafe.aircraftModelMagnement.domain.maximumRange;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.IATACompanyCode;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.alsafe.engineModelMagnement.Domain.engineModel;
import eapli.alsafe.engineModelMagnement.Domain.engineModelEfficiency;
import eapli.alsafe.engineModelMagnement.Domain.engineModelFuel;
import eapli.alsafe.engineModelMagnement.Domain.engineModelName;
import eapli.alsafe.engineModelMagnement.Domain.engineModelPower;
import eapli.alsafe.engineModelMagnement.Domain.engineType;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.domain.MakerCountry;
import eapli.alsafe.aircraftModelMagnement.domain.MakerName;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListFleetControllerTest {

    private static class FakeAuthorizationService extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {
        }
    }

    private static class FakeAircraftRepository implements AircraftRepository {
        private final List<Aircraft> store = new ArrayList<>();

        @Override
        public Aircraft save(final Aircraft entity) {
            store.add(entity);
            return entity;
        }

        @Override
        public Optional<Aircraft> ofIdentity(final RegistrationID id) {
            return store.stream().filter(a -> a.identity().equals(id)).findFirst();
        }

        @Override
        public Iterable<Aircraft> findAll() {
            return store;
        }

        @Override
        public void delete(final Aircraft entity) {
            store.remove(entity);
        }

        @Override
        public void deleteOfIdentity(final RegistrationID id) {
            store.removeIf(a -> a.identity().equals(id));
        }

        @Override
        public long count() {
            return store.size();
        }

        @Override
        public boolean containsOfIdentity(final RegistrationID id) {
            return ofIdentity(id).isPresent();
        }

        @Override
        public boolean existsByRegistrationID(final RegistrationID registrationID) {
            return store.stream().anyMatch(a -> a.identity().equals(registrationID));
        }

        @Override
        public boolean existsByAircraftModel(final aircraftModel model) {
            return store.stream().anyMatch(a -> a.model().equals(model));
        }

        @Override
        public boolean addAircraft(final aircraftModel model, final RegistrationID registrationID,
                                   final AircraftCountry country, final CabinConfiguration cabinConfig,
                                   final List<CrewElement> crew, final AirTransportCompany company) {
            final Aircraft aircraft = Aircraft.create(model, registrationID, country, cabinConfig, crew, company);
            return save(aircraft) != null;
        }

        @Override
        public Iterable<Aircraft> findByCompany(final AirTransportCompany company) {
            return store.stream().filter(a -> a.company().equals(company)).toList();
        }

        @Override
        public Iterable<Aircraft> findByCompanyAndModel(final AirTransportCompany company,
                                                        final aircraftModel model) {
            return store.stream()
                    .filter(a -> a.company().equals(company) && a.model().equals(model))
                    .toList();
        }
    }

    private static class FakeAircraftModelRepository implements aircraftModelRepository {
        private final List<aircraftModel> store = new ArrayList<>();

        @Override
        public aircraftModel save(final aircraftModel aircraftModel) {
            store.add(aircraftModel);
            return aircraftModel;
        }

        @Override
        public Optional<aircraftModel> ofIdentity(final eapli.alsafe.aircraftModelMagnement.domain.aircraftModelId id) {
            return store.stream().filter(a -> a.identity().equals(id)).findFirst();
        }

        @Override
        public Iterable<aircraftModel> findAll() {
            return store;
        }

        @Override
        public Optional<aircraftModel> findByName(final String name) {
            return store.stream().filter(a -> a.name().name().equals(name)).findFirst();
        }

        @Override
        public void delete(final aircraftModel aircraftModel) {
            store.remove(aircraftModel);
        }

        @Override
        public void deleteOfIdentity(final eapli.alsafe.aircraftModelMagnement.domain.aircraftModelId id) {
            store.removeIf(a -> a.identity().equals(id));
        }

        @Override
        public long count() {
            return store.size();
        }

        @Override
        public boolean containsOfIdentity(final eapli.alsafe.aircraftModelMagnement.domain.aircraftModelId id) {
            return ofIdentity(id).isPresent();
        }
    }

    private static class FakeCollaboratorRepositoryATCC implements CollaboratorRepositoryATCC {
        private CollaboratorATCC collaborator;
        private AirTransportCompany company;

        @Override
        public CollaboratorATCC save(final CollaboratorATCC entity) {
            this.collaborator = entity;
            return entity;
        }

        @Override
        public Optional<CollaboratorATCC> ofIdentity(final Long id) {
            return Optional.ofNullable(collaborator);
        }

        @Override
        public Iterable<CollaboratorATCC> findAll() {
            return collaborator != null ? List.of(collaborator) : List.of();
        }

        @Override
        public void delete(final CollaboratorATCC entity) {
            collaborator = null;
        }

        @Override
        public void deleteOfIdentity(final Long id) {
            collaborator = null;
        }

        @Override
        public long count() {
            return collaborator != null ? 1 : 0;
        }

        @Override
        public boolean containsOfIdentity(final Long id) {
            return collaborator != null;
        }

        @Override
        public Optional<CollaboratorATCC> findByEmail(final CollaboratorEmail email) {
            return Optional.empty();
        }

        @Override
        public Optional<CollaboratorATCC> findBySystemUser(final SystemUser systemUser) {
            return Optional.ofNullable(collaborator);
        }

        @Override
        public Optional<AirTransportCompany> findCompanyByCollaborator(final CollaboratorATCC collaborator) {
            return Optional.ofNullable(company);
        }

        @Override
        public Iterable<CollaboratorATCC> findActiveByCompany(final AirTransportCompany company) {
            return collaborator != null ? List.of(collaborator) : List.of();
        }

        public void setCollaboratorAndCompany(final CollaboratorATCC collaborator,
                                              final AirTransportCompany company) {
            this.collaborator = collaborator;
            this.company = company;
        }
    }

    private FakeAircraftRepository aircraftRepository;
    private FakeAircraftModelRepository aircraftModelRepository;
    private FakeCollaboratorRepositoryATCC collaboratorRepository;
    private AuthorizationService authz;
    private ListFleetController controller;

    private Maker airbusMaker;
    private Maker boeingMaker;
    private aircraftModel a320Model;
    private aircraftModel b787Model;
    private AirTransportCompany tapAir;

    private static Maker createMaker(final String name, final String country) {
        return new Maker(new MakerName(name), new MakerCountry(country));
    }

    private static aircraftModel createAircraftModel(final String name, final Maker maker,
                                                     final aircraftModelType type,
                                                     final int maxPassengerSeats) {
        final aircraftModelName modelName = new aircraftModelName(name);
        final maximumRange range = new maximumRange(10000);
        final double emptyWeight = 40000.0;
        final double payload = maxPassengerSeats * 90.0;
        final double mzfw = emptyWeight + payload;
        final double mtow = mzfw + 30000.0;
        final aircraftModelPhysicsData physics = new aircraftModelPhysicsData(
                emptyWeight,
                mtow,
                mzfw,
                50000.0,
                12000.0,
                800.0,
                100.0,
                0.02,
                0.5
        );
        final engineModel engine = createEngineModel("DefaultEngine", maker);
        final Set<engineModel> engines = new HashSet<>();
        engines.add(engine);
        return new aircraftModel(modelName, maker, type, engineType.TURBOFAN, range, physics, engines);
    }

    private static engineModel createEngineModel(final String name, final Maker maker) {
        return new engineModel(
                new engineModelName(name),
                maker,
                engineType.TURBOFAN,
                new engineModelPower(100000),
                engineModelFuel.JET_A1,
                new engineModelEfficiency(0.85)
        );
    }

    private static AirTransportCompany createCompany(final String name, final String icao, final String iata) {
        return new AirTransportCompany(
                new IATACompanyCode(iata),
                new ICAOCompanyCode(icao),
                name
        );
    }

    private static SystemUser createSystemUser(final String email) {
        return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with(email, "Password1", "John", "Doe", email)
                .withRoles(Set.of(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR).toArray(new Role[0]))
                .build();
    }

    private void stubAuthenticatedAtcc(final AirTransportCompany company) {
        // Arrange
        final SystemUser user = createSystemUser("atcc@tap.com");
        final CollaboratorATCC collaborator = new CollaboratorATCC(
                "John Doe",
                CollaboratorEmail.valueOf("atcc@tap.com"),
                CollaboratorPhone.valueOf("+351911111111"),
                user,
                company
        );
        collaboratorRepository.setCollaboratorAndCompany(collaborator, company);

        // Mock authz
        authz = mock(AuthorizationService.class);
        final UserSession session = mock(UserSession.class);
        when(session.authenticatedUser()).thenReturn(user);
        when(authz.session()).thenReturn(Optional.of(session));
    }

    private Aircraft createAircraft(final aircraftModel model, final String registration,
                                    final AirTransportCompany company, final int totalSeats) {
        final List<CrewElement> crew = new ArrayList<>();
        crew.add(new CrewElement("Pilot"));
        crew.add(new CrewElement("Co-Pilot"));

        final CabinConfiguration cabinConfig = new CabinConfiguration(
                totalSeats, 0, 0, model.maxPassengerSeats()
        );

        return Aircraft.create(
                model,
                new RegistrationID(registration),
                new AircraftCountry("PT"),
                cabinConfig,
                crew,
                company
        );
    }

    @BeforeEach
    void setUp() {
        aircraftRepository = new FakeAircraftRepository();
        aircraftModelRepository = new FakeAircraftModelRepository();
        collaboratorRepository = new FakeCollaboratorRepositoryATCC();
        authz = new FakeAuthorizationService();

        // Create test data
        airbusMaker = createMaker("Airbus", "FR");
        boeingMaker = createMaker("Boeing", "US");
        a320Model = createAircraftModel("A320", airbusMaker, aircraftModelType.PASSENGER, 180);
        b787Model = createAircraftModel("B787", boeingMaker, aircraftModelType.PASSENGER, 242);

        aircraftModelRepository.save(a320Model);
        aircraftModelRepository.save(b787Model);

        tapAir = createCompany("TAP Air Portugal", "TAP", "TP");

        controller = new ListFleetController(
                authz,
                aircraftRepository,
                aircraftModelRepository,
                collaboratorRepository
        );
    }

    @Test
    void ensureFleetReturnsEmptyListWhenNoAircraftInCompany() {
        // Arrange
        stubAuthenticatedAtcc(tapAir);
        controller = new ListFleetController(authz, aircraftRepository, aircraftModelRepository, collaboratorRepository);

        // Act
        final Iterable<Aircraft> result = controller.fleet();

        // Assert
        assertNotNull(result);
        assertEquals(0, ((List<Aircraft>) result).size());
    }

    @Test
    void ensureFleetReturnsAllAircraftForCompany() {
        // Arrange
        final Aircraft aircraft1 = createAircraft(a320Model, "CS-TAP1", tapAir, 150);
        final Aircraft aircraft2 = createAircraft(b787Model, "CS-TAP2", tapAir, 200);
        aircraftRepository.save(aircraft1);
        aircraftRepository.save(aircraft2);

        stubAuthenticatedAtcc(tapAir);
        controller = new ListFleetController(authz, aircraftRepository, aircraftModelRepository, collaboratorRepository);

        // Act
        final Iterable<Aircraft> result = controller.fleet();

        // Assert
        assertNotNull(result);
        assertEquals(2, ((List<Aircraft>) result).size());
    }

    @Test
    void ensureFleetByModelReturnsAircraftWithSpecificModel() {
        // Arrange
        final Aircraft aircraft1 = createAircraft(a320Model, "CS-TAP1", tapAir, 150);
        final Aircraft aircraft2 = createAircraft(a320Model, "CS-TAP2", tapAir, 160);
        final Aircraft aircraft3 = createAircraft(b787Model, "CS-TAP3", tapAir, 200);
        aircraftRepository.save(aircraft1);
        aircraftRepository.save(aircraft2);
        aircraftRepository.save(aircraft3);

        stubAuthenticatedAtcc(tapAir);
        controller = new ListFleetController(authz, aircraftRepository, aircraftModelRepository, collaboratorRepository);

        // Act
        final Iterable<Aircraft> result = controller.fleetByModel(a320Model);

        // Assert
        assertNotNull(result);
        final List<Aircraft> resultList = (List<Aircraft>) result;
        assertEquals(2, resultList.size());
        assertTrue(resultList.stream().allMatch(a -> a.model().equals(a320Model)));
    }

    @Test
    void ensureFleetByModelThrowsWhenModelIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.fleetByModel(null));
    }

    @Test
    void ensureFleetByMakerReturnsAircraftFromMaker() {
        // Arrange
        final Aircraft aircraft1 = createAircraft(a320Model, "CS-TAP1", tapAir, 150);
        final Aircraft aircraft2 = createAircraft(a320Model, "CS-TAP2", tapAir, 160);
        final Aircraft aircraft3 = createAircraft(b787Model, "CS-TAP3", tapAir, 200);
        aircraftRepository.save(aircraft1);
        aircraftRepository.save(aircraft2);
        aircraftRepository.save(aircraft3);

        stubAuthenticatedAtcc(tapAir);
        controller = new ListFleetController(authz, aircraftRepository, aircraftModelRepository, collaboratorRepository);

        // Act
        final Iterable<Aircraft> result = controller.fleetByMaker(airbusMaker);

        // Assert
        assertNotNull(result);
        final List<Aircraft> resultList = (List<Aircraft>) result;
        assertEquals(2, resultList.size());
        assertTrue(resultList.stream().allMatch(a -> a.model().maker().equals(airbusMaker)));
    }

    @Test
    void ensureFleetByMakerThrowsWhenMakerIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.fleetByMaker(null));
    }

    @Test
    void ensureFleetByCapacityReturnsAircraftWithCapacity() {
        // Arrange
        final Aircraft aircraft1 = createAircraft(a320Model, "CS-TAP1", tapAir, 150);
        final Aircraft aircraft2 = createAircraft(a320Model, "CS-TAP2", tapAir, 150);
        final Aircraft aircraft3 = createAircraft(b787Model, "CS-TAP3", tapAir, 200);
        aircraftRepository.save(aircraft1);
        aircraftRepository.save(aircraft2);
        aircraftRepository.save(aircraft3);

        stubAuthenticatedAtcc(tapAir);
        controller = new ListFleetController(authz, aircraftRepository, aircraftModelRepository, collaboratorRepository);

        // Act
        final Iterable<Aircraft> result = controller.fleetByCapacity(150);

        // Assert
        assertNotNull(result);
        final List<Aircraft> resultList = (List<Aircraft>) result;
        assertEquals(2, resultList.size());
        assertTrue(resultList.stream().allMatch(a -> a.cabinConfiguration().totalSeats() == 150));
    }

    @Test
    void ensureFleetByCapacityThrowsWhenCapacityIsZero() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.fleetByCapacity(0));
    }

    @Test
    void ensureFleetByCapacityThrowsWhenCapacityIsNegative() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.fleetByCapacity(-10));
    }

    @Test
    void ensureAircraftModelsReturnsAllModels() {
        // Arrange
        final FakeAuthorizationService authz = new FakeAuthorizationService();

        controller = new ListFleetController(authz, aircraftRepository, aircraftModelRepository, collaboratorRepository);

        // Act
        final Iterable<aircraftModel> result = controller.aircraftModels();

        // Assert
        assertNotNull(result);
        assertEquals(2, aircraftModelRepository.count());
        assertTrue(result.iterator().hasNext());
    }

    @Test
    void ensureAircraftModelsReturnsEmptyWhenNoModels() {
        // Arrange
        final FakeAircraftModelRepository emptyModelRepository = new FakeAircraftModelRepository();
        final FakeAuthorizationService authz = new FakeAuthorizationService();

        controller = new ListFleetController(authz, aircraftRepository, emptyModelRepository, collaboratorRepository);

        // Act
        final Iterable<aircraftModel> result = controller.aircraftModels();

        // Assert
        assertNotNull(result);
        assertEquals(0, emptyModelRepository.count());
    }

    @Test
    void ensureToDtoConvertsAircraftCorrectly() {
        // Arrange
        final Aircraft aircraft = createAircraft(a320Model, "CS-TAP1", tapAir, 150);

        controller = new ListFleetController(
                new FakeAuthorizationService(),
                aircraftRepository,
                aircraftModelRepository,
                collaboratorRepository
        );

        // Act
        final AircraftDTO dto = controller.toDto(aircraft);

        // Assert
        assertNotNull(dto);
        assertEquals("CS-TAP1", dto.registrationID);
        assertTrue(dto.aircraftModel.contains("A320"));
        assertEquals("TAP Air Portugal", dto.company);
        assertTrue(dto.cabinConfiguration.contains("150"));
        assertEquals("OPERATIONAL", dto.maintenanceStatus);
    }

    @Test
    void ensureToDtoContainsAllAircraftData() {
        // Arrange
        final Aircraft aircraft = createAircraft(b787Model, "CS-DLH1", tapAir, 242);
        aircraft.decommission();

        controller = new ListFleetController(
                new FakeAuthorizationService(),
                aircraftRepository,
                aircraftModelRepository,
                collaboratorRepository
        );

        // Act
        final AircraftDTO dto = controller.toDto(aircraft);

        // Assert
        assertNotNull(dto);
        assertEquals("CS-DLH1", dto.registrationID);
        assertTrue(dto.aircraftModel.contains("B787"));
        assertEquals("TAP Air Portugal", dto.company);
        assertEquals("DECOMMISSIONED", dto.maintenanceStatus);
    }

    @Test
    void ensureAuthenticatedCollaboratorCompanyReturnsCorrectCompany() {
        // Arrange
        stubAuthenticatedAtcc(tapAir);
        controller = new ListFleetController(authz, aircraftRepository, aircraftModelRepository, collaboratorRepository);

        // Act
        final AirTransportCompany result = controller.authenticatedCollaboratorCompany();

        // Assert
        assertNotNull(result);
        assertEquals(tapAir, result);
    }

    @Test
    void ensureFleetByModelReturnsEmptyWhenNoMatchingModels() {
        // Arrange
        final Aircraft aircraft = createAircraft(a320Model, "CS-TAP1", tapAir, 150);
        aircraftRepository.save(aircraft);

        stubAuthenticatedAtcc(tapAir);
        controller = new ListFleetController(authz, aircraftRepository, aircraftModelRepository, collaboratorRepository);

        // Act
        final Iterable<Aircraft> result = controller.fleetByModel(b787Model);

        // Assert
        assertNotNull(result);
        assertEquals(0, ((List<Aircraft>) result).size());
    }

    @Test
    void ensureFleetByMakerReturnsEmptyWhenNoMatchingMaker() {
        // Arrange
        final Aircraft aircraft = createAircraft(a320Model, "CS-TAP1", tapAir, 150);
        aircraftRepository.save(aircraft);

        final Maker bombardierMaker = createMaker("Bombardier", "CA");

        stubAuthenticatedAtcc(tapAir);
        controller = new ListFleetController(authz, aircraftRepository, aircraftModelRepository, collaboratorRepository);

        // Act
        final Iterable<Aircraft> result = controller.fleetByMaker(bombardierMaker);

        // Assert
        assertNotNull(result);
        assertEquals(0, ((List<Aircraft>) result).size());
    }

    @Test
    void ensureFleetByCapacityReturnsEmptyWhenNoMatchingCapacity() {
        // Arrange
        final Aircraft aircraft = createAircraft(a320Model, "CS-TAP1", tapAir, 150);
        aircraftRepository.save(aircraft);

        stubAuthenticatedAtcc(tapAir);
        controller = new ListFleetController(authz, aircraftRepository, aircraftModelRepository, collaboratorRepository);

        // Act
        final Iterable<Aircraft> result = controller.fleetByCapacity(300);

        // Assert
        assertNotNull(result);
        assertEquals(0, ((List<Aircraft>) result).size());
    }
}

















