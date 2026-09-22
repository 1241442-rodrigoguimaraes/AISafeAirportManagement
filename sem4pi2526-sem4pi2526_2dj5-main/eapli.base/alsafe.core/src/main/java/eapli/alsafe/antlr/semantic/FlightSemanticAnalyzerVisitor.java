package eapli.alsafe.antlr.semantic;

import eapli.alsafe.antlr.SemanticError;
import eapli.alsafe.antlr.domain.Coords3D;
import eapli.alsafe.dsl.FlightParser;
import eapli.alsafe.dsl.FlightParserBaseVisitor;
import org.antlr.v4.runtime.Token;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Semantic analysis over the ANTLR parse tree. It fills a symbol table and
 * computes synthesized attributes from grammar nodes before domain objects are built.
 */
public class FlightSemanticAnalyzerVisitor extends FlightParserBaseVisitor<Object> {

    private final SymbolTable symbols = new SymbolTable();
    private final List<SemanticError> errors = new ArrayList<>();
    private String currentFlightId = "<file>";

    public List<SemanticError> getErrors() {
        return errors;
    }

    public SymbolTable symbolTable() {
        return symbols;
    }

    @Override
    public Object visitStart(FlightParser.StartContext ctx) {
        for (FlightParser.FlightBlockContext flight : ctx.flightBlock()) {
            visit(flight);
        }
        return null;
    }

    @Override
    public Object visitFlightBlock(FlightParser.FlightBlockContext ctx) {
        Token flightToken = ctx.FLIGHT_DESIGNATOR(0).getSymbol();
        String previousFlightId = currentFlightId;
        currentFlightId = ctx.FLIGHT_DESIGNATOR(0).getText();

        Symbol flight = new Symbol(currentFlightId, SymbolKind.FLIGHT,
                flightToken.getLine(), flightToken.getCharPositionInLine())
                .withAttribute("type", ctx.flightType().getText())
                .withAttribute("route", ctx.FLIGHT_DESIGNATOR(1).getText())
                .withAttribute("date", ctx.DATE_LIT().getText())
                .withAttribute("time", ctx.TIME_LIT().getText())
                .withAttribute("aircraft", ctx.AIRCRAFT_REG().getText());

        if (!symbols.insertCurrentScope(flight)) {
            error(flightToken, "Duplicate flight identifier '" + currentFlightId + "' in file.");
        }

        validateDate(ctx.DATE_LIT().getSymbol(), ctx.DATE_LIT().getText());
        validateTime(ctx.TIME_LIT().getSymbol(), ctx.TIME_LIT().getText());

        if (ctx.legBlock().isEmpty()) {
            error(flightToken, "Flight '" + currentFlightId + "' must contain at least one leg.");
        }

        symbols.enterScope();
        insertAppliedSymbol(ctx.FLIGHT_DESIGNATOR(1).getSymbol(), SymbolKind.ROUTE);
        insertAppliedSymbol(ctx.AIRCRAFT_REG().getSymbol(), SymbolKind.AIRCRAFT);

        List<LegAttributes> legs = new ArrayList<>();
        for (FlightParser.LegBlockContext legCtx : ctx.legBlock()) {
            legs.add((LegAttributes) visit(legCtx));
        }

        validateLegSequence(legs);
        validateRepeatedAirports(legs);
        symbols.exitScope();
        currentFlightId = previousFlightId;
        return null;
    }

    @Override
    public Object visitLegBlock(FlightParser.LegBlockContext ctx) {
        String departure = ctx.airportCode(0).getText();
        String arrival = ctx.airportCode(1).getText();

        insertAppliedSymbol(ctx.airportCode(0).IATA_CODE().getSymbol(), SymbolKind.AIRPORT);
        insertAppliedSymbol(ctx.airportCode(1).IATA_CODE().getSymbol(), SymbolKind.AIRPORT);

        if (departure.equals(arrival)) {
            error(ctx.airportCode(1).IATA_CODE().getSymbol(),
                    "Leg departure and arrival airport are the same ('" + departure + "').");
        }

        double fuelQuantity = number(ctx.fuelInfo().NUMBER().getText());
        if (fuelQuantity <= 0) {
            error(ctx.fuelInfo().NUMBER().getSymbol(),
                    "Fuel quantity must be strictly positive, got " + fuelQuantity + ".");
        }

        visit(ctx.flightProfile());
        if (ctx.segmentBlock().isEmpty()) {
            error(ctx.airportCode(0).IATA_CODE().getSymbol(),
                    "Leg " + departure + " -> " + arrival + " must contain at least one segment.");
        }
        for (FlightParser.SegmentBlockContext segmentCtx : ctx.segmentBlock()) {
            visit(segmentCtx);
        }

        Symbol leg = new Symbol("leg:" + departure + ":" + arrival + ":" + ctx.start.getLine(),
                SymbolKind.LEG, ctx.start.getLine(), ctx.start.getCharPositionInLine())
                .withAttribute("departure", departure)
                .withAttribute("arrival", arrival);
        symbols.insertCurrentScope(leg);

        return new LegAttributes(departure, arrival);
    }

