package eapli.alsafe.simulationreport.domain;

public record FlightStatusSummary(String designator, int id, String status) {

    public FlightStatusSummary {
        if (designator == null || designator.isBlank()) {
            throw new IllegalArgumentException("Flight designator is required.");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Flight status is required.");
        }

        designator = designator.trim();
        status = status.trim();
    }
}
