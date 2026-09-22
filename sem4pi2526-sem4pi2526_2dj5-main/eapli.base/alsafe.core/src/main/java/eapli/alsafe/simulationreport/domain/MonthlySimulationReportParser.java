package eapli.alsafe.simulationreport.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonthlySimulationReportParser {

    public static final Pattern TIMESTAMPED_REPORT_PATTERN =
            Pattern.compile("^simulation_report_(\\d{8}_\\d{6})\\.txt$");

    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Pattern FLIGHT_STATUS_PATTERN =
            Pattern.compile("^- Flight\\s+(.+)\\s+\\(id:\\s*(-?\\d+)\\)\\s+-\\s+(.+)$");
    private static final Pattern FLIGHT_POSITION_PATTERN =
            Pattern.compile("^- Flight [AB]:\\s+(.+)\\s+at\\s+Lat=([-+]?\\d+(?:\\.\\d+)?),\\s*Lon=([-+]?\\d+(?:\\.\\d+)?),\\s*Alt=([-+]?\\d+(?:\\.\\d+)?).*$");

    public SimulationReportSnapshot parse(final Path sourcePath) throws IOException {
        if (sourcePath == null) {
            throw new IllegalArgumentException("Source report path is required.");
        }
        if (!Files.exists(sourcePath)) {
            throw new IOException("Simulation report file not found: " + sourcePath);
        }

        return parse(sourcePath, Files.readAllLines(sourcePath));
    }

    public SimulationReportSnapshot parse(final Path sourcePath, final List<String> lines) {
        if (lines == null) {
            throw new IllegalArgumentException("Report lines are required.");
        }

        final LocalDateTime timestamp = timestampFromFileName(sourcePath);
        Integer totalFlights = null;
        ValidationResult validationResult = null;
        final List<FlightStatusSummary> flightStatuses = new ArrayList<>();
        final List<SafetyViolationSummary> violations = new ArrayList<>();

        String violationTimestamp = null;
        String violationFlightA = null;
        String violationFlightB = null;
        Position violationPositionA = null;
        Position violationPositionB = null;
        boolean inViolation = false;

        for (final String rawLine : lines) {
            final String line = rawLine.trim();

            if (line.startsWith("Total flights:")) {
                totalFlights = parseIntegerAfterColon(line, "total flights");
            } else if (line.startsWith("- Flight ") && !inViolation) {
                final Matcher matcher = FLIGHT_STATUS_PATTERN.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException("Malformed flight status line: " + line);
                }
                flightStatuses.add(new FlightStatusSummary(
                        matcher.group(1),
                        Integer.parseInt(matcher.group(2)),
                        matcher.group(3)));
            } else if (line.startsWith("Violation #")) {
                if (inViolation) {
                    violations.add(buildViolation(violationTimestamp, violationFlightA, violationFlightB,
                            violationPositionA, violationPositionB));
                }
                inViolation = true;
                violationTimestamp = null;
                violationFlightA = null;
                violationFlightB = null;
                violationPositionA = null;
                violationPositionB = null;
            } else if (inViolation && line.startsWith("- Timestamp:")) {
                violationTimestamp = afterColon(line);
            } else if (inViolation && line.startsWith("- Flight A:")) {
                final ParsedFlightPosition parsed = parseFlightPosition(line);
                violationFlightA = parsed.flight();
                violationPositionA = parsed.position();
            } else if (inViolation && line.startsWith("- Flight B:")) {
                final ParsedFlightPosition parsed = parseFlightPosition(line);
                violationFlightB = parsed.flight();
                violationPositionB = parsed.position();
            } else if (line.startsWith("VALIDATION RESULT:")) {
                validationResult = ValidationResult.fromText(afterColon(line));
            }
        }

        if (inViolation) {
            violations.add(buildViolation(violationTimestamp, violationFlightA, violationFlightB,
                    violationPositionA, violationPositionB));
        }

        if (totalFlights == null) {
            throw new IllegalArgumentException("Missing total flights in simulation report.");
        }
        if (validationResult == null) {
            throw new IllegalArgumentException("Missing validation result in simulation report.");
        }

        return new SimulationReportSnapshot(sourcePath, timestamp, totalFlights, flightStatuses, violations, validationResult);
    }

    public static LocalDateTime timestampFromFileName(final Path sourcePath) {
        final String fileName = sourcePath.getFileName().toString();
        final Matcher matcher = TIMESTAMPED_REPORT_PATTERN.matcher(fileName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid timestamped simulation report filename: " + fileName);
        }
        return LocalDateTime.parse(matcher.group(1), FILE_TIMESTAMP);
    }

    private static SafetyViolationSummary buildViolation(final String timestamp,
                                                         final String flightA,
                                                         final String flightB,
                                                         final Position positionA,
                                                         final Position positionB) {
        return new SafetyViolationSummary(timestamp, flightA, flightB, positionA, positionB);
    }

    private static ParsedFlightPosition parseFlightPosition(final String line) {
        final Matcher matcher = FLIGHT_POSITION_PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Malformed violation position line: " + line);
        }
        return new ParsedFlightPosition(matcher.group(1), new Position(
                Double.parseDouble(matcher.group(2)),
                Double.parseDouble(matcher.group(3)),
                Double.parseDouble(matcher.group(4))));
    }

    private static int parseIntegerAfterColon(final String line, final String label) {
        try {
            return Integer.parseInt(afterColon(line));
        } catch (final NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + label + " value: " + line, ex);
        }
    }

    private static String afterColon(final String line) {
        final int index = line.indexOf(':');
        if (index < 0) {
            throw new IllegalArgumentException("Expected ':' in line: " + line);
        }
        return line.substring(index + 1).trim();
    }

    private record ParsedFlightPosition(String flight, Position position) {}
}
