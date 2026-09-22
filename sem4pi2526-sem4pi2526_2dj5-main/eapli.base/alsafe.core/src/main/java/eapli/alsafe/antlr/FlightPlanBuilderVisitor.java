package eapli.alsafe.antlr;


import eapli.alsafe.antlr.domain.*;
import eapli.alsafe.dsl.FlightParser;
import eapli.alsafe.dsl.FlightParserBaseVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor that traverses the ANTLR parse tree and builds a list of {@link FlightPlanDSL}
 * domain objects — one per {@code flight} block found in the file.
 *
 * <p>Usage:
 * <pre>
 *   FlightPlanBuilderVisitor visitor = new FlightPlanBuilderVisitor();
 *   visitor.visit(parseTree);
 *   List&lt;FlightPlan&gt; plans = visitor.getFlightPlans();
 * </pre>
 *
 * <p>Unlike a Listener (where ANTLR drives the walk), here we explicitly call
 * {@code visit()} on each child we care about, giving us full control over
 * which nodes are visited and allowing us to return typed values up the call stack.
 */
public class FlightPlanBuilderVisitor extends FlightParserBaseVisitor<Object> {

    /** All flight plans found in the parsed file. */
    private final List<FlightPlanDSL> flightPlanDSLS = new ArrayList<>();

    public List<FlightPlanDSL> getFlightPlans() {
        return flightPlanDSLS;
    }

    // -------------------------------------------------------------------------
    // start rule — visit every flightBlock inside the file
    // -------------------------------------------------------------------------

