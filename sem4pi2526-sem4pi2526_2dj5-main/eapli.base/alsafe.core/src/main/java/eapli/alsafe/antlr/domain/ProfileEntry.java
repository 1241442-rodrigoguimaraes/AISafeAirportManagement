package eapli.alsafe.antlr.domain;

public class ProfileEntry {

    private final double altitude;
    private final double speed;
    private final Double rateDescent;

    public ProfileEntry(double altitude, double speed) {
        this(altitude, speed, null);
    }

    public ProfileEntry(double altitude, double speed, Double rateDescent) {
        this.altitude = altitude;
        this.speed = speed;
        this.rateDescent = rateDescent;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getSpeed() {
        return speed;
    }

    public Double getRateDescent() {
        return rateDescent;
    }

    @Override
    public String toString() {
        if (rateDescent != null) {
            return String.format("ProfileEntry[alt=%.1f, speed=%.1f, rate=%.1f]", altitude, speed, rateDescent);
        }
        return String.format("ProfileEntry[alt=%.1f, speed=%.1f]", altitude, speed);
    }
}
