package eapli.alsafe.antlr.domain;

/**
 * Represents a 3D coordinate point (latitude, longitude, altitude).
 */
public class Coords3D {

    private final double latitude;
    private final double longitude;
    private final double altitudeMeters;

    public Coords3D(double latitude, double longitude, double altitudeMeters) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitudeMeters = altitudeMeters;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getAltitudeMeters() { return altitudeMeters; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coords3D)) return false;
        Coords3D c = (Coords3D) o;
        return Double.compare(c.latitude, latitude) == 0
                && Double.compare(c.longitude, longitude) == 0
                && Double.compare(c.altitudeMeters, altitudeMeters) == 0;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(latitude, longitude, altitudeMeters);
    }

    @Override
    public String toString() {
        return String.format("(lat=%.6f, lon=%.6f, alt=%.1fm)", latitude, longitude, altitudeMeters);
    }
}