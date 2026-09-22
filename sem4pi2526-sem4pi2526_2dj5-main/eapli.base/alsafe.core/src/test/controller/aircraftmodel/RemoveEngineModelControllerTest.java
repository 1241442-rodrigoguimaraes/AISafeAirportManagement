package controller.aircraftmodel;

import eapli.alsafe.aircraft.domain.*;
import eapli.alsafe.aircraft.repositories.AircraftRepository;
import eapli.alsafe.aircraftModelMagnement.application.RemoveEngineModelController;
import eapli.alsafe.aircraftModelMagnement.domain.*;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.engineModelMagnement.Domain.*;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.domain.MakerCountry;
import eapli.alsafe.aircraftModelMagnement.domain.MakerName;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RemoveEngineModelControllerTest {

    private static class FakeAuthorizationService extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {
        }
    }

    private static class FakeAircraftModelRepository implements aircraftModelRepository {
        private final List<aircraftModel> store = new ArrayList<>();

        @Override
        public aircraftModel save(final aircraftModel aircraftModel) {
            if (!store.contains(aircraftModel)) {
                store.add(aircraftModel);
            }
            return aircraftModel;
        }

        @Override
        public Optional<aircraftModel> ofIdentity(final aircraftModelId id) {
            return store.stream()
                    .filter(a -> a.identity().equals(id))
                    .findFirst();
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
        public void deleteOfIdentity(final aircraftModelId id) {
            store.removeIf(a -> a.identity().equals(id));
        }

        @Override
        public long count() {
            return store.size();
        }

        @Override
        public boolean containsOfIdentity(final aircraftModelId id) {
            return ofIdentity(id).isPresent();
        }
    }

    private static class FakeAircraftRepository implements AircraftRepository {

        private boolean existsByAircraftModel = false;

        public void setExistsByAircraftModel(final boolean existsByAircraftModel) {
            this.existsByAircraftModel = existsByAircraftModel;
        }

        @Override
        public boolean existsByAircraftModel(final aircraftModel model) {
            return existsByAircraftModel;
        }

        @Override
        public boolean existsByRegistrationID(final RegistrationID registrationID) {
            return false;
        }

        @Override
        public boolean addAircraft(final aircraftModel model,
                                   final RegistrationID registrationID,
                                   final AircraftCountry country,
                                   final CabinConfiguration cabinConfig,
                                   final List<CrewElement> crew,
                                   final AirTransportCompany company) {
            return false;
        }

        @Override
        public Iterable<Aircraft> findByCompany(final AirTransportCompany company) {
            return List.of();
        }

        @Override
        public Iterable<Aircraft> findByCompanyAndModel(final AirTransportCompany company,
                                                        final aircraftModel model) {
            return List.of();
        }

        @Override
        public Aircraft save(final Aircraft entity) {
            return entity;
        }

        @Override
        public Optional<Aircraft> ofIdentity(final RegistrationID id) {
            return Optional.empty();
        }

        @Override
        public Iterable<Aircraft> findAll() {
            return List.of();
        }

        @Override
        public void delete(final Aircraft entity) {
        }

        @Override
        public void deleteOfIdentity(final RegistrationID id) {
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public boolean containsOfIdentity(final RegistrationID id) {
            return false;
        }
    }

    private FakeAircraftModelRepository repository;
    private FakeAircraftRepository fleetRepository;
    private RemoveEngineModelController controller;
    private Maker maker;
    private engineModel engine1;
    private engineModel engine2;

    @BeforeEach
    void setUp() {
        repository = new FakeAircraftModelRepository();
        fleetRepository = new FakeAircraftRepository();

        controller = new RemoveEngineModelController(
                new FakeAuthorizationService(),
                repository,
                fleetRepository
        );

        maker = new Maker(new MakerName("Airbus"), new MakerCountry("FR"));

        engine1 = sampleEngine("CFM56");
        engine2 = sampleEngine("LEAP-1A");
    }

    private engineModel sampleEngine(final String name) {
        return new engineModel(
                new engineModelName(name),
                maker,
                engineType.TURBOFAN,
                new engineModelPower(100000),
                engineModelFuel.JET_A1,
                new engineModelEfficiency(0.85)
        );
    }

    private aircraftModel sampleAircraftModelWithEngines(final Set<engineModel> engines) {
        return new aircraftModel(
                new aircraftModelName("A320"),
                maker,
                aircraftModelType.PASSENGER,
                engineType.TURBOFAN,
                new maximumRange(6150),
                new aircraftModelPhysicsData(
                        42600,
                        78000,
                        62500,
                        24210,
                        12100,
                        840,
                        122.6,
                        0.024,
                        0.50
                ),
                engines
        );
    }

    @Test
    void ensureAircraftModelsReturnsAllAircraftModels() {
        // Arrange
        aircraftModel aircraftModel = sampleAircraftModelWithEngines(Set.of(engine1));
        repository.save(aircraftModel);

        // Act
        Iterable<aircraftModel> result = controller.aircraftModels();

        // Assert
        assertNotNull(result);
        assertEquals(1, repository.count());
    }

    @Test
    void ensureRemoveEngineModelSucceedsWhenMoreThanOneEngineExists() {
        // Arrange
        Set<engineModel> engines = new HashSet<>();
        engines.add(engine1);
        engines.add(engine2);

        aircraftModel aircraftModel = sampleAircraftModelWithEngines(engines);
        repository.save(aircraftModel);

        // Act
        controller.removeEngineModel(aircraftModel, engine1);

        // Assert
        assertEquals(1, aircraftModel.certifiedEngines().size());
        assertFalse(aircraftModel.certifiedEngines().contains(engine1));
        assertTrue(aircraftModel.certifiedEngines().contains(engine2));
        assertEquals(1, repository.count());
    }

    @Test
    void ensureRemoveLastEngineModelIsRejected() {
        // Arrange
        Set<engineModel> engines = new HashSet<>();
        engines.add(engine1);

        aircraftModel aircraftModel = sampleAircraftModelWithEngines(engines);
        repository.save(aircraftModel);

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                controller.removeEngineModel(aircraftModel, engine1)
        );
    }

    @Test
    void ensureRemoveEngineModelIsRejectedWhenAircraftUseModel() {
        // Arrange
        Set<engineModel> engines = new HashSet<>();
        engines.add(engine1);
        engines.add(engine2);

        aircraftModel aircraftModel = sampleAircraftModelWithEngines(engines);
        repository.save(aircraftModel);

        fleetRepository.setExistsByAircraftModel(true);

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                controller.removeEngineModel(aircraftModel, engine1)
        );
    }
}