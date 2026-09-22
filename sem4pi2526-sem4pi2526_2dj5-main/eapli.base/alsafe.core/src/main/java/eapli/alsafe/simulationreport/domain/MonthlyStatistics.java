package eapli.alsafe.simulationreport.domain;

import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MonthlyStatistics {

    private final YearMonth period;
    private final int totalSimulations;
    private final int totalFlights;
    private final int totalSafetyViolations;
    private final int passedSimulations;
    private final int failedSimulations;
    private final Map<String, Integer> flightStatusTotals;

    public MonthlyStatistics(final YearMonth period,
                             final int totalSimulations,
                             final int totalFlights,
                             final int totalSafetyViolations,
                             final int passedSimulations,
                             final int failedSimulations,
                             final Map<String, Integer> flightStatusTotals) {
        if (period == null) {
            throw new IllegalArgumentException("Report period is required.");
        }
        if (totalSimulations < 0 || totalFlights < 0 || totalSafetyViolations < 0
                || passedSimulations < 0 || failedSimulations < 0) {
            throw new IllegalArgumentException("Monthly statistics cannot contain negative totals.");
        }
        if (passedSimulations + failedSimulations != totalSimulations) {
            throw new IllegalArgumentException("Passed and failed simulations must match total simulations.");
        }
        if (flightStatusTotals == null) {
            throw new IllegalArgumentException("Flight status totals are required.");
        }

        this.period = period;
        this.totalSimulations = totalSimulations;
        this.totalFlights = totalFlights;
        this.totalSafetyViolations = totalSafetyViolations;
        this.passedSimulations = passedSimulations;
        this.failedSimulations = failedSimulations;
        this.flightStatusTotals = Map.copyOf(flightStatusTotals);
    }

    public YearMonth period() {
        return period;
    }

    public int totalSimulations() {
        return totalSimulations;
    }

    public int totalFlights() {
        return totalFlights;
    }

    public int totalSafetyViolations() {
        return totalSafetyViolations;
    }

    public int passedSimulations() {
        return passedSimulations;
    }

    public int failedSimulations() {
        return failedSimulations;
    }

    public Map<String, Integer> flightStatusTotals() {
        return flightStatusTotals;
    }

    public static MonthlyStatistics from(final YearMonth period,
                                         final Iterable<SimulationReportSnapshot> snapshots) {
        int totalSimulations = 0;
        int totalFlights = 0;
        int totalSafetyViolations = 0;
        int passedSimulations = 0;
        int failedSimulations = 0;
        final Map<String, Integer> statusTotals = new LinkedHashMap<>();

        for (final SimulationReportSnapshot snapshot : snapshots) {
            totalSimulations++;
            totalFlights += snapshot.totalFlights();
            totalSafetyViolations += snapshot.safetyViolations().size();

            if (snapshot.validationResult() == ValidationResult.PASS) {
                passedSimulations++;
            } else {
                failedSimulations++;
            }

            for (final FlightStatusSummary status : snapshot.flightStatuses()) {
                statusTotals.merge(status.status(), 1, Integer::sum);
            }
        }

        return new MonthlyStatistics(period, totalSimulations, totalFlights, totalSafetyViolations,
                passedSimulations, failedSimulations, statusTotals);
    }
}
