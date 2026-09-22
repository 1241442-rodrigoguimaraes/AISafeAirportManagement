package domain.simulationreport;

import eapli.alsafe.simulationreport.domain.FlightStatusSummary;
import eapli.alsafe.simulationreport.domain.Position;
import eapli.alsafe.simulationreport.domain.SafetyViolationSummary;
import eapli.alsafe.simulationreport.domain.SimulationReport;
import eapli.alsafe.simulationreport.domain.SimulationReportWriter;
import eapli.alsafe.simulationreport.domain.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationReportWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writesTimestampedSimulationReportFile() throws Exception {
        final var report = new SimulationReport(Path.of("SCOMP/reports/simulation_report.txt"), 1,
                List.of(new FlightStatusSummary("TP123", 10, "COMPLETED")),
                List.of(new SafetyViolationSummary(
                        "T+0 s (step 0)",
                        "TP123 (id: 10)",
                        "AA004 (id: 20)",
                        new Position(41.0, -8.0, 100.0),
                        new Position(41.1, -8.1, 120.0))),
                ValidationResult.FAIL);
        final var clock = Clock.fixed(Instant.parse("2026-06-13T17:16:20Z"), ZoneOffset.UTC);

        final Path output = new SimulationReportWriter().write(report, tempDir, clock);

        assertEquals("simulation_report_20260613_171620.txt", output.getFileName().toString());
        final String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("Total flights: 1"));
        assertTrue(content.contains("Flight TP123 (id: 10) - COMPLETED"));
        assertTrue(content.contains("Violation #1"));
        assertTrue(content.contains("Timestamp: T+0 s (step 0)"));
        assertTrue(content.contains("VALIDATION RESULT: FAIL"));
    }

    @Test
    void writesNoViolationMessage() throws Exception {
        final var report = new SimulationReport(Path.of("source.txt"), 1,
                List.of(new FlightStatusSummary("EK312", 1, "COMPLETED")),
                List.of(), ValidationResult.PASS);

        final Path output = new SimulationReportWriter().write(report, tempDir, Clock.systemUTC());

        assertTrue(Files.readString(output, StandardCharsets.UTF_8)
                .contains("No safety violations were recorded."));
    }

    @Test
    void writerRequiresDependencies() {
        final var writer = new SimulationReportWriter();
        assertThrows(IllegalArgumentException.class, () -> writer.write(null, tempDir, Clock.systemUTC()));
    }
}
