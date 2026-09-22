package eapli.alsafe.antlr.domain;


import java.util.List;

/**
 * Represents a flight segment — a linear path between two 3D coordinate points.
 */
public class Segment {

    public enum SegmentMode { CLIMB, CRUISE, DESCEND }

    private final SegmentMode mode;
    private final Coords3D start;
    private final Coords3D end;
    private final List<Double> altitudeSlots;
    private final double widthMeters;
    private final double windDirectionDeg;
    private final double windSpeedMs;

    public Segment(SegmentMode mode,
                   Coords3D start,
                   Coords3D end,
                   List<Double> altitudeSlots,
                   double widthMeters,
                   double windDirectionDeg,
                   double windSpeedMs) {
        this.mode = mode;
        this.start = start;
        this.end = end;
        this.altitudeSlots = altitudeSlots;
        this.widthMeters = widthMeters;
        this.windDirectionDeg = windDirectionDeg;
        this.windSpeedMs = windSpeedMs;
    }

    public SegmentMode getMode() { return mode; }
    public Coords3D getStart() { return start; }
    public Coords3D getEnd() { return end; }
    public List<Double> getAltitudeSlots() { return altitudeSlots; }
    public double getWidthMeters() { return widthMeters; }
    public double getWindDirectionDeg() { return windDirectionDeg; }
    public double getWindSpeedMs() { return windSpeedMs; }

    @Override
    public String toString() {
        return String.format("Segment[%s: %s -> %s, wind=%.1fdeg %.1fm/s]",
                mode, start, end, windDirectionDeg, windSpeedMs);
    }
}
