package eapli.alsafe.simulationreport.domain;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonthlyStatisticsTest {

    @Test
    void aggregatesMultipleSnapshotsAndCountsDuplicateFlightsAsEntries() {
        final YearMonth period = YearMonth.of(2026, 6);
        final SimulationReportSnapshot first = snapshot("a.txt", 3, ValidationResult.FAIL,
                List.of(
                        new FlightStatusSummary("TP123", 1, "TERMINATED_SAFETY"),
                        new FlightStatusSummary("TP123", 1, "TERMINATED_SAFETY"),
                        new FlightStatusSummary("EK312", 2, "COMPLETED")),
                1);
        final SimulationReportSnapshot second = snapshot("b.txt", 1, ValidationResult.PASS,
                List.of(new FlightStatusSummary("QF143", 3, "COMPLETED")),
                0);

        final MonthlyStatistics statistics = MonthlyStatistics.from(period, List.of(first, second));

        assertEquals(2, statistics.totalSimulations());
        assertEquals(4, statistics.totalFlights());
        assertEquals(1, statistics.totalSafetyViolations());
        assertEquals(1, statistics.passedSimulations());
        assertEquals(1, statistics.failedSimulations());
        assertEquals(2, statistics.flightStatusTotals().get("TERMINATED_SAFETY"));
        assertEquals(2, statistics.flightStatusTotals().get("COMPLETED"));
    }

    @Test
    void emptyMonthProducesZeroStatistics() {
        final MonthlyStatistics statistics = MonthlyStatistics.from(YearMonth.of(2026, 7), List.of());

        assertEquals(0, statistics.totalSimulations());
        assertEquals(0, statistics.totalFlights());
        assertEquals(0, statistics.totalSafetyViolations());
        assertEquals(Map.of(), statistics.flightStatusTotals());
    }

    @Test
    void invalidStatisticsAreRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new MonthlyStatistics(YearMonth.of(2026, 6), 2, 0, 0, 2, 1, Map.of()));
    }

    private static SimulationReportSnapshot snapshot(final String source,
                                                     final int totalFlights,
                                                     final ValidationResult result,
                                                     final List<FlightStatusSummary> statuses,
                                                     final int violations) {
        return new SimulationReportSnapshot(Path.of(source), LocalDateTime.of(2026, 6, 1, 12, 0),
                totalFlights, statuses, violationList(violations), result);
    }

    private static List<SafetyViolationSummary> violationList(final int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new SafetyViolationSummary("T+" + i + " s", "A", "B",
                        new Position(0, 0, 0), new Position(1, 1, 1)))
                .toList();
    }
}
