package controller.flightplan;

import eapli.alsafe.antlr.FlightDSLProcessor;
import eapli.alsafe.antlr.flightplan.application.ImportFlightPlanFromFileController;
import eapli.alsafe.antlr.flightplan.domain.FlightPlanId;
import eapli.alsafe.antlr.flightplan.domain.ImportedFlightPlan;
import eapli.alsafe.antlr.flightplan.repositories.ImportedFlightPlanRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ImportFlightPlanDSLFromFileControllerTest {

    private static final String BASE = "src/test/resources/dsl/";

    private ImportedFlightPlanRepository repository;
    private ImportFlightPlanFromFileController controller;

    private static class FakeImportedFlightPlanRepository implements ImportedFlightPlanRepository {
        private final List<ImportedFlightPlan> plans = new ArrayList<>();

        @Override
        public <S extends ImportedFlightPlan> S save(final S entity) {
            plans.add(entity);
            return entity;
        }

        @Override
        public Iterable<ImportedFlightPlan> findAll() {
            return plans;
        }

        @Override
        public Optional<ImportedFlightPlan> ofIdentity(final FlightPlanId id) {
            return plans.stream().filter(p -> p.identity().equals(id)).findFirst();
        }

        @Override
        public void delete(final ImportedFlightPlan entity) {
            plans.remove(entity);
        }

        @Override
        public void deleteOfIdentity(final FlightPlanId id) {
            plans.removeIf(p -> p.identity().equals(id));
        }

        @Override
        public long count() {
            return plans.size();
        }
    }

    private static class PermissiveAuthz extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {}
    }

    @BeforeEach
    void setUp() {
        repository = new FakeImportedFlightPlanRepository();
        controller = new ImportFlightPlanFromFileController(repository, new PermissiveAuthz());
    }

    @Test
    void nullPath_shouldThrowIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.importFromFile(null));
    }

    @Test
    void nonExistentFile_shouldThrowIllegalArgument() {
        final Path missing = Path.of("does_not_exist.fp");
        assertThrows(IllegalArgumentException.class,
                () -> controller.importFromFile(missing));
    }

    @Test
    void wrongExtension_shouldThrowIllegalArgument() throws Exception {
        final Path tmp = Files.createTempFile("plan", ".txt");
        try {
            assertThrows(IllegalArgumentException.class,
                    () -> controller.importFromFile(tmp));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void validFile_shouldReturnValidResultAndPersistPlans() throws Exception {
        final Path file = Path.of(BASE + "valid_basic.fp");

        final FlightDSLProcessor.Result result = controller.importFromFile(file);

        assertTrue(result.isValid());
        assertEquals(1, result.getFlightPlans().size());
        assertEquals(1, repository.count());
    }

    @Test
    void missingType_shouldReturnSyntaxErrorAndNotPersist() throws Exception {
        final Path file = Path.of(BASE + "invalid_missing_type.fp");

        final FlightDSLProcessor.Result result = controller.importFromFile(file);

        assertFalse(result.isSyntaxOk());
        assertFalse(result.isValid());
        assertEquals(0, repository.count());
    }

    @Test
    void badDesignator_shouldReturnSyntaxErrorAndNotPersist() throws Exception {
        final Path file = Path.of(BASE + "invalid_bad_designator.fp");

        final FlightDSLProcessor.Result result = controller.importFromFile(file);

        assertFalse(result.isSyntaxOk());
        assertEquals(0, repository.count());
    }

    @Test
    void badIata_shouldReturnSyntaxErrorAndNotPersist() throws Exception {
        final Path file = Path.of(BASE + "invalid_bad_iata.fp");

        final FlightDSLProcessor.Result result = controller.importFromFile(file);

        assertFalse(result.isSyntaxOk());
        assertEquals(0, repository.count());
    }

    @Test
    void duplicateFlightId_shouldReportSemanticErrorAndNotPersist() throws Exception {
        final Path file = Path.of(BASE + "invalid_duplicate_flight_id.fp");

        final FlightDSLProcessor.Result result = controller.importFromFile(file);

        assertTrue(result.isSyntaxOk());
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getMessage().contains("Duplicate flight identifier")));
        assertEquals(0, repository.count());
    }

    @Test
    void zeroFuel_shouldReportSemanticErrorAndNotPersist() throws Exception {
        final Path file = Path.of(BASE + "invalid_zero_fuel.fp");

        final FlightDSLProcessor.Result result = controller.importFromFile(file);

        assertTrue(result.isSyntaxOk());
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getMessage().contains("strictly positive")));
        assertEquals(0, repository.count());
    }

    @Test
    void sameCoords_shouldReportSemanticErrorAndNotPersist() throws Exception {
        final Path file = Path.of(BASE + "invalid_same_coords.fp");

        final FlightDSLProcessor.Result result = controller.importFromFile(file);

        assertTrue(result.isSyntaxOk());
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getMessage().contains("start and end coordinates are identical")));
        assertEquals(0, repository.count());
    }
}