    @Override
    public Object visitSegmentBlock(FlightParser.SegmentBlockContext ctx) {
        Coords3D start = (Coords3D) visit(ctx.coords3d(0));
        Coords3D end = (Coords3D) visit(ctx.coords3d(1));
        List<Double> slots = (List<Double>) visit(ctx.altitudeSlotList());

        if (start.equals(end)) {
            error(ctx.coords3d(1).start, "Segment start and end coordinates are identical: " + start + ".");
        }
        if (start.getAltitudeMeters() < 0) {
            error(ctx.coords3d(0).start, "Segment start altitude cannot be negative.");
        }
        if (end.getAltitudeMeters() < 0) {
            error(ctx.coords3d(1).start, "Segment end altitude cannot be negative.");
        }

        if (slots.isEmpty()) {
            error(ctx.altitudeSlotList().start, "Altitude slots list is empty.");
        }
        for (Double slot : slots) {
            if (slot <= 0) {
                error(ctx.altitudeSlotList().start,
                        "Altitude slot value must be positive, got " + slot + ".");
            }
        }

        double width = number(ctx.NUMBER(0).getText());
        double windDirection = number(ctx.NUMBER(1).getText());
        double windSpeed = number(ctx.NUMBER(2).getText());

        if (width <= 0) {
            error(ctx.NUMBER(0).getSymbol(), "Segment width must be positive, got " + width + " m.");
        }
        if (windDirection < 0 || windDirection > 360) {
            error(ctx.NUMBER(1).getSymbol(),
                    "Wind direction must be between 0 and 360 degrees, got " + windDirection + ".");
        }
        if (windSpeed <= 0) {
            error(ctx.NUMBER(2).getSymbol(), "Wind speed must be strictly positive, got " + windSpeed + " m/s.");
        }

        Symbol segment = new Symbol("segment:" + ctx.start.getLine() + ":" + ctx.start.getCharPositionInLine(),
                SymbolKind.SEGMENT, ctx.start.getLine(), ctx.start.getCharPositionInLine())
                .withAttribute("mode", ctx.segmentMode().getText())
                .withAttribute("start", start)
                .withAttribute("end", end)
                .withAttribute("altitudeSlots", slots)
                .withAttribute("width", width)
                .withAttribute("windDirection", windDirection)
                .withAttribute("windSpeed", windSpeed);
        symbols.insertCurrentScope(segment);
        return null;
    }

    @Override
    public Object visitClimbProfile(FlightParser.ClimbProfileContext ctx) {
        List<Double> altitudes = new ArrayList<>();
        for (FlightParser.ClimbEntryContext entry : ctx.climbEntry()) {
            ProfileEntryAttributes attributes = (ProfileEntryAttributes) visit(entry);
            altitudes.add(attributes.altitude);
        }
        validateAscending(ctx.CLIMB().getSymbol(), altitudes, "Climb profile altitudes must be in ascending order.");
        return null;
    }

    @Override
    public Object visitClimbEntry(FlightParser.ClimbEntryContext ctx) {
        double altitude = (Double) visit(ctx.altitudeValue());
        double speed = (Double) visit(ctx.speedValue());
        return new ProfileEntryAttributes(altitude, speed, null);
    }

    @Override
    public Object visitCruiseProfile(FlightParser.CruiseProfileContext ctx) {
        visit(ctx.speedValue());
        return null;
    }

    @Override
    public Object visitDescendProfile(FlightParser.DescendProfileContext ctx) {
        List<Double> altitudes = new ArrayList<>();
        for (FlightParser.DescendEntryContext entry : ctx.descendEntry()) {
            ProfileEntryAttributes attributes = (ProfileEntryAttributes) visit(entry);
            altitudes.add(attributes.altitude);
        }
        validateDescending(ctx.DESCEND().getSymbol(), altitudes, "Descend profile altitudes must be in descending order.");
        return null;
    }

    @Override
    public Object visitDescendEntry(FlightParser.DescendEntryContext ctx) {
        double altitude = (Double) visit(ctx.altitudeValue());
        double speed = (Double) visit(ctx.speedValue());
        double rate = (Double) visit(ctx.rateDescentValue());
        return new ProfileEntryAttributes(altitude, speed, rate);
    }

