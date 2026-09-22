package eapli.alsafe.flightPlan.infrastructure;

import eapli.alsafe.Application;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SCOMPFlightPlanTestRunner implements FlightPlanTestRunner {
    private static final long TIMEOUT_SECONDS = 60;

    private final Path scompDir;
    private final String binaryName;

    public SCOMPFlightPlanTestRunner() {
        this(Path.of(settingOrDefault("scomp.dir", "./SCOMP")),
                settingOrDefault("scomp.binary", "flight_simulation"));
    }

    public SCOMPFlightPlanTestRunner(Path scompDir, String binaryName) {
        this.scompDir = scompDir;
        this.binaryName = binaryName;
    }

    @Override
    public TestResult run(String dslContent) throws IOException {
        Path fpFile = Files.createTempFile("flightPlan", ".fp");
        Path outFile = Files.createTempFile("flightPlanTest", ".out");
        Files.writeString(fpFile, dslContent, StandardCharsets.UTF_8);

        try {
            List<String> command = new ArrayList<>();
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
            if (isWindows) {
                command.add("wsl");
                command.add("./" + binaryName);
                command.add(toWslPath(fpFile));
            } else {
                command.add("./" + binaryName);
                command.add(fpFile.toAbsolutePath().toString());
            }

            Process process = new ProcessBuilder(command)
                    .directory(scompDir.toFile())
                    .redirectErrorStream(true)
                    .redirectOutput(outFile.toFile())
                    .start();

            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IOException("C test component timed out after " + TIMEOUT_SECONDS + "s.");
            }

            String output = Files.readString(outFile, StandardCharsets.UTF_8);
            int exitCode = process.exitValue();
            return new TestResult(exitCode == 0, exitCode, output);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("C test component interrupted", e);
        } finally {
            Files.deleteIfExists(fpFile);
            Files.deleteIfExists(outFile);
        }
    }

    public static String toWslPath(Path winPath) {
        String wslPath = winPath.toAbsolutePath().toString().replace("\\", "/");
        return "/mnt/" + Character.toLowerCase(wslPath.charAt(0)) + wslPath.substring(2);
    }

    private static String settingOrDefault(final String key, final String defaultValue) {
        final String value = Application.settings().getProperty(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
