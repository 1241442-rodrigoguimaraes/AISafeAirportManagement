package eapli.alsafe.antlr.domain;

import java.util.List;

/**
 * Represents a complete flight plan as parsed from the Flight DSL.
 * A flight plan aggregates global information (identifier, type, route, date/time, aircraft)
 * and one or more legs.
 */
public class FlightPlanDSL {

    public enum FlightType { REGULAR, CHARTER }

    private final String flightId;
    private final FlightType type;
    private final String routeId;
    private final String date;       // YYYY-MM-DD
    private final String time;       // HH:MM
    private final String aircraft;   // e.g. CS-TUA
    private final List<Leg> legs;

    public FlightPlanDSL(String flightId,
                         FlightType type,
                         String routeId,
                         String date,
                         String time,
                         String aircraft,
                         List<Leg> legs) {
        this.flightId = flightId;
        this.type = type;
        this.routeId = routeId;
        this.date = date;
        this.time = time;
        this.aircraft = aircraft;
        this.legs = legs;
    }

    public String getFlightId() { return flightId; }
    public FlightType getType() { return type; }
    public String getRouteId() { return routeId; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getAircraft() { return aircraft; }
    public List<Leg> getLegs() { return legs; }

    @Override
    public String toString() {
        return String.format("FlightPlan[id=%s, type=%s, route=%s, date=%s %s, aircraft=%s, legs=%d]",
                flightId, type, routeId, date, time, aircraft, legs.size());
    }
}
