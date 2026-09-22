package domain.simulationreport;

import eapli.alsafe.simulationreport.domain.FlightStatusSummary;
import eapli.alsafe.simulationreport.domain.Position;
import eapli.alsafe.simulationreport.domain.SafetyViolationSummary;
import eapli.alsafe.simulationreport.domain.SimulationReport;
import eapli.alsafe.simulationreport.domain.ValidationResult;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationReportTest {

    @Test
    void validFailedReport_keepsFlightsViolationsAndResult() {
        final var flight = new FlightStatusSummary("TP123", 1, "TERMINATED_SAFETY");
        final var violation = new SafetyViolationSummary(
                "T+0 s (step 0)",
                "TP123 (id: 1)",
                "AA004 (id: 2)",
                new Position(41.0, -8.0, 100.0),
                new Position(41.1, -8.1, 120.0));

        final var report = new SimulationReport(Path.of("source.txt"), 1,
                List.of(flight), List.of(violation), ValidationResult.FAIL);

        assertEquals(1, report.totalFlights());
        assertEquals(1, report.flightStatuses().size());
        assertEquals(1, report.safetyViolations().size());
        assertFalse(report.passed());
    }

    @Test
    void validPassedReportWithNoViolations_isPassed() {
        final var flight = new FlightStatusSummary("EK312", 3, "COMPLETED");

        final var report = new SimulationReport(Path.of("source.txt"), 1,
                List.of(flight), List.of(), ValidationResult.PASS);

        assertTrue(report.passed());
        assertEquals(ValidationResult.PASS, report.validationResult());
    }

    @Test
    void totalFlightsMustMatchListedStatuses() {
        final var flight = new FlightStatusSummary("EK312", 3, "COMPLETED");

        assertThrows(IllegalArgumentException.class, () ->
                new SimulationReport(Path.of("source.txt"), 2,
                        List.of(flight), List.of(), ValidationResult.PASS));
    }

    @Test
    void invalidDomainValuesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Position(Double.NaN, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new FlightStatusSummary(" ", 1, "COMPLETED"));
        assertThrows(IllegalArgumentException.class, () -> new SafetyViolationSummary(
                "", "A", "B", new Position(0, 0, 0), new Position(1, 1, 1)));
        assertThrows(IllegalArgumentException.class, () -> ValidationResult.fromText(""));
    }
}
