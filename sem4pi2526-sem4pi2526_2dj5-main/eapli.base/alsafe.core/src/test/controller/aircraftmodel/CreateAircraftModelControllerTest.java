package controller.aircraftmodel;

import eapli.alsafe.aircraftModelMagnement.application.CreateAircraftModelController;
import eapli.alsafe.aircraftModelMagnement.domain.*;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
import eapli.alsafe.engineModelMagnement.Domain.*;
import eapli.alsafe.engineModelMagnement.Repositories.engineModelRepository;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.domain.MakerCountry;
import eapli.alsafe.aircraftModelMagnement.domain.MakerName;
import eapli.alsafe.aircraftModelMagnement.repositories.MakersRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CreateAircraftModelControllerTest {

    private static class FakeAuthorizationService extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {}
    }

    private static class FakeAircraftModelRepository implements aircraftModelRepository {
        private final List<aircraftModel> store = new ArrayList<>();

        @Override
        public aircraftModel save(final aircraftModel aircraftModel) {
            store.add(aircraftModel);
            return aircraftModel;
        }

        @Override
        public Optional<aircraftModel> ofIdentity(final aircraftModelId id) {
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

    private static class FakeMakersRepository implements MakersRepository {
        private final List<Maker> store = new ArrayList<>();

        @Override
        public Maker save(final Maker maker) {
            store.add(maker);
            return maker;
        }

        @Override
        public Optional<Maker> ofIdentity(final MakerName id) {
            return store.stream().filter(m -> m.identity().equals(id)).findFirst();
        }

        @Override
        public Iterable<Maker> findAll() {
            return store;
        }

        @Override
        public void delete(final Maker maker) {
            store.remove(maker);
        }

        @Override
        public void deleteOfIdentity(final MakerName id) {
            store.removeIf(m -> m.identity().equals(id));
        }

        @Override
        public long count() {
            return store.size();
        }

        @Override
        public boolean containsOfIdentity(final MakerName id) {
            return ofIdentity(id).isPresent();
        }
    }

    private static class FakeEngineModelRepository implements engineModelRepository {
        private final List<engineModel> store = new ArrayList<>();

        @Override
        public engineModel save(final engineModel engine) {
            store.add(engine);
            return engine;
        }

        @Override
        public Optional<engineModel> ofIdentity(final engineModelId id) {
            return store.stream().filter(e -> e.identity().equals(id)).findFirst();
        }

        @Override
        public Iterable<engineModel> findAll() {
            return store;
        }

        @Override
        public Iterable<engineModel> findByMotorization(final engineType motorization) {
            return store.stream()
                    .filter(e -> e.getType().equals(motorization))
                    .toList();
        }

        @Override
        public void delete(final engineModel engine) {
            store.remove(engine);
        }

        @Override
        public void deleteOfIdentity(final engineModelId id) {
            store.removeIf(e -> e.identity().equals(id));
        }

        @Override
        public long count() {
            return store.size();
        }

        @Override
        public boolean containsOfIdentity(final engineModelId id) {
            return ofIdentity(id).isPresent();
        }
    }

    private FakeAircraftModelRepository aircraftRepository;
    private FakeMakersRepository makerRepository;
    private FakeEngineModelRepository engineRepository;
    private CreateAircraftModelController controller;
    private Maker maker;
    private engineModel engine;

    @BeforeEach
    void setUp() {
        aircraftRepository = new FakeAircraftModelRepository();
        makerRepository = new FakeMakersRepository();
        engineRepository = new FakeEngineModelRepository();

        maker = new Maker(new MakerName("Airbus"), new MakerCountry("FR"));

        engine = new engineModel(
                new engineModelName("CFM56"),
                maker,
                engineType.TURBOFAN,
                new engineModelPower(100000),
                engineModelFuel.JET_A1,
                new engineModelEfficiency(0.85)
        );

        makerRepository.save(maker);
        engineRepository.save(engine);

        controller = new CreateAircraftModelController(
                new FakeAuthorizationService(),
                aircraftRepository,
                makerRepository,
                engineRepository
        );
    }

    @Test
    void ensureMakersReturnsAvailableMakers() {
        // Act
        Iterable<Maker> result = controller.makers();

        // Assert
        assertNotNull(result);
        assertEquals(1, makerRepository.count());
    }

    @Test
    void ensureEngineTypesAreReturned() {
        // Act
        List<engineType> result = controller.getEngineTypes();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(engineType.TURBOFAN));
    }

    @Test
    void ensureEngineModelsReturnsEnginesByMotorization() {
        // Act
        Iterable<engineModel> result = controller.engineModels(engineType.TURBOFAN);

        // Assert
        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
    }

    @Test
    void ensureControllerCannotBeCreatedWithNullBuilder() {
        assertThrows(IllegalArgumentException.class, () ->
                new CreateAircraftModelController(new FakeAuthorizationService(), null, aircraftRepository, makerRepository, engineRepository)
        );
    }

    @Test
    void ensureRegisterAircraftModelSavesAndReturnsAircraftModel() {
        // Arrange
        Set<engineModel> engines = new HashSet<>();
        engines.add(engine);

        // Act
        aircraftModel result = controller.registerAircraftModel(
                "A320",
                maker,
                aircraftModelType.PASSENGER,
                engineType.TURBOFAN,
                6150,
                42600,
                78000,
                62500,
                24210,
                12100,
                840,
                122.6,
                0.024,
                0.50,
                engines
        );

        // Assert
        assertNotNull(result);
        assertEquals(new aircraftModelName("A320"), result.name());
        assertEquals(maker, result.maker());
        assertEquals(engineType.TURBOFAN, result.motorization());
        assertEquals(1, aircraftRepository.count());
    }

    @Test
    void ensureDuplicateAircraftModelIsRejected() {
        // Arrange
        Set<engineModel> engines = new HashSet<>();
        engines.add(engine);

        controller.registerAircraftModel(
                "A320",
                maker,
                aircraftModelType.PASSENGER,
                engineType.TURBOFAN,
                6150,
                42600,
                78000,
                62500,
                24210,
                12100,
                840,
                122.6,
                0.024,
                0.50,
                engines
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                controller.registerAircraftModel(
                        "A320",
                        maker,
                        aircraftModelType.PASSENGER,
                        engineType.TURBOFAN,
                        6200,
                        42600,
                        78000,
                        62500,
                        24210,
                        12100,
                        840,
                        122.6,
                        0.024,
                        0.50,
                        engines
                )
        );
    }

    @Test
    void ensureAircraftModelWithoutEnginesIsRejected() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                controller.registerAircraftModel(
                        "A321",
                        maker,
                        aircraftModelType.PASSENGER,
                        engineType.TURBOFAN,
                        6150,
                        42600,
                        78000,
                        62500,
                        24210,
                        12100,
                        840,
                        122.6,
                        0.024,
                        0.50,
                        new HashSet<>()
                )
        );
    }
}
