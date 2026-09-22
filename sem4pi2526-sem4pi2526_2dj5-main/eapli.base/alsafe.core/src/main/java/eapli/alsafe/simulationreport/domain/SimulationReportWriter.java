package eapli.alsafe.simulationreport.domain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SimulationReportWriter {

    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter REPORT_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Path write(final SimulationReport report, final Path outputDirectory, final Clock clock) throws IOException {
        if (report == null) {
            throw new IllegalArgumentException("Simulation report is required.");
        }
        if (outputDirectory == null) {
            throw new IllegalArgumentException("Output directory is required.");
        }
        if (clock == null) {
            throw new IllegalArgumentException("Clock is required.");
        }

        Files.createDirectories(outputDirectory);

        final LocalDateTime generatedAt = LocalDateTime.now(clock);
        final Path outputPath = outputDirectory.resolve("simulation_report_" + generatedAt.format(FILE_TIMESTAMP) + ".txt");
        Files.writeString(outputPath, content(report, generatedAt), StandardCharsets.UTF_8);
        return outputPath;
    }

    String content(final SimulationReport report, final LocalDateTime generatedAt) {
        final StringBuilder builder = new StringBuilder();
        builder.append("========================================\n");
        builder.append("ALSAFE - SIMULATION REPORT\n");
        builder.append("========================================\n");
        builder.append("Generated at: ").append(generatedAt.format(REPORT_TIMESTAMP)).append('\n');
        builder.append("Source report: ").append(report.sourcePath()).append("\n\n");

        builder.append("SIMULATION RESULT: ").append(report.validationResult()).append("\n\n");

        builder.append("FLIGHTS SUMMARY\n");
        builder.append("Total flights: ").append(report.totalFlights()).append('\n');
        for (final FlightStatusSummary flight : report.flightStatuses()) {
            builder.append("- Flight ").append(flight.designator())
                    .append(" (id: ").append(flight.id())
                    .append(") - ").append(flight.status()).append('\n');
        }
        builder.append('\n');

        builder.append("SAFETY VIOLATIONS\n");
        if (report.safetyViolations().isEmpty()) {
            builder.append("No safety violations were recorded.\n");
        } else {
            int number = 1;
            for (final SafetyViolationSummary violation : report.safetyViolations()) {
                builder.append("Violation #").append(number++).append('\n');
                builder.append("- Timestamp: ").append(violation.timestamp()).append('\n');
                builder.append("- Flight A: ").append(violation.flightA())
                        .append(" at ").append(formatPosition(violation.positionA())).append('\n');
                builder.append("- Flight B: ").append(violation.flightB())
                        .append(" at ").append(formatPosition(violation.positionB())).append('\n');
            }
        }

        builder.append('\n');
        builder.append("VALIDATION RESULT: ").append(report.validationResult()).append('\n');
        builder.append("========================================\n");
        return builder.toString();
    }

    private static String formatPosition(final Position position) {
        return String.format(Locale.US, "Lat=%.6f, Lon=%.6f, Alt=%.2f",
                position.latitude(), position.longitude(), position.altitude());
    }
}
