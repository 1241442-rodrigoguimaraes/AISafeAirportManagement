package domain.flightPlan;

import eapli.alsafe.flightPlan.infrastructure.FlightPlanTestRunner;
import eapli.alsafe.flightPlan.infrastructure.SCOMPFlightPlanTestRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class SCOMPFlightPlanTestRunnerTest {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    private Path scompDir;

    @BeforeEach
    void createScompDir() throws IOException {
        scompDir = Files.createTempDirectory("scomp-test");
    }

    @AfterEach
    void cleanupScompDir() {
        try (var paths = Files.walk(scompDir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    @Test
    void testResult_summaryReflectsSuccessAndFailure() {
        final var passed = new FlightPlanTestRunner.TestResult(true, 0, "ok");
        final var failed = new FlightPlanTestRunner.TestResult(false, 1, "violation");

        assertEquals("All safety checks passed.", passed.summary());
        assertTrue(failed.summary().contains("Exit code 1"));
        assertTrue(failed.summary().contains("violation"));
    }

    @Test
    void toWslPath_convertsWindowsPathToMntForm() {
        assumeTrue(IS_WINDOWS, "path conversion only meaningful on Windows");

        final String wsl = SCOMPFlightPlanTestRunner.toWslPath(Path.of("C:\\Users\\henri\\plan.fp"));

        assertEquals("/mnt/c/Users/henri/plan.fp", wsl);
    }

    @Test
    void toWslPath_lowercasesTheDriveLetter() {
        assumeTrue(IS_WINDOWS, "path conversion only meaningful on Windows");

        final String wsl = SCOMPFlightPlanTestRunner.toWslPath(Path.of("D:\\data\\x.fp"));

        assertEquals("/mnt/d/data/x.fp", wsl);
    }

    @Test
    void run_whenScompDirDoesNotExist_throwsIOException() {
        final var runner = new SCOMPFlightPlanTestRunner(
                scompDir.resolve("does-not-exist"), "flight_simulation");

        assertThrows(IOException.class, () -> runner.run("flight TP123 { }"));
    }

    @Test
    void run_whenBinaryExitsZero_reportsSuccessAndCapturesOutput() throws IOException {
        assumeTrue(shellAvailable(), "requires a POSIX shell (Linux or Windows+WSL)");
        fakeBinary("fake_sim", "#!/bin/sh\necho 'All flights completed'\nexit 0\n");
        final var runner = new SCOMPFlightPlanTestRunner(scompDir, "fake_sim");

        final var result = runner.run("flight TP123 { }");

        assertTrue(result.success());
        assertEquals(0, result.exitCode());
        assertTrue(result.message().contains("All flights completed"));
    }

    @Test
    void run_whenBinaryExitsNonZero_reportsFailureWithOutput() throws IOException {
        assumeTrue(shellAvailable(), "requires a POSIX shell (Linux or Windows+WSL)");
        fakeBinary("fake_sim", "#!/bin/sh\necho 'Safety violation in segment 1'\nexit 1\n");
        final var runner = new SCOMPFlightPlanTestRunner(scompDir, "fake_sim");

        final var result = runner.run("flight TP123 { }");

        assertFalse(result.success());
        assertEquals(1, result.exitCode());
        assertTrue(result.message().contains("Safety violation"));
    }

    @Test
    void run_writesTheDslContentToTheFpFilePassedToTheBinary() throws IOException {
        assumeTrue(shellAvailable(), "requires a POSIX shell (Linux or Windows+WSL)");
        fakeBinary("fake_sim", "#!/bin/sh\ncat \"$1\"\nexit 0\n");
        final var runner = new SCOMPFlightPlanTestRunner(scompDir, "fake_sim");
        final String dsl = "flight TP123 { type: regular }";

        final var result = runner.run(dsl);

        assertTrue(result.message().contains(dsl));
    }

    private void fakeBinary(String name, String script) throws IOException {
        final Path bin = scompDir.resolve(name);
        Files.writeString(bin, script, StandardCharsets.UTF_8);
        if (!IS_WINDOWS) {
            Files.setPosixFilePermissions(bin, PosixFilePermissions.fromString("rwxr-xr-x"));
        }
    }

    private static boolean shellAvailable() {
        if (!IS_WINDOWS) {
            return true;
        }
        try {
            final Process probe = new ProcessBuilder("wsl", "true").start();
            return probe.waitFor(15, TimeUnit.SECONDS) && probe.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}
