package domain.simulationreport;

import eapli.alsafe.simulationreport.domain.SimulationReportParser;
import eapli.alsafe.simulationreport.domain.ValidationResult;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimulationReportParserTest {

    private final SimulationReportParser parser = new SimulationReportParser();

    @Test
    void parsesCurrentScompReportFormat() {
        final var report = parser.parse(Path.of("SCOMP/reports/simulation_report.txt"), List.of(
                "# Final Simulation Report",
                "Generated at: Sat Jun 13 17:16:20 2026",
                "Total flights: 2",
                "Total safety violations: 1",
                "Validation result: FAIL",
                "",
                "## Flight Execution Statuses",
                "Flight AA004 (id: 213626907)",
                "- PID: 2208",
                "- Status: TERMINATED_SAFETY",
                "Flight PT99P (id: 232108651)",
                "- PID: 2210",
                "- Status: TERMINATED_SAFETY",
                "",
                "## Safety Violation Events",
                "Event 1",
                "- Simulation timestamp: T+0 s (step 0)",
                "- Type: Critical collision",
                "- Flights: AA004 (id: 213626907) and PT99P (id: 232108651)",
                "- Flight A position: Lat=33.941600, Lon=-118.408500, Alt=38.00",
                "- Flight B position: Lat=33.941600, Lon=-118.408500, Alt=38.00"
        ));

        assertEquals(2, report.totalFlights());
        assertEquals(2, report.flightStatuses().size());
        assertEquals("AA004", report.flightStatuses().get(0).designator());
        assertEquals("TERMINATED_SAFETY", report.flightStatuses().get(0).status());
        assertEquals(1, report.safetyViolations().size());
        assertEquals("T+0 s (step 0)", report.safetyViolations().get(0).timestamp());
        assertEquals(ValidationResult.FAIL, report.validationResult());
    }

    @Test
    void parsesReportWithNoSafetyViolations() {
        final var report = parser.parse(Path.of("SCOMP/reports/simulation_report.txt"), List.of(
                "# Final Simulation Report",
                "Total flights: 1",
                "Total safety violations: 0",
                "Validation result: PASS",
                "Flight EK312 (id: 218733259)",
                "- Status: COMPLETED",
                "## Safety Violation Events",
                "No safety violations were recorded."
        ));

        assertEquals(1, report.totalFlights());
        assertEquals(0, report.safetyViolations().size());
        assertEquals(ValidationResult.PASS, report.validationResult());
    }

    @Test
    void missingRequiredFieldsFailFast() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(Path.of("bad.txt"), List.of(
                        "Total safety violations: 0",
                        "Validation result: PASS"
                )));
    }

    @Test
    void declaredViolationsMustMatchListedEvents() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(Path.of("bad.txt"), List.of(
                        "Total flights: 1",
                        "Total safety violations: 1",
                        "Validation result: FAIL",
                        "Flight TP123 (id: 1)",
                        "- Status: COMPLETED"
                )));
    }

    @Test
    void malformedPositionIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(Path.of("bad.txt"), List.of(
                        "Total flights: 1",
                        "Total safety violations: 1",
                        "Validation result: FAIL",
                        "Flight TP123 (id: 1)",
                        "- Status: TERMINATED_SAFETY",
                        "Event 1",
                        "- Simulation timestamp: T+0 s (step 0)",
                        "- Flights: TP123 (id: 1) and AA004 (id: 2)",
                        "- Flight A position: nowhere",
                        "- Flight B position: Lat=0.000000, Lon=0.000000, Alt=0.00"
                )));
    }
}
