package eapli.alsafe.simulationreport.domain;

public record SafetyViolationSummary(
        String timestamp,
        String flightA,
        String flightB,
        Position positionA,
        Position positionB) {

    public SafetyViolationSummary {
        if (timestamp == null || timestamp.isBlank()) {
            throw new IllegalArgumentException("Violation timestamp is required.");
        }
        if (flightA == null || flightA.isBlank() || flightB == null || flightB.isBlank()) {
            throw new IllegalArgumentException("Both violation flight designators are required.");
        }
        if (positionA == null || positionB == null) {
            throw new IllegalArgumentException("Both violation positions are required.");
        }

        timestamp = timestamp.trim();
        flightA = flightA.trim();
        flightB = flightB.trim();
    }
}
