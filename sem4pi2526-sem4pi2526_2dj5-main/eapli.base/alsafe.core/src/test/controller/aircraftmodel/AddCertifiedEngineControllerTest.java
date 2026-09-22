package controller.aircraftmodel;

import eapli.alsafe.aircraftModelMagnement.application.AddCertifiedEngineController;
import eapli.alsafe.aircraftModelMagnement.domain.*;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
import eapli.alsafe.engineModelMagnement.Domain.*;
import eapli.alsafe.engineModelMagnement.Repositories.engineModelRepository;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.domain.MakerCountry;
import eapli.alsafe.aircraftModelMagnement.domain.MakerName;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AddCertifiedEngineControllerTest {

    private static class FakeAuthorizationService extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... actions) {}
    }

    private static class FakeAircraftModelRepository implements aircraftModelRepository {
        public final List<aircraftModel> store = new ArrayList<>();

        @Override
        public Iterable<aircraftModel> findAll() { return store; }

        @Override
        public Optional<aircraftModel> findByName(final String name) {
            return store.stream().filter(a -> a.name().name().equals(name)).findFirst();
        }

        @Override
        public aircraftModel save(aircraftModel entity) {
            store.removeIf(a -> a.identity().equals(entity.identity()));
            store.add(entity);
            return entity;
        }

        @Override
        public Optional<aircraftModel> ofIdentity(final aircraftModelId id) {
            return store.stream().filter(a -> a.identity().equals(id)).findFirst();
        }

        @Override
        public void delete(final aircraftModel o) { store.remove(o); }

        @Override
        public void deleteOfIdentity(final aircraftModelId id) {}

        @Override
        public long count() { return store.size(); }
    }

    private static class FakeEngineModelRepository implements engineModelRepository {
        public final List<engineModel> store = new ArrayList<>();

        @Override
        public Iterable<engineModel> findAll() { return store; }

        @Override
        public Iterable<engineModel> findByMotorization(final engineType motorization) {
            return store.stream().filter(e -> e.getType() == motorization).toList();
        }

        @Override
        public engineModel save(engineModel entity) {
            store.add(entity);
            return entity;
        }

        @Override
        public Optional<engineModel> ofIdentity(final engineModelId id) { return Optional.empty(); }

        @Override
        public void delete(final engineModel o) {}

        @Override
        public void deleteOfIdentity(final engineModelId id) {}

        @Override
        public long count() { return store.size(); }
    }

    private AddCertifiedEngineController controller;
    private FakeAircraftModelRepository amRepository;
    private FakeEngineModelRepository emRepository;

    private aircraftModel testAircraftModel;
    private engineModel compatibleEngineModel;
    private engineModel incompatibleEngineModel;

    @BeforeEach
    void setUp() {
        amRepository = new FakeAircraftModelRepository();
        emRepository = new FakeEngineModelRepository();
        controller = new AddCertifiedEngineController(new FakeAuthorizationService(), amRepository, emRepository);

        engineModelName modelName1 = new engineModelName("model1");
        engineModelName modelName2 = new engineModelName("model2");
        aircraftModelName amName = new aircraftModelName("amModel1");

        Maker maker = new Maker(new MakerName("Dummy"), new MakerCountry("PT"));

        engineModelPower power = new engineModelPower(200.0);

        engineModelEfficiency efficiency = new engineModelEfficiency(300.0);

        maximumRange maxRange = new maximumRange(300.0);

        aircraftModelPhysicsData physicsData = new aircraftModelPhysicsData(42600.0, 78000.0, 62500.0, 24000.0, 12000.0, 850.0, 122.6, 0.02, 0.5);

        compatibleEngineModel = new engineModel(modelName1, maker, engineType.TURBOPROP, power, engineModelFuel.JET_A1, efficiency);
        incompatibleEngineModel = new engineModel(modelName2, maker, engineType.ELECTRIC_PROPELLER, power, engineModelFuel.JET_A1, efficiency);
        testAircraftModel =  new aircraftModel(amName, maker, aircraftModelType.MIXED, engineType.TURBOPROP, maxRange, physicsData, new HashSet<>());

        testAircraftModel.certifiedEngines().add(compatibleEngineModel);

        emRepository.store.add(compatibleEngineModel);
        emRepository.store.add(incompatibleEngineModel);
        amRepository.store.add(testAircraftModel);
    }

    @Test
    void ensureControllerCannotBeCreatedWithNullAuthz() {
        assertThrows(IllegalArgumentException.class, () ->
                new AddCertifiedEngineController(null, amRepository, emRepository)
        );
    }

    @Test
    void ensureControllerCannotBeCreatedWithNullAircraftModelRepository() {
        assertThrows(IllegalArgumentException.class, () ->
                new AddCertifiedEngineController(new FakeAuthorizationService(), null, emRepository)
        );
    }

    @Test
    void ensureControllerCannotBeCreatedWithNullEngineModelRepository() {
        assertThrows(IllegalArgumentException.class, () ->
                new AddCertifiedEngineController(new FakeAuthorizationService(), amRepository, null)
        );
    }

    @Test
    void ensureGetAllAircraftModelsReturnsAllModels() {
        Iterable<aircraftModel> result = controller.getAllAircraftModels();

        List<aircraftModel> list = new ArrayList<>();
        result.forEach(list::add);

        assertEquals(1, list.size());
        assertTrue(list.contains(testAircraftModel));
    }

    @Test
    void ensureGetCompatibleEngineModelsDoesNotReturnInsertedOrIncompatibleOnes() {
        Iterable<engineModel> result = controller.getCompatibleEngineModels(testAircraftModel);

        List<engineModel> list = new ArrayList<>();
        result.forEach(list::add);

        assertFalse(list.contains(compatibleEngineModel));
        assertFalse(list.contains(incompatibleEngineModel));
    }

    @Test
    void ensureGetCompatibleEngineModelsExcludesAlreadyCertifiedEngines() {
        Iterable<engineModel> result = controller.getCompatibleEngineModels(testAircraftModel);

        List<engineModel> list = new ArrayList<>();
        result.forEach(list::add);

        assertFalse(list.contains(compatibleEngineModel));
    }

    @Test
    void ensureAddCertifiedEngineAddsEngineToAircraft() {
        engineModel newEngineModel = new engineModel(new engineModelName("A"), new Maker(new MakerName("A"), new MakerCountry("PT")), engineType.TURBOPROP,
                new engineModelPower(200.0), engineModelFuel.AVGAS, new engineModelEfficiency(400.0));
        emRepository.store.add(newEngineModel);

        controller.addCertifiedEngine(testAircraftModel, newEngineModel);

        assertTrue(testAircraftModel.certifiedEngines().contains(newEngineModel));
    }

    @Test
    void ensureAddCertifiedEngineThrowsWhenEngineAlreadyCertified() {
        assertThrows(IntegrityViolationException.class, () ->
                controller.addCertifiedEngine(testAircraftModel, compatibleEngineModel)
        );
    }

    @Test
    void ensureAddCertifiedEngineThrowsWhenEngineTypeIsIncompatible() {
        assertThrows(IntegrityViolationException.class, () ->
                controller.addCertifiedEngine(testAircraftModel, incompatibleEngineModel)
        );
    }

    @Test
    void ensureAddCertifiedEngineThrowsWhenEngineIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.addCertifiedEngine(testAircraftModel, null)
        );
    }

    @Test
    void ensureAddCertifiedEngineSavesAircraftModel() {
        engineModel newEngineModel = new engineModel(new engineModelName("AB"), new Maker(new MakerName("A"), new MakerCountry("PT")), engineType.TURBOPROP,
                new engineModelPower(300.0), engineModelFuel.AVGAS, new engineModelEfficiency(500.0));
        emRepository.store.add(newEngineModel);

        controller.addCertifiedEngine(testAircraftModel, newEngineModel);

        assertTrue(amRepository.store.contains(testAircraftModel));
    }
}
