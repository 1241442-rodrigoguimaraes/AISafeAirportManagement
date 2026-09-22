package eapli.alsafe.simulationreport.domain;

public record Position(double latitude, double longitude, double altitude) {

    public Position {
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude) || !Double.isFinite(altitude)) {
            throw new IllegalArgumentException("Position coordinates must be finite numbers.");
        }
    }
}
