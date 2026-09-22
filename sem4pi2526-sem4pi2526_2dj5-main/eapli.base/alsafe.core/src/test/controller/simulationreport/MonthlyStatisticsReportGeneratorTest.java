package eapli.alsafe.simulationreport.application;

import eapli.alsafe.simulationreport.domain.MonthlySimulationReportParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonthlyStatisticsReportGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void noFilesInMonthProducesReportWithNoData() throws Exception {
        final Path output = generator().generate(YearMonth.of(2026, 7));

        final String content = Files.readString(output, StandardCharsets.UTF_8);
        assertEquals("monthly_statistics_report_2026_07.txt", output.getFileName().toString());
        assertTrue(content.contains("No simulation data found for this period."));
        assertTrue(content.contains("Total simulations: 0"));
    }

    @Test
    void multipleSimulationsInSameMonthAggregateCorrectlyAndIgnoreOtherMonths() throws Exception {
        writeReport("simulation_report_20260601_120000.txt", "PASS", 1, false);
        writeReport("simulation_report_20260614_002917.txt", "FAIL", 3, true);
        writeReport("simulation_report_20260701_120000.txt", "PASS", 9, false);

        final Path output = generator().generate(YearMonth.of(2026, 6));

        final String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("Total simulations: 2"));
        assertTrue(content.contains("Total flights: 4"));
        assertTrue(content.contains("Total safety violations: 1"));
        assertTrue(content.contains("Passed simulations: 1"));
        assertTrue(content.contains("Failed simulations: 1"));
        assertTrue(content.contains("- COMPLETED: 2"));
        assertTrue(content.contains("- TERMINATED_SAFETY: 2"));
    }

    @Test
    void malformedFilesAreReportedAsWarningsWithoutStoppingGeneration() throws Exception {
        writeReport("simulation_report_20260601_120000.txt", "PASS", 1, false);
        Files.writeString(tempDir.resolve("simulation_report_20260602_120000.txt"),
                "VALIDATION RESULT: PASS", StandardCharsets.UTF_8);

        final Path output = generator().generate(YearMonth.of(2026, 6));

        final String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("Total simulations: 1"));
        assertTrue(content.contains("WARNINGS"));
        assertTrue(content.contains("simulation_report_20260602_120000.txt"));
        assertTrue(content.contains("Missing total flights"));
    }

    @Test
    void reportTypeIdentifiesStrategy() {
        assertEquals("Monthly Statistics Report", generator().reportType());
    }

    private MonthlyStatisticsReportGenerator generator() {
        return new MonthlyStatisticsReportGenerator(
                tempDir,
                tempDir,
                Clock.fixed(Instant.parse("2026-06-14T00:30:00Z"), ZoneOffset.UTC),
                new MonthlySimulationReportParser(),
                new MonthlyStatisticsReportWriter());
    }

    private void writeReport(final String fileName,
                             final String result,
                             final int totalFlights,
                             final boolean withViolation) throws Exception {
        final String violations = withViolation
                ? """
                Violation #1
                - Timestamp: T+0 s (step 0)
                - Flight A: TP123 (id: 1) at Lat=41.000000, Lon=-8.000000, Alt=69.00
                - Flight B: EK312 (id: 2) at Lat=42.000000, Lon=-8.500000, Alt=70.00
                """
                : "No safety violations were recorded.\n";

        Files.writeString(tempDir.resolve(fileName), String.join("\n",
                "ALSAFE - SIMULATION REPORT",
                "SIMULATION RESULT: " + result,
                "FLIGHTS SUMMARY",
                "Total flights: " + totalFlights,
                "- Flight TP123 (id: 1) - " + (withViolation ? "TERMINATED_SAFETY" : "COMPLETED"),
                totalFlights > 1 ? "- Flight TP123 (id: 1) - TERMINATED_SAFETY" : "",
                totalFlights > 2 ? "- Flight EK312 (id: 2) - COMPLETED" : "",
                "SAFETY VIOLATIONS",
                violations,
                "VALIDATION RESULT: " + result),
                StandardCharsets.UTF_8);
    }
}