    @Override
    public Object visitAltitudeValue(FlightParser.AltitudeValueContext ctx) {
        double altitude = number(ctx.NUMBER().getText());
        if (altitude < 0) {
            error(ctx.NUMBER().getSymbol(), "Altitude value must be non-negative, got " + altitude + ".");
        }
        return altitude;
    }

    @Override
    public Object visitSpeedValue(FlightParser.SpeedValueContext ctx) {
        double speed = number(ctx.NUMBER().getText());
        if (speed <= 0) {
            error(ctx.NUMBER().getSymbol(), "Speed value must be strictly positive, got " + speed + ".");
        }
        return speed;
    }

    @Override
    public Object visitRateDescentValue(FlightParser.RateDescentValueContext ctx) {
        double rate = signedNumber(ctx.signedNumber());
        if (rate >= 0) {
            error(ctx.signedNumber().start, "Rate of descent must be negative, got " + rate + ".");
        }
        return rate;
    }

    @Override
    public Object visitCoords3d(FlightParser.Coords3dContext ctx) {
        double latitude = signedNumber(ctx.signedNumber(0));
        double longitude = signedNumber(ctx.signedNumber(1));
        double altitude = number(ctx.NUMBER().getText());
        return new Coords3D(latitude, longitude, altitude);
    }

    @Override
    public Object visitAltitudeSlotList(FlightParser.AltitudeSlotListContext ctx) {
        List<Double> slots = new ArrayList<>();
        for (int i = 0; i < ctx.NUMBER().size(); i++) {
            slots.add(number(ctx.NUMBER(i).getText()));
        }
        return slots;
    }

    private void insertAppliedSymbol(Token token, SymbolKind kind) {
        Symbol symbol = new Symbol(token.getText(), kind, token.getLine(), token.getCharPositionInLine());
        symbols.insertCurrentScope(symbol);
    }

    private void validateDate(Token token, String date) {
        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            error(token, "Date '" + date + "' is not a valid calendar date (expected YYYY-MM-DD).");
        }
    }

    private void validateTime(Token token, String time) {
        try {
            LocalTime.parse(time);
        } catch (DateTimeParseException e) {
            error(token, "Time '" + time + "' is not a valid time (expected HH:MM).");
        }
    }

    private void validateLegSequence(List<LegAttributes> legs) {
        for (int i = 0; i < legs.size() - 1; i++) {
            LegAttributes current = legs.get(i);
            LegAttributes next = legs.get(i + 1);
            if (!current.arrival.equals(next.departure)) {
                errors.add(new SemanticError(currentFlightId,
                        String.format("Leg %d arrival airport '%s' does not match Leg %d departure airport '%s'.",
                                i + 1, current.arrival, i + 2, next.departure)));
            }
        }
    }

    private void validateRepeatedAirports(List<LegAttributes> legs) {
        Set<String> visited = new LinkedHashSet<>();
        for (int i = 0; i < legs.size(); i++) {
            String departure = legs.get(i).departure;
            if (!visited.add(departure)) {
                errors.add(new SemanticError(currentFlightId,
                        "Airport '" + departure + "' is visited more than once (at leg " + (i + 1) + " departure)."));
            }
        }
        if (!legs.isEmpty()) {
            String finalArrival = legs.get(legs.size() - 1).arrival;
            if (visited.contains(finalArrival)) {
                errors.add(new SemanticError(currentFlightId,
                        "Airport '" + finalArrival + "' is visited more than once (final arrival)."));
            }
        }
    }

    private void validateAscending(Token token, List<Double> values, String message) {
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) < values.get(i - 1)) {
                error(token, message);
                return;
            }
        }
    }

    private void validateDescending(Token token, List<Double> values, String message) {
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) > values.get(i - 1)) {
                error(token, message);
                return;
            }
        }
    }

    private double signedNumber(FlightParser.SignedNumberContext ctx) {
        if (ctx.NEG_NUMBER() != null) {
            return number(ctx.NEG_NUMBER().getText());
        }
        return number(ctx.NUMBER().getText());
    }

    private double number(String text) {
        return Double.parseDouble(text);
    }

    private void error(Token token, String message) {
        errors.add(new SemanticError(currentFlightId,
                String.format("line %d:%d: %s", token.getLine(), token.getCharPositionInLine(), message)));
    }

    private static class LegAttributes {
        private final String departure;
        private final String arrival;

        private LegAttributes(String departure, String arrival) {
            this.departure = departure;
            this.arrival = arrival;
        }
    }

    private static class ProfileEntryAttributes {
        private final double altitude;
        private final double speed;
        private final Double rateDescent;

        private ProfileEntryAttributes(double altitude, double speed, Double rateDescent) {
            this.altitude = altitude;
            this.speed = speed;
            this.rateDescent = rateDescent;
        }
    }
}
