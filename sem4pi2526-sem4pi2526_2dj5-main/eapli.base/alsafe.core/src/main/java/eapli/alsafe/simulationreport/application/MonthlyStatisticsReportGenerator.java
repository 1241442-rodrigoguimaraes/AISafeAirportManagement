package eapli.alsafe.simulationreport.application;

import eapli.alsafe.Application;
import eapli.alsafe.simulationreport.domain.MonthlyReportWarning;
import eapli.alsafe.simulationreport.domain.MonthlySimulationReportParser;
import eapli.alsafe.simulationreport.domain.MonthlyStatistics;
import eapli.alsafe.simulationreport.domain.MonthlyStatisticsReport;
import eapli.alsafe.simulationreport.domain.SimulationReportSnapshot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;

public class MonthlyStatisticsReportGenerator implements ReportGenerator {

    private final Path inputDirectory;
    private final Path outputDirectory;
    private final Clock clock;
    private final MonthlySimulationReportParser parser;
    private final MonthlyStatisticsReportWriter writer;

    public MonthlyStatisticsReportGenerator() {
        this(resolveInputDirectory(), resolveOutputDirectory(), Clock.systemDefaultZone(),
                new MonthlySimulationReportParser(), new MonthlyStatisticsReportWriter());
    }

    public MonthlyStatisticsReportGenerator(final Path inputDirectory,
                                            final Path outputDirectory,
                                            final Clock clock,
                                            final MonthlySimulationReportParser parser,
                                            final MonthlyStatisticsReportWriter writer) {
        if (inputDirectory == null || outputDirectory == null || clock == null || parser == null || writer == null) {
            throw new IllegalArgumentException("Monthly report generator dependencies are required.");
        }
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
        this.clock = clock;
        this.parser = parser;
        this.writer = writer;
    }

    @Override
    public Path generate(final YearMonth period) throws IOException {
        if (period == null) {
            throw new IllegalArgumentException("Report period is required.");
        }

        final List<SimulationReportSnapshot> snapshots = new ArrayList<>();
        final List<MonthlyReportWarning> warnings = new ArrayList<>();

        if (Files.exists(inputDirectory)) {
            try (var paths = Files.list(inputDirectory)) {
                final List<Path> candidates = paths
                        .filter(Files::isRegularFile)
                        .filter(this::matchesTimestampedReportName)
                        .filter(path -> belongsToPeriod(path, period))
                        .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                        .toList();

                for (final Path candidate : candidates) {
                    try {
                        snapshots.add(parser.parse(candidate));
                    } catch (final RuntimeException | IOException ex) {
                        warnings.add(new MonthlyReportWarning(candidate, ex.getMessage()));
                    }
                }
            }
        }

        final MonthlyStatistics statistics = MonthlyStatistics.from(period, snapshots);
        final MonthlyStatisticsReport report = new MonthlyStatisticsReport(period, statistics, snapshots, warnings);
        return writer.write(report, outputDirectory, clock);
    }

    @Override
    public String reportType() {
        return "Monthly Statistics Report";
    }

    private boolean matchesTimestampedReportName(final Path path) {
        return MonthlySimulationReportParser.TIMESTAMPED_REPORT_PATTERN
                .matcher(path.getFileName().toString())
                .matches();
    }

    private boolean belongsToPeriod(final Path path, final YearMonth period) {
        final Matcher matcher = MonthlySimulationReportParser.TIMESTAMPED_REPORT_PATTERN
                .matcher(path.getFileName().toString());
        return matcher.matches()
                && YearMonth.from(MonthlySimulationReportParser.timestampFromFileName(path)).equals(period);
    }

    private static Path resolveInputDirectory() {
        final String configured = Application.settings().getProperty("simulation.reports.input.dir");
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured);
        }
        return resolveReportsDirectory();
    }

    private static Path resolveOutputDirectory() {
        final String configured = Application.settings().getProperty("monthly.reports.output.dir");
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured);
        }
        return resolveReportsDirectory();
    }

    private static Path resolveReportsDirectory() {
        final String scompDir = settingOrDefault("scomp.dir", "./SCOMP");
        final Path configuredReports = Path.of(scompDir).resolve("reports");
        if (Files.exists(configuredReports)) {
            return configuredReports;
        }

        final Path parentReports = Path.of("..", "SCOMP", "reports");
        if (Files.exists(parentReports)) {
            return parentReports;
        }

        return configuredReports;
    }

    private static String settingOrDefault(final String key, final String defaultValue) {
        final String value = Application.settings().getProperty(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
