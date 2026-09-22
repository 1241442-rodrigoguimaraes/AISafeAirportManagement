package eapli.alsafe.simulationreport.application;

import eapli.alsafe.simulationreport.domain.FlightStatusSummary;
import eapli.alsafe.simulationreport.domain.MonthlyReportWarning;
import eapli.alsafe.simulationreport.domain.MonthlyStatistics;
import eapli.alsafe.simulationreport.domain.MonthlyStatisticsReport;
import eapli.alsafe.simulationreport.domain.SafetyViolationSummary;
import eapli.alsafe.simulationreport.domain.SimulationReportSnapshot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class MonthlyStatisticsReportWriter {

    private static final DateTimeFormatter REPORT_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Path write(final MonthlyStatisticsReport report, final Path outputDirectory, final Clock clock) throws IOException {
        if (report == null || outputDirectory == null || clock == null) {
            throw new IllegalArgumentException("Monthly report writer dependencies are required.");
        }

        Files.createDirectories(outputDirectory);
        final Path outputPath = outputDirectory.resolve(outputFileName(report.period()));
        Files.writeString(outputPath, content(report, LocalDateTime.now(clock)), StandardCharsets.UTF_8);
        return outputPath;
    }

    static String outputFileName(final YearMonth period) {
        return String.format("monthly_statistics_report_%d_%02d.txt", period.getYear(), period.getMonthValue());
    }

    String content(final MonthlyStatisticsReport report, final LocalDateTime generatedAt) {
        final MonthlyStatistics statistics = report.statistics();
        final StringBuilder builder = new StringBuilder();

        builder.append("========================================\n");
        builder.append("ALSAFE - MONTHLY STATISTICS REPORT\n");
        builder.append("========================================\n");
        builder.append("Period: ").append(report.period()).append('\n');
        builder.append("Generated at: ").append(generatedAt.format(REPORT_TIMESTAMP)).append("\n\n");

        builder.append("SUMMARY\n");
        builder.append("Total simulations: ").append(statistics.totalSimulations()).append('\n');
        builder.append("Total flights: ").append(statistics.totalFlights()).append('\n');
        builder.append("Total safety violations: ").append(statistics.totalSafetyViolations()).append('\n');
        builder.append("Passed simulations: ").append(statistics.passedSimulations()).append('\n');
        builder.append("Failed simulations: ").append(statistics.failedSimulations()).append("\n\n");

        builder.append("FLIGHT STATUS TOTALS\n");
        if (statistics.flightStatusTotals().isEmpty()) {
            builder.append("No flight status data found.\n");
        } else {
            for (final Map.Entry<String, Integer> entry : statistics.flightStatusTotals().entrySet()) {
                builder.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
            }
        }
        builder.append('\n');

        builder.append("SIMULATION DETAILS\n");
        if (report.snapshots().isEmpty()) {
            builder.append("No simulation data found for this period.\n");
        } else {
            int simulationNumber = 1;
            for (final SimulationReportSnapshot snapshot : report.snapshots()) {
                builder.append("Simulation #").append(simulationNumber++).append(" - ")
                        .append(snapshot.generatedAt().format(REPORT_TIMESTAMP)).append('\n');
                builder.append("- Source: ").append(snapshot.sourcePath()).append('\n');
                builder.append("- Result: ").append(snapshot.validationResult()).append('\n');
                builder.append("- Flights: ").append(snapshot.totalFlights()).append('\n');
                builder.append("- Safety violations: ").append(snapshot.safetyViolations().size()).append('\n');
                builder.append("- Flight statuses:\n");
                for (final FlightStatusSummary status : snapshot.flightStatuses()) {
                    builder.append("  - Flight ").append(status.designator())
                            .append(" (id: ").append(status.id())
                            .append(") - ").append(status.status()).append('\n');
                }

                if (!snapshot.safetyViolations().isEmpty()) {
                    builder.append("- Violations:\n");
                    int violationNumber = 1;
                    for (final SafetyViolationSummary violation : snapshot.safetyViolations()) {
                        builder.append("  Violation #").append(violationNumber++).append('\n');
                        builder.append("  - Timestamp: ").append(violation.timestamp()).append('\n');
                        builder.append("  - Flight A: ").append(violation.flightA()).append('\n');
                        builder.append("  - Flight B: ").append(violation.flightB()).append('\n');
                    }
                }
                builder.append('\n');
            }
        }

        builder.append("WARNINGS\n");
        if (report.warnings().isEmpty()) {
            builder.append("No malformed simulation report files were found.\n");
        } else {
            for (final MonthlyReportWarning warning : report.warnings()) {
                builder.append("- ").append(warning.sourcePath()).append(": ").append(warning.reason()).append('\n');
            }
        }

        builder.append('\n');
        builder.append("VALIDATION SUMMARY\n");
        builder.append(statistics.failedSimulations() == 0 && statistics.totalSafetyViolations() == 0
                ? "Overall result: PASS\n"
                : "Overall result: FAIL\n");
        builder.append("========================================\n");

        return builder.toString();
    }
}
