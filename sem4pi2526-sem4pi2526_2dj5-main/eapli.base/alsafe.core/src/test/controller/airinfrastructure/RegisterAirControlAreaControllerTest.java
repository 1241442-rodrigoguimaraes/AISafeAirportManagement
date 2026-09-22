package controller.airinfrastructure;

import eapli.alsafe.airinfrastructure.application.RegisterAirControlAreaController;
import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaBuilder;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class RegisterAirControlAreaControllerTest {

    private static class FakeAuthorizationService extends AuthorizationService {

        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... actions) {}
    }

    private static class FakeAirControlAreaRepository implements AirControlAreaRepository {
        private final List<AirControlArea> store = new ArrayList<>();

        @Override
        public Iterable<AirControlArea> findAll() {
            return store;
        }

        @Override
        public Optional<AirControlArea> findById(AirControlAreaID id) {
            return store.stream().filter(a -> a.getId().equals(id)).findFirst();
        }

        @Override
        public AirControlArea save(final AirControlArea o) {
            store.add(o);
            return o;
        }

        @Override
        public Optional<AirControlArea> ofIdentity(final AirControlAreaID id) {
            return Optional.empty();
        }

        @Override
        public void delete(final AirControlArea o) {}

        @Override
        public void deleteOfIdentity(final AirControlAreaID id) {}

        @Override
        public long count() {
            return store.size();
        }

        @Override
        public Optional<AirControlArea> findByCode(String code) {
            return store.stream().filter(a -> a.identity().toString().equals(code)).findFirst();
        }
    }

    private FakeAirControlAreaRepository repository;
    private AirControlAreaBuilder builder;
    private RegisterAirControlAreaController controller;

    private static final String NAME = "Portugal";
    private static final Double MINIMUM_FUEL = 150.0;

    @BeforeEach
    void setUp() {
        repository = new FakeAirControlAreaRepository();
        builder = new AirControlAreaBuilder();
        controller = new RegisterAirControlAreaController(new FakeAuthorizationService(), builder, repository);
    }

    @Test
    void ensureControllerCannotBeCreatedWithNullAuthz() {
        assertThrows(IllegalArgumentException.class, () ->
                new RegisterAirControlAreaController(null, builder, repository)
        );
    }

    @Test
    void ensureControllerCannotBeCreatedWithNullBuilder() {
        assertThrows(IllegalArgumentException.class, () ->
                new RegisterAirControlAreaController(new FakeAuthorizationService(), null, repository)
        );
    }

    @Test
    void ensureControllerCannotBeCreatedWithNullRepository() {
        assertThrows(IllegalArgumentException.class, () ->
                new RegisterAirControlAreaController(new FakeAuthorizationService(), builder, null)
        );
    }

    @Test
    void ensureCreateCoordinateAddsCoordinateToBuilder() {
        controller.createCoordinate(42.15, -8.85);
        controller.createCoordinate(36.95, -7.90);

        assertDoesNotThrow(() -> controller.registerAirControlArea(NAME, MINIMUM_FUEL));
    }

    @Test
    void ensureRegisterAirControlAreaSavesAndReturnsArea() {
        controller.createCoordinate(42.15, -8.85);
        controller.createCoordinate(36.95, -7.90);

        AirControlArea result = controller.registerAirControlArea(NAME, MINIMUM_FUEL);

        assertNotNull(result);
        assertEquals(NAME, result.getName());
        assertEquals(MINIMUM_FUEL, result.getMinimumFuel().getMinimumFuel());
        assertEquals(1, repository.store.size());
    }

    @Test
    void ensureSequenceIsIncrementedBasedOnRepositoryCount() {
        double[][] areaCoords = {
                {42.15, -8.85, 41.00, -10.00},
                {42.15, -6.00, 41.00,  -7.50},
                {42.15, -3.00, 41.00,  -4.50},
                {42.15,  0.00, 41.00,  -1.50}
        };

        for (int i = 0; i < 4; i++) {
            builder = new AirControlAreaBuilder();
            controller = new RegisterAirControlAreaController(new FakeAuthorizationService(), builder, repository);
            controller.createCoordinate(areaCoords[i][0], areaCoords[i][1]);
            controller.createCoordinate(areaCoords[i][2], areaCoords[i][3]);
            controller.registerAirControlArea(NAME + (i + 1), MINIMUM_FUEL);
        }

        builder = new AirControlAreaBuilder();
        controller = new RegisterAirControlAreaController(new FakeAuthorizationService(), builder, repository);
        controller.createCoordinate(42.15, 2.00);
        controller.createCoordinate(41.00, 0.50);

        AirControlArea result = controller.registerAirControlArea("Portugal5", MINIMUM_FUEL);

        assertEquals("ACA-0005", result.identity().getId());
    }

    @Test
    void ensureOverlappingAreaIsRejected() {
        // regista a primeira área
        controller.createCoordinate(42.15, -9.50);
        controller.createCoordinate(36.95, -7.50);
        controller.registerAirControlArea(NAME, MINIMUM_FUEL);

        // tenta registar uma área que se sobrepõe
        builder = new AirControlAreaBuilder();
        controller = new RegisterAirControlAreaController(new FakeAuthorizationService(), builder, repository);
        controller.createCoordinate(41.00, -9.00); // dentro dos limites da primeira
        controller.createCoordinate(37.50, -8.00);

        assertThrows(IllegalArgumentException.class, () ->
                controller.registerAirControlArea("Overlap Area", MINIMUM_FUEL)
        );
    }

    @Test
    void ensureNonOverlappingAreaIsAccepted() {
        controller.createCoordinate(42.15, -9.50);
        controller.createCoordinate(36.95, -7.50);
        controller.registerAirControlArea(NAME, MINIMUM_FUEL);

        builder = new AirControlAreaBuilder();
        controller = new RegisterAirControlAreaController(new FakeAuthorizationService(), builder, repository);
        controller.createCoordinate(42.15, -7.49);
        controller.createCoordinate(36.95, -5.00);

        assertDoesNotThrow(() -> controller.registerAirControlArea("Adjacent Area", MINIMUM_FUEL));
    }
}
