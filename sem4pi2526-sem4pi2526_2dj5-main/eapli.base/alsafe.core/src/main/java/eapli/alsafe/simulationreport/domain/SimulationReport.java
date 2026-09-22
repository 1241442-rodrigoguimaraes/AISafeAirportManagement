package eapli.alsafe.simulationreport.domain;

import java.nio.file.Path;
import java.util.List;

public final class SimulationReport {

    private final Path sourcePath;
    private final int totalFlights;
    private final List<FlightStatusSummary> flightStatuses;
    private final List<SafetyViolationSummary> safetyViolations;
    private final ValidationResult validationResult;

    public SimulationReport(final Path sourcePath,
                            final int totalFlights,
                            final List<FlightStatusSummary> flightStatuses,
                            final List<SafetyViolationSummary> safetyViolations,
                            final ValidationResult validationResult) {
        if (sourcePath == null) {
            throw new IllegalArgumentException("Source report path is required.");
        }
        if (totalFlights < 0) {
            throw new IllegalArgumentException("Total flights cannot be negative.");
        }
        if (flightStatuses == null || safetyViolations == null) {
            throw new IllegalArgumentException("Report collections are required.");
        }
        if (validationResult == null) {
            throw new IllegalArgumentException("Validation result is required.");
        }
        if (flightStatuses.size() != totalFlights) {
            throw new IllegalArgumentException("Total flights must match the listed flight statuses.");
        }

        this.sourcePath = sourcePath;
        this.totalFlights = totalFlights;
        this.flightStatuses = List.copyOf(flightStatuses);
        this.safetyViolations = List.copyOf(safetyViolations);
        this.validationResult = validationResult;
    }

    public Path sourcePath() {
        return sourcePath;
    }

    public int totalFlights() {
        return totalFlights;
    }

    public List<FlightStatusSummary> flightStatuses() {
        return flightStatuses;
    }

    public List<SafetyViolationSummary> safetyViolations() {
        return safetyViolations;
    }

    public ValidationResult validationResult() {
        return validationResult;
    }

    public boolean passed() {
        return validationResult == ValidationResult.PASS;
    }
}
