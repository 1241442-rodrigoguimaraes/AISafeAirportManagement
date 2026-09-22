package eapli.alsafe.antlr;

import eapli.alsafe.dsl.FlightParserBaseListener;
import eapli.alsafe.dsl.FlightParser;

/**
 * Listener that prints a structured summary of the flight plan file as
 * ANTLR automatically walks the parse tree.
 *
 * <p>Unlike the Visitor, we do NOT control the traversal — ANTLR calls
 * {@code enterXxx()} when it enters a node and {@code exitXxx()} when it
 * leaves. We simply react to those events to produce a human-readable log.
 *
 * <p>Usage:
 * <pre>
 *   FlightPlanSummaryListener listener = new FlightPlanSummaryListener();
 *   ParseTreeWalker.DEFAULT.walk(listener, parseTree);
 *   // summary is printed to stdout automatically
 * </pre>
 */
public class FlightPlanSummaryListener extends FlightParserBaseListener {

    private int legIndex    = 0;
    private int segIndex    = 0;
    private int flightIndex = 0;

    // -------------------------------------------------------------------------
    // flight block
    // -------------------------------------------------------------------------

    @Override
    public void enterFlightBlock(FlightParser.FlightBlockContext ctx) {
        flightIndex++;
        legIndex = 0;
        System.out.println("┌─────────────────────────────────────────────");
        System.out.printf ("│ FLIGHT #%d%n", flightIndex);
        System.out.printf ("│   ID       : %s%n", ctx.FLIGHT_DESIGNATOR(0).getText());
        System.out.printf ("│   Route    : %s%n", ctx.FLIGHT_DESIGNATOR(1).getText());
        System.out.printf ("│   Date     : %s  Time: %s%n",
                ctx.DATE_LIT().getText(), ctx.TIME_LIT().getText());
        System.out.printf ("│   Aircraft : %s%n", ctx.AIRCRAFT_REG().getText());
    }

    @Override
    public void exitFlightBlock(FlightParser.FlightBlockContext ctx) {
        System.out.println("└─────────────────────────────────────────────");
        System.out.println();
    }

    @Override
    public void enterFlightType(FlightParser.FlightTypeContext ctx) {
        System.out.printf("│   Type     : %s%n", ctx.getText().toUpperCase());
    }

    // -------------------------------------------------------------------------
    // leg block
    // -------------------------------------------------------------------------

    @Override
    public void enterLegBlock(FlightParser.LegBlockContext ctx) {
        legIndex++;
        segIndex = 0;
        System.out.printf("│%n│   ┌── LEG #%d%n", legIndex);
        System.out.printf("│   │   Departure : %s%n", ctx.airportCode(0).getText());
        System.out.printf("│   │   Arrival   : %s%n", ctx.airportCode(1).getText());
    }

    @Override
    public void exitLegBlock(FlightParser.LegBlockContext ctx) {
        System.out.println("│   └──────────────────────────────");
    }

    // -------------------------------------------------------------------------
    // fuel
    // -------------------------------------------------------------------------

    @Override
    public void enterFuelInfo(FlightParser.FuelInfoContext ctx) {
        System.out.printf("│   │   Fuel      : %s %s%n",
                ctx.NUMBER().getText(),
                ctx.fuelUnit().getText().toUpperCase());
    }

    // -------------------------------------------------------------------------
    // segment block
    // -------------------------------------------------------------------------

    @Override
    public void enterSegmentBlock(FlightParser.SegmentBlockContext ctx) {
        segIndex++;
        System.out.printf("│   │%n│   │   ┌── SEGMENT #%d  [mode: %s]%n",
                segIndex, ctx.segmentMode().getText().toUpperCase());

        FlightParser.Coords3dContext s = ctx.coords3d(0);
        FlightParser.Coords3dContext e = ctx.coords3d(1);
        System.out.printf("│   │   │   Start : lat=%s  lon=%s  alt=%s m%n",
                s.signedNumber(0).getText(),
                s.signedNumber(1).getText(),
                s.NUMBER().getText());
        System.out.printf("│   │   │   End   : lat=%s  lon=%s  alt=%s m%n",
                e.signedNumber(0).getText(),
                e.signedNumber(1).getText(),
                e.NUMBER().getText());
        System.out.printf("│   │   │   Width : %s m%n", ctx.NUMBER(0).getText());
        System.out.printf("│   │   │   Wind  : %s deg  %s m/s%n",
                ctx.NUMBER(1).getText(), ctx.NUMBER(2).getText());
        System.out.printf("│   │   │   Slots : %s%n",
                ctx.altitudeSlotList().getText());
    }

    @Override
    public void exitSegmentBlock(FlightParser.SegmentBlockContext ctx) {
        System.out.println("│   │   └──────────────────────────────");
    }

    // -------------------------------------------------------------------------
    // profile (climb / cruise / descend) — brief mention
    // -------------------------------------------------------------------------

    @Override
    public void enterClimbProfile(FlightParser.ClimbProfileContext ctx) {
        System.out.printf("│   │   Profile/Climb   : %d entry(ies)%n",
                ctx.climbEntry().size());
    }

    @Override
    public void enterCruiseProfile(FlightParser.CruiseProfileContext ctx) {
        System.out.printf("│   │   Profile/Cruise  : speed=%s %s%n",
                ctx.speedValue().NUMBER().getText(),
                ctx.speedValue().speedUnit().getText());
    }

    @Override
    public void enterDescendProfile(FlightParser.DescendProfileContext ctx) {
        System.out.printf("│   │   Profile/Descend : %d entry(ies)%n",
                ctx.descendEntry().size());
    }
}