package controller.enginemodel;

import eapli.alsafe.engineModelMagnement.Application.CreateEngineModelController;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CreateEngineModelControllerTest {

    private static class FakeAuthorizationService extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {}
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
            return store.stream()
                    .filter(e -> e.identity().equals(id))
                    .findFirst();
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

    private static class FakeMakersRepository implements MakersRepository {
        private final List<Maker> store = new ArrayList<>();

        @Override
        public Maker save(final Maker maker) {
            store.add(maker);
            return maker;
        }

        @Override
        public Optional<Maker> ofIdentity(final MakerName id) {
            return store.stream()
                    .filter(m -> m.identity().equals(id))
                    .findFirst();
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

    private FakeEngineModelRepository engineRepository;
    private FakeMakersRepository makerRepository;
    private CreateEngineModelController controller;
    private Maker maker;

    @BeforeEach
    void setUp() {
        engineRepository = new FakeEngineModelRepository();
        makerRepository = new FakeMakersRepository();

        maker = new Maker(new MakerName("CFM International"), new MakerCountry("FR"));
        makerRepository.save(maker);

        controller = new CreateEngineModelController(
                new FakeAuthorizationService(),
                engineRepository,
                makerRepository
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
    void ensureControllerCannotBeCreatedWithNullBuilder() {
        assertThrows(IllegalArgumentException.class, () ->
                new CreateEngineModelController(new FakeAuthorizationService(), null, engineRepository, makerRepository)
        );
    }

    @Test
    void ensureRegisterEngineModelSavesAndReturnsEngineModel() {
        // Act
        engineModel result = controller.registerEngineModel(
                "LEAP-1A",
                maker,
                engineType.TURBOFAN,
                150000,
                engineModelFuel.JET_A1,
                0.89
        );

        // Assert
        assertNotNull(result);
        assertEquals(new engineModelName("LEAP-1A"), result.name());
        assertEquals(maker, result.maker());
        assertEquals(1, engineRepository.count());
    }

    @Test
    void ensureDuplicateEngineModelIsRejected() {
        // Arrange
        controller.registerEngineModel(
                "LEAP-1A",
                maker,
                engineType.TURBOFAN,
                150000,
                engineModelFuel.JET_A1,
                0.89
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                controller.registerEngineModel(
                        "LEAP-1A",
                        maker,
                        engineType.TURBOFAN,
                        160000,
                        engineModelFuel.JET_A1,
                        0.91
                )
        );
    }

    @Test
    void ensureRepositoryCanFilterByMotorization() {
        // Arrange
        controller.registerEngineModel(
                "LEAP-1A",
                maker,
                engineType.TURBOFAN,
                150000,
                engineModelFuel.JET_A1,
                0.89
        );

        // Act
        Iterable<engineModel> result = engineRepository.findByMotorization(engineType.TURBOFAN);

        // Assert
        assertTrue(result.iterator().hasNext());
    }
}
