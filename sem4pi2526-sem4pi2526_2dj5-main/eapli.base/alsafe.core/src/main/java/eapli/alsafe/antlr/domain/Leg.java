package eapli.alsafe.antlr.domain;

import java.util.List;

/**
 * Represents a single flight leg — one non-stop journey between two airports.
 */
public class Leg {

    private final String departureAirport;
    private final String arrivalAirport;
    private final FuelInfo fuel;
    private final FlightProfile profile;
    private final List<Segment> segments;

    public Leg(String departureAirport,
               String arrivalAirport,
               FuelInfo fuel,
               FlightProfile profile,
               List<Segment> segments) {
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.fuel = fuel;
        this.profile = profile;
        this.segments = segments;
    }

    public String getDepartureAirport() { return departureAirport; }
    public String getArrivalAirport() { return arrivalAirport; }
    public FuelInfo getFuel() { return fuel; }
    public FlightProfile getProfile() { return profile; }
    public List<Segment> getSegments() { return segments; }

    @Override
    public String toString() {
        return String.format("Leg[%s -> %s, fuel=%s, profile=%s, segments=%d]",
                departureAirport, arrivalAirport, fuel, profile, segments.size());
    }
}
