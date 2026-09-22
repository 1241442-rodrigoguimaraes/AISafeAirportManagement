package eapli.alsafe.simulationreport.domain;

import java.time.YearMonth;
import java.util.List;

public final class MonthlyStatisticsReport {

    private final YearMonth period;
    private final MonthlyStatistics statistics;
    private final List<SimulationReportSnapshot> snapshots;
    private final List<MonthlyReportWarning> warnings;

    public MonthlyStatisticsReport(final YearMonth period,
                                   final MonthlyStatistics statistics,
                                   final List<SimulationReportSnapshot> snapshots,
                                   final List<MonthlyReportWarning> warnings) {
        if (period == null || statistics == null || snapshots == null || warnings == null) {
            throw new IllegalArgumentException("Monthly report data is required.");
        }
        if (!period.equals(statistics.period())) {
            throw new IllegalArgumentException("Monthly statistics period must match report period.");
        }

        this.period = period;
        this.statistics = statistics;
        this.snapshots = List.copyOf(snapshots);
        this.warnings = List.copyOf(warnings);
    }

    public YearMonth period() {
        return period;
    }

    public MonthlyStatistics statistics() {
        return statistics;
    }

    public List<SimulationReportSnapshot> snapshots() {
        return snapshots;
    }

    public List<MonthlyReportWarning> warnings() {
        return warnings;
    }
}
