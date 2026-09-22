package eapli.alsafe.simulationreport.application;

import eapli.alsafe.Application;
import eapli.alsafe.simulationreport.domain.SimulationReport;
import eapli.alsafe.simulationreport.domain.SimulationReportParser;
import eapli.alsafe.simulationreport.domain.SimulationReportWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

public class GenerateSimulationReportService {

    private static final String SOURCE_REPORT_FILE = "simulation_report.txt";

    private final Path reportsDirectory;
    private final Clock clock;
    private final SimulationReportParser parser;
    private final SimulationReportWriter writer;

    public GenerateSimulationReportService() {
        this(resolveReportsDirectory(), Clock.systemDefaultZone(),
                new SimulationReportParser(), new SimulationReportWriter());
    }

    public GenerateSimulationReportService(final Path reportsDirectory,
                                           final Clock clock,
                                           final SimulationReportParser parser,
                                           final SimulationReportWriter writer) {
        if (reportsDirectory == null || clock == null || parser == null || writer == null) {
            throw new IllegalArgumentException("Report service dependencies are required.");
        }
        this.reportsDirectory = reportsDirectory;
        this.clock = clock;
        this.parser = parser;
        this.writer = writer;
    }

    public Path generateReport() throws IOException {
        final Path sourcePath = reportsDirectory.resolve(SOURCE_REPORT_FILE);
        final SimulationReport report = parser.parse(sourcePath);
        return writer.write(report, reportsDirectory, clock);
    }

    private static String settingOrDefault(final String key, final String defaultValue) {
        final String value = Application.settings().getProperty(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static Path resolveReportsDirectory() {
        final Path configuredReports = Path.of(settingOrDefault("scomp.dir", "./SCOMP")).resolve("reports");
        if (Files.exists(configuredReports)) {
            return configuredReports;
        }

        final Path parentReports = Path.of("..", "SCOMP", "reports");
        if (Files.exists(parentReports)) {
            return parentReports;
        }

        return configuredReports;
    }
}
