package eapli.alsafe.simulationreport.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimulationReportParser {

    private static final Pattern FLIGHT_PATTERN = Pattern.compile("^Flight\\s+(.+)\\s+\\(id:\\s*(-?\\d+)\\)$");
    private static final Pattern POSITION_PATTERN = Pattern.compile(".*Lat=([-+]?\\d+(?:\\.\\d+)?),\\s*Lon=([-+]?\\d+(?:\\.\\d+)?),\\s*Alt=([-+]?\\d+(?:\\.\\d+)?).*");

    public SimulationReport parse(final Path sourcePath) throws IOException {
        if (sourcePath == null) {
            throw new IllegalArgumentException("Source report path is required.");
        }
        if (!Files.exists(sourcePath)) {
            throw new IOException("Simulation report file not found: " + sourcePath);
        }

        return parse(sourcePath, Files.readAllLines(sourcePath));
    }

    public SimulationReport parse(final Path sourcePath, final List<String> lines) {
        if (lines == null) {
            throw new IllegalArgumentException("Report lines are required.");
        }

        Integer totalFlights = null;
        Integer declaredViolations = null;
        ValidationResult validationResult = null;
        final List<FlightStatusSummary> flights = new ArrayList<>();
        final List<SafetyViolationSummary> violations = new ArrayList<>();

        String currentFlightDesignator = null;
        int currentFlightId = 0;

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
            } else if (line.startsWith("Total safety violations:")) {
                declaredViolations = parseIntegerAfterColon(line, "total safety violations");
            } else if (line.startsWith("Validation result:")) {
                validationResult = ValidationResult.fromText(afterColon(line));
            } else if (line.startsWith("Flight ") && !inViolation) {
                final Matcher matcher = FLIGHT_PATTERN.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException("Malformed flight status line: " + line);
                }
                currentFlightDesignator = matcher.group(1);
                currentFlightId = Integer.parseInt(matcher.group(2));
            } else if (line.startsWith("- Status:") && currentFlightDesignator != null && !inViolation) {
                flights.add(new FlightStatusSummary(currentFlightDesignator, currentFlightId, afterColon(line)));
                currentFlightDesignator = null;
            } else if (line.startsWith("Event ")) {
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
            } else if (inViolation && line.startsWith("- Simulation timestamp:")) {
                violationTimestamp = afterColon(line);
            } else if (inViolation && line.startsWith("- Flights:")) {
                final String flightsText = afterColon(line);
                final String[] parts = flightsText.split("\\s+and\\s+", 2);
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Malformed violation flights line: " + line);
                }
                violationFlightA = parts[0];
                violationFlightB = parts[1];
            } else if (inViolation && line.startsWith("- Flight A position:")) {
                violationPositionA = parsePosition(line);
            } else if (inViolation && line.startsWith("- Flight B position:")) {
                violationPositionB = parsePosition(line);
            }
        }

        if (inViolation) {
            violations.add(buildViolation(violationTimestamp, violationFlightA, violationFlightB,
                    violationPositionA, violationPositionB));
        }

        if (totalFlights == null) {
            throw new IllegalArgumentException("Missing total flights in simulation report.");
        }
        if (declaredViolations == null) {
            throw new IllegalArgumentException("Missing total safety violations in simulation report.");
        }
        if (validationResult == null) {
            throw new IllegalArgumentException("Missing validation result in simulation report.");
        }
        if (declaredViolations != violations.size()) {
            throw new IllegalArgumentException("Declared safety violations do not match listed events.");
        }

        return new SimulationReport(sourcePath, totalFlights, flights, violations, validationResult);
    }

    private static SafetyViolationSummary buildViolation(final String timestamp,
                                                         final String flightA,
                                                         final String flightB,
                                                         final Position positionA,
                                                         final Position positionB) {
        return new SafetyViolationSummary(timestamp, flightA, flightB, positionA, positionB);
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

    private static Position parsePosition(final String line) {
        final Matcher matcher = POSITION_PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Malformed position line: " + line);
        }
        return new Position(
                Double.parseDouble(matcher.group(1)),
                Double.parseDouble(matcher.group(2)),
                Double.parseDouble(matcher.group(3)));
    }
}
