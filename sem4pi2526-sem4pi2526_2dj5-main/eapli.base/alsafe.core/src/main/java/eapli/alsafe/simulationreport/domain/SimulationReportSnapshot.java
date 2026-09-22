package eapli.alsafe.simulationreport.domain;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public final class SimulationReportSnapshot {

    private final Path sourcePath;
    private final LocalDateTime generatedAt;
    private final int totalFlights;
    private final List<FlightStatusSummary> flightStatuses;
    private final List<SafetyViolationSummary> safetyViolations;
    private final ValidationResult validationResult;

    public SimulationReportSnapshot(final Path sourcePath,
                                    final LocalDateTime generatedAt,
                                    final int totalFlights,
                                    final List<FlightStatusSummary> flightStatuses,
                                    final List<SafetyViolationSummary> safetyViolations,
                                    final ValidationResult validationResult) {
        if (sourcePath == null) {
            throw new IllegalArgumentException("Source report path is required.");
        }
        if (generatedAt == null) {
            throw new IllegalArgumentException("Report timestamp is required.");
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
            throw new IllegalArgumentException("Total flights must match listed flight entries.");
        }

        this.sourcePath = sourcePath;
        this.generatedAt = generatedAt;
        this.totalFlights = totalFlights;
        this.flightStatuses = List.copyOf(flightStatuses);
        this.safetyViolations = List.copyOf(safetyViolations);
        this.validationResult = validationResult;
    }

    public Path sourcePath() {
        return sourcePath;
    }

    public LocalDateTime generatedAt() {
        return generatedAt;
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
}
