package eapli.alsafe.simulationreport.domain;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonthlySimulationReportParserTest {

    private final MonthlySimulationReportParser parser = new MonthlySimulationReportParser();

    @Test
    void parsesRealUs111FormatAndCountsDuplicateFlightsAsEntries() {
        final SimulationReportSnapshot snapshot = parser.parse(Path.of("simulation_report_20260614_002917.txt"), List.of(
                "========================================",
                "ALSAFE - SIMULATION REPORT",
                "Generated at: 2026-06-14 00:29:17",
                "SIMULATION RESULT: FAIL",
                "FLIGHTS SUMMARY",
                "Total flights: 3",
                "- Flight TP123 (id: 236699615) - TERMINATED_SAFETY",
                "- Flight TP123 (id: 236699615) - TERMINATED_SAFETY",
                "- Flight EK312 (id: 218733259) - COMPLETED",
                "SAFETY VIOLATIONS",
                "Violation #1",
                "- Timestamp: T+0 s (step 0)",
                "- Flight A: TP123 (id: 236699615) at Lat=41.262891, Lon=-8.685220, Alt=69.00",
                "- Flight B: EK312 (id: 218733259) at Lat=41.000000, Lon=-8.000000, Alt=70.00",
                "VALIDATION RESULT: FAIL"
        ));

        assertEquals(LocalDateTime.of(2026, 6, 14, 0, 29, 17), snapshot.generatedAt());
        assertEquals(3, snapshot.totalFlights());
        assertEquals(3, snapshot.flightStatuses().size());
        assertEquals(2, snapshot.flightStatuses().stream().filter(f -> f.designator().equals("TP123")).count());
        assertEquals(1, snapshot.safetyViolations().size());
        assertEquals(ValidationResult.FAIL, snapshot.validationResult());
    }

    @Test
    void parsesReportWithNoViolations() {
        final SimulationReportSnapshot snapshot = parser.parse(Path.of("simulation_report_20260601_120000.txt"), List.of(
                "Total flights: 1",
                "- Flight EK312 (id: 218733259) - COMPLETED",
                "SAFETY VIOLATIONS",
                "No safety violations were recorded.",
                "VALIDATION RESULT: PASS"
        ));

        assertEquals(0, snapshot.safetyViolations().size());
        assertEquals(ValidationResult.PASS, snapshot.validationResult());
    }

    @Test
    void malformedReportIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(Path.of("simulation_report_20260601_120000.txt"), List.of(
                        "- Flight EK312 (id: 218733259) - COMPLETED",
                        "VALIDATION RESULT: PASS"
                )));
    }

    @Test
    void invalidFileNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(Path.of("simulation_report.txt"), List.of(
                        "Total flights: 0",
                        "VALIDATION RESULT: PASS"
                )));
    }
}
