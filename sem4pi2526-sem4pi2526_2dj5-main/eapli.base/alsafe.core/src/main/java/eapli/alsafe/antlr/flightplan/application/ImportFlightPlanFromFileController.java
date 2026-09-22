package eapli.alsafe.antlr.flightplan.application;

import eapli.alsafe.antlr.FlightDSLProcessor;
import eapli.alsafe.antlr.flightplan.repositories.ImportedFlightPlanRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@UseCaseController
public class ImportFlightPlanFromFileController {

    private final ImportedFlightPlanRepository importedFlightPlanRepository;
    private final AuthorizationService authz;

    public ImportFlightPlanFromFileController() {
        this(PersistenceContext.repositories().importedFlightPlans(), AuthzRegistry.authorizationService());
    }

    public ImportFlightPlanFromFileController(final ImportedFlightPlanRepository importedFlightPlanRepository) {
        this(importedFlightPlanRepository, AuthzRegistry.authorizationService());
    }

    public ImportFlightPlanFromFileController(final ImportedFlightPlanRepository importedFlightPlanRepository,
                                              final AuthorizationService authz) {
        this.importedFlightPlanRepository = importedFlightPlanRepository;
        this.authz = authz;
    }

    /**
     * Processes a .fp file and, if valid, persists the flight plans.
     *
     * @param filePath path to the .fp file
     * @return the full processing result (caller decides how to display errors/success)
     * @throws IOException if the file cannot be read
     */
    public FlightDSLProcessor.Result importFromFile(final Path filePath) throws IOException {
        return importFromFile(filePath, null);
    }

    /**
     * Processes a .fp file and, if valid, persists the flight plans.
     *
     * @param filePath      path to the .fp file
     * @param jsonOutputPath optional JSON export path; when null, JSON is written next to the .fp file
     */
    public FlightDSLProcessor.Result importFromFile(final Path filePath, final Path jsonOutputPath)
            throws IOException {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.PILOT);

        validateFilePath(filePath);

        final FlightDSLProcessor processor = new FlightDSLProcessor();
        final FlightDSLProcessor.Result result = jsonOutputPath == null
                ? processor.process(filePath)
                : processor.process(filePath, jsonOutputPath);

        if (result.isValid()) {
            result.getFlightPlans().stream()
                    .map(FlightPlanMapper::toEntity)
                    .forEach(importedFlightPlanRepository::save);
        }

        return result;
    }

    private void validateFilePath(final Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("File path must not be null.");
        }
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }
        if (!Files.isRegularFile(filePath)) {
            throw new IllegalArgumentException("Path is not a regular file: " + filePath);
        }
        if (!Files.isReadable(filePath)) {
            throw new IllegalArgumentException("File is not readable: " + filePath);
        }
        if (!filePath.getFileName().toString().toLowerCase().endsWith(".fp")) {
            throw new IllegalArgumentException(
                    "Invalid file format. Expected a .fp file, got: " + filePath.getFileName());
        }
    }
}
