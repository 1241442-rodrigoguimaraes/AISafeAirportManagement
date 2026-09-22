package eapli.alsafe.antlr.domain;

import java.util.List;

public class FlightProfile {

    private final List<ProfileEntry> climbEntries;
    private final double cruiseSpeed;
    private final String cruiseSpeedUnit;
    private final List<ProfileEntry> descendEntries;

    public FlightProfile(List<ProfileEntry> climbEntries,
                         double cruiseSpeed,
                         String cruiseSpeedUnit,
                         List<ProfileEntry> descendEntries) {
        this.climbEntries = climbEntries;
        this.cruiseSpeed = cruiseSpeed;
        this.cruiseSpeedUnit = cruiseSpeedUnit;
        this.descendEntries = descendEntries;
    }

    public List<ProfileEntry> getClimbEntries() {
        return climbEntries;
    }

    public double getCruiseSpeed() {
        return cruiseSpeed;
    }

    public String getCruiseSpeedUnit() {
        return cruiseSpeedUnit;
    }

    public List<ProfileEntry> getDescendEntries() {
        return descendEntries;
    }

    @Override
    public String toString() {
        return String.format("FlightProfile[climb=%d entries, cruise=%.1f %s, descend=%d entries]",
                climbEntries.size(), cruiseSpeed, cruiseSpeedUnit, descendEntries.size());
    }
}
