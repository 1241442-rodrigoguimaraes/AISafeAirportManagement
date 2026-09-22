package eapli.alsafe.simulationreport.application;

public final class ReportGeneratorFactory {

    private ReportGeneratorFactory() {
    }

    public static ReportGenerator monthlyStatistics() {
        return new MonthlyStatisticsReportGenerator();
    }
}