    @Override
    public Object visitStart(FlightParser.StartContext ctx) {
        for (FlightParser.FlightBlockContext fb : ctx.flightBlock()) {
            flightPlanDSLS.add((FlightPlanDSL) visit(fb));
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // flightBlock — builds one FlightPlan
    // -------------------------------------------------------------------------

    @Override
    public Object visitFlightBlock(FlightParser.FlightBlockContext ctx) {
        String flightId = ctx.FLIGHT_DESIGNATOR(0).getText();
        String routeId  = ctx.FLIGHT_DESIGNATOR(1).getText();
        String date     = ctx.DATE_LIT().getText();
        String time     = ctx.TIME_LIT().getText();
        String aircraft = ctx.AIRCRAFT_REG().getText();

        FlightPlanDSL.FlightType type = (FlightPlanDSL.FlightType) visit(ctx.flightType());

        List<Leg> legs = new ArrayList<>();
        for (FlightParser.LegBlockContext legCtx : ctx.legBlock()) {
            legs.add((Leg) visit(legCtx));
        }

        return new FlightPlanDSL(flightId, type, routeId, date, time, aircraft, legs);
    }

    @Override
    public Object visitFlightType(FlightParser.FlightTypeContext ctx) {
        if (ctx.REGULAR() != null) return FlightPlanDSL.FlightType.REGULAR;
        return FlightPlanDSL.FlightType.CHARTER;
    }

    // -------------------------------------------------------------------------
    // legBlock — builds one Leg
    // -------------------------------------------------------------------------

    @Override
    public Object visitLegBlock(FlightParser.LegBlockContext ctx) {
        String departure = ctx.airportCode(0).getText();
        String arrival   = ctx.airportCode(1).getText();

        FuelInfo fuel = (FuelInfo) visit(ctx.fuelInfo());
        FlightProfile profile = (FlightProfile) visit(ctx.flightProfile());

        List<Segment> segments = new ArrayList<>();
        for (FlightParser.SegmentBlockContext segCtx : ctx.segmentBlock()) {
            segments.add((Segment) visit(segCtx));
        }

        return new Leg(departure, arrival, fuel, profile, segments);
    }

    // -------------------------------------------------------------------------
    // fuelInfo
    // -------------------------------------------------------------------------

    @Override
    public Object visitFuelInfo(FlightParser.FuelInfoContext ctx) {
        double quantity = Double.parseDouble(ctx.NUMBER().getText());
        FuelInfo.FuelUnit unit = (FuelInfo.FuelUnit) visit(ctx.fuelUnit());
        return new FuelInfo(quantity, unit);
    }

    @Override
    public Object visitFuelUnit(FlightParser.FuelUnitContext ctx) {
        if (ctx.UNIT_KG() != null) return FuelInfo.FuelUnit.KG;
        return FuelInfo.FuelUnit.L;
    }

    // -------------------------------------------------------------------------
    // segmentBlock — builds one Segment
    // -------------------------------------------------------------------------

    @Override
    public Object visitSegmentBlock(FlightParser.SegmentBlockContext ctx) {
        Segment.SegmentMode mode = (Segment.SegmentMode) visit(ctx.segmentMode());

        Coords3D start = (Coords3D) visit(ctx.coords3d(0));
        Coords3D end   = (Coords3D) visit(ctx.coords3d(1));

        List<Double> slots = (List<Double>) visit(ctx.altitudeSlotList());

        // WIDTH: NUMBER UNIT_M  — ctx.NUMBER(0) is the width value
        double width = Double.parseDouble(ctx.NUMBER(0).getText());

        // WIND: NUMBER UNIT_DEG NUMBER UNIT_MS — ctx.NUMBER(1) = direction, ctx.NUMBER(2) = speed
        double windDir   = Double.parseDouble(ctx.NUMBER(1).getText());
        double windSpeed = Double.parseDouble(ctx.NUMBER(2).getText());

        return new Segment(mode, start, end, slots, width, windDir, windSpeed);
    }

    @Override
    public Object visitSegmentMode(FlightParser.SegmentModeContext ctx) {
        if (ctx.CLIMB()   != null) return Segment.SegmentMode.CLIMB;
        if (ctx.CRUISE()  != null) return Segment.SegmentMode.CRUISE;
        return Segment.SegmentMode.DESCEND;
    }

    // -------------------------------------------------------------------------
    // coords3d
    // -------------------------------------------------------------------------

    @Override
    public Object visitCoords3d(FlightParser.Coords3dContext ctx) {
        double lat = parseSignedNumber(ctx.signedNumber(0));
        double lon = parseSignedNumber(ctx.signedNumber(1));
        // altitude: NUMBER UNIT_M — ctx.NUMBER() is the altitude
        double alt = Double.parseDouble(ctx.NUMBER().getText());
        return new Coords3D(lat, lon, alt);
    }

    // -------------------------------------------------------------------------
    // altitudeSlotList
    // -------------------------------------------------------------------------

    @Override
    public Object visitAltitudeSlotList(FlightParser.AltitudeSlotListContext ctx) {
        List<Double> slots = new ArrayList<>();
        for (var numToken : ctx.NUMBER()) {
            slots.add(Double.parseDouble(numToken.getText()));
        }
        return slots;
    }

    // -------------------------------------------------------------------------
    // flightProfile — builds one FlightProfile
    // -------------------------------------------------------------------------

    @Override
    public Object visitFlightProfile(FlightParser.FlightProfileContext ctx) {
        @SuppressWarnings("unchecked")
        List<ProfileEntry> climbEntries = (List<ProfileEntry>) visit(ctx.climbProfile());

        double[] cruiseInfo = (double[]) visit(ctx.cruiseProfile());
        double cruiseSpeed = cruiseInfo[0];
        String cruiseUnit  = cruiseInfo.length > 1 ? ctx.cruiseProfile().speedValue().speedUnit().getText() : "knots";

        @SuppressWarnings("unchecked")
        List<ProfileEntry> descendEntries = (List<ProfileEntry>) visit(ctx.descendProfile());

        return new FlightProfile(climbEntries, cruiseSpeed, cruiseUnit, descendEntries);
    }

    @Override
    public Object visitClimbProfile(FlightParser.ClimbProfileContext ctx) {
        List<ProfileEntry> entries = new ArrayList<>();
        for (FlightParser.ClimbEntryContext entryCtx : ctx.climbEntry()) {
            entries.add((ProfileEntry) visit(entryCtx));
        }
        return entries;
    }

    @Override
    public Object visitClimbEntry(FlightParser.ClimbEntryContext ctx) {
        double altitude = (double) visit(ctx.altitudeValue());
        double speed = (double) visit(ctx.speedValue());
        return new ProfileEntry(altitude, speed);
    }

    @Override
    public Object visitCruiseProfile(FlightParser.CruiseProfileContext ctx) {
        double speed = (double) visit(ctx.speedValue());
        return new double[]{ speed };
    }

    @Override
    public Object visitDescendProfile(FlightParser.DescendProfileContext ctx) {
        List<ProfileEntry> entries = new ArrayList<>();
        for (FlightParser.DescendEntryContext entryCtx : ctx.descendEntry()) {
            entries.add((ProfileEntry) visit(entryCtx));
        }
        return entries;
    }

    @Override
    public Object visitDescendEntry(FlightParser.DescendEntryContext ctx) {
        double altitude = (double) visit(ctx.altitudeValue());
        double speed = (double) visit(ctx.speedValue());
        double rate = (double) visit(ctx.rateDescentValue());
        return new ProfileEntry(altitude, speed, rate);
    }

    @Override
    public Object visitAltitudeValue(FlightParser.AltitudeValueContext ctx) {
        return Double.parseDouble(ctx.NUMBER().getText());
    }

    @Override
    public Object visitSpeedValue(FlightParser.SpeedValueContext ctx) {
        return Double.parseDouble(ctx.NUMBER().getText());
    }

    @Override
    public Object visitRateDescentValue(FlightParser.RateDescentValueContext ctx) {
        return parseSignedNumber(ctx.signedNumber());
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private double parseSignedNumber(FlightParser.SignedNumberContext ctx) {
        if (ctx.NEG_NUMBER() != null) return Double.parseDouble(ctx.NEG_NUMBER().getText());
        return Double.parseDouble(ctx.NUMBER().getText());
    }
}