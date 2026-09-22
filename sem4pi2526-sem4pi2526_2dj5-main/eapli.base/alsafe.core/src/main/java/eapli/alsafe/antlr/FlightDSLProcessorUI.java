package eapli.alsafe.antlr;

import eapli.alsafe.antlr.flightplan.application.ImportFlightPlanFromFileController;
import eapli.alsafe.antlr.flightplan.domain.FlightPlanId;
import eapli.alsafe.antlr.flightplan.domain.ImportedFlightPlan;
import eapli.alsafe.antlr.flightplan.repositories.ImportedFlightPlanRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Command-line UI for importing flight plans from .fp files (US081).
 *
 * <p>Delegates parsing, validation and persistence to {@link ImportFlightPlanFromFileController}.
 * JSON output is written to the {@code flightPlanJson} folder under the working directory when processing succeeds.
 *
 * <p>Usage:
 * <pre>
 *   java -cp ... eapli.alsafe.antlr.FlightDSLProcessorUI
 * </pre>
 */
public class FlightDSLProcessorUI {

    private static final Path OUTPUT_DIR = Paths.get("flightPlanJson");

    private static final String SEPARATOR =
            "─────────────────────────────────────────────────────────";

    private final ImportFlightPlanFromFileController controller;

    public FlightDSLProcessorUI() {
        this(new ImportFlightPlanFromFileController(
                new InMemoryImportedFlightPlanStore(), new StandaloneAuthz()));
    }

    FlightDSLProcessorUI(final ImportFlightPlanFromFileController controller) {
        this.controller = controller;
    }

    public static void main(final String[] args) {
        new FlightDSLProcessorUI().run();
    }

    private void run() {
        final Scanner scanner = new Scanner(System.in);

        printBanner();

        final Path outputDir = ensureDirectoryExists(OUTPUT_DIR);
        System.out.println("  Output directory: " + outputDir.toAbsolutePath());
        System.out.println();

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("> ");
            final String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> processFileInteractive(scanner, outputDir);
                case "2" -> processDirectoryInteractive(scanner, outputDir);
                case "0" -> running = false;
                default -> System.out.println("  [!] Invalid option. Try again.");
            }
        }

        System.out.println();
        System.out.println("  Bye!");
        scanner.close();
    }

    private void processFileInteractive(final Scanner scanner, final Path outputDir) {
        System.out.println("  Enter path to .fp file:");
        System.out.print("  > ");
        final String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return;
        }

        final BatchSummary summary = processFiles(List.of(Paths.get(input)), outputDir);
        printBatchSummary(summary);
    }

    private void processDirectoryInteractive(final Scanner scanner, final Path outputDir) {
        System.out.println("  Enter directory path containing .fp files:");
        System.out.print("  > ");
        final String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return;
        }

        final Path dir = Paths.get(input);
        if (!Files.isDirectory(dir)) {
            System.err.println("  [ERROR] Not a directory: " + dir);
            return;
        }

        final List<Path> fpFiles = collectFpFiles(dir);
        if (fpFiles.isEmpty()) {
            System.out.println("  [!] No .fp files found in: " + dir);
            return;
        }

        System.out.println("  Found " + fpFiles.size() + " .fp file(s). Processing...");
        final BatchSummary summary = processFiles(fpFiles, outputDir);
        printBatchSummary(summary);
    }

    private BatchSummary processFiles(final List<Path> fpFiles, final Path outputDir) {
        int ok = 0;
        int failed = 0;

        for (final Path fp : fpFiles) {
            System.out.println();
            System.out.println(SEPARATOR);
            System.out.println("  File : " + fp.toAbsolutePath());

            final Path jsonOut = outputDir.resolve(baseNameOf(fp) + ".json");

            try {
                final FlightDSLProcessor.Result result = controller.importFromFile(fp, jsonOut);
                printImportOutcome(result);
                if (result.isValid()) {
                    ok++;
                } else {
                    failed++;
                }
            } catch (final IllegalArgumentException e) {
                System.err.println("  [FAIL] Invalid file: " + e.getMessage());
                failed++;
            } catch (final IOException e) {
                System.err.println("  [FAIL] Could not read file: " + e.getMessage());
                failed++;
            }
        }

        System.out.println(SEPARATOR);
        return new BatchSummary(ok, failed);
    }

    private void printImportOutcome(final FlightDSLProcessor.Result result) {
        if (!result.isSyntaxOk()) {
            System.err.println("  [FAIL] Flight plan rejected: lexical or syntactic errors were found.");
            System.err.println("         Please fix the file and try again.");
            return;
        }

        if (!result.isValid()) {
            System.err.println("  [FAIL] Flight plan rejected: semantic validation failed.");
            result.getErrors().forEach(e -> System.err.println("         " + e));
            return;
        }

        System.out.printf("  [OK]   %d flight plan(s) imported.%n", result.getFlightPlans().size());
        result.getFlightPlans().forEach(p -> System.out.println("         " + p));
        if (result.getJsonOutputPath() != null) {
            System.out.println("         JSON -> " + result.getJsonOutputPath());
        }
    }

    private Path ensureDirectoryExists(final Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
                System.out.println("  [INFO] Created output directory: " + dir.toAbsolutePath());
            } catch (final IOException e) {
                throw new RuntimeException(
                        "Cannot create output directory '" + dir + "': " + e.getMessage(), e);
            }
        } else if (!Files.isDirectory(dir)) {
            throw new RuntimeException("Path exists but is not a directory: " + dir);
        }
        return dir;
    }

    private List<Path> collectFpFiles(final Path dir) {
        final List<Path> result = new ArrayList<>();
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                    if (file.getFileName().toString().endsWith(".fp")) {
                        result.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (final IOException e) {
            System.err.println("[WARN] Error scanning directory '" + dir + "': " + e.getMessage());
        }
        return result;
    }

    private void printBanner() {
        System.out.println();
        System.out.println("+=======================================================+");
        System.out.println("|     Flight Plan Import  --  .fp validate & persist    |");
        System.out.println("+=======================================================+");
        System.out.println();
    }

    private void printMenu() {
        System.out.println();
        System.out.println("  1) Import a single .fp file");
        System.out.println("  2) Import all .fp files in a directory");
        System.out.println("  0) Exit");
        System.out.println();
    }

    private void printBatchSummary(final BatchSummary summary) {
        System.out.println();
        System.out.println("  +-----------------------------+");
        System.out.printf("  |  Processed : %3d file(s)    |%n", summary.ok + summary.failed);
        System.out.printf("  |  Imported  : %3d            |%n", summary.ok);
        System.out.printf("  |  Failed    : %3d            |%n", summary.failed);
        System.out.println("  +-----------------------------+");
    }

    private static String baseNameOf(final Path file) {
        final String name = file.getFileName().toString();
        final int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private record BatchSummary(int ok, int failed) {}

    /**
     * In-memory store for standalone runs of this UI (no full application bootstrap).
     */
    private static final class InMemoryImportedFlightPlanStore implements ImportedFlightPlanRepository {
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

    /** Skips role checks when running this UI outside the authenticated console app. */
    private static final class StandaloneAuthz extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {}
    }
}
