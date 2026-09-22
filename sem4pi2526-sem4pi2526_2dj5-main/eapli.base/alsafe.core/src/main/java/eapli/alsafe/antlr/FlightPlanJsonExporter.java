package eapli.alsafe.antlr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eapli.alsafe.antlr.domain.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Exports a list of {@link FlightPlanDSL} domain objects to a JSON file.
 *
 * <p>Usage:
 * <pre>
 *   FlightDSLProcessor.Result result = new FlightDSLProcessor().process(inputPath);
 *   if (result.isValid()) {
 *       new FlightPlanJsonExporter().export(result.getFlightPlans(), Path.of("output.json"));
 *   }
 * </pre>
 *
 * <p>Jackson is used for serialization and is already present in the project classpath
 * via spring-boot-starter-json.
 */
public class FlightPlanJsonExporter {

    private final ObjectMapper mapper;

    public FlightPlanJsonExporter() {
        this.mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Exports all flight plans to a JSON file at {@code outputPath}.
     *
     * @param flightPlanDSLS the plans produced by {@link FlightPlanBuilderVisitor}
     * @param outputPath  destination file (will be created or overwritten)
     * @throws IOException if the file cannot be written
     */
    public void export(List<FlightPlanDSL> flightPlanDSLS, Path outputPath) throws IOException {
        List<Map<String, Object>> root = new ArrayList<>();
        for (FlightPlanDSL plan : flightPlanDSLS) {
            root.add(serializeFlightPlan(plan));
        }
        mapper.writeValue(outputPath.toFile(), root);
    }

    /**
     * Converts a single {@link FlightPlanDSL} to a {@code Map} that Jackson will serialize.
     */
    private Map<String, Object> serializeFlightPlan(FlightPlanDSL plan) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("flightId",  plan.getFlightId());
        map.put("type",      plan.getType().name().toLowerCase());
        map.put("route",     plan.getRouteId());
        map.put("date",      plan.getDate());
        map.put("time",      plan.getTime());
        map.put("aircraft",  plan.getAircraft());

        List<Map<String, Object>> legs = new ArrayList<>();
        for (Leg leg : plan.getLegs()) {
            legs.add(serializeLeg(leg));
        }
        map.put("legs", legs);

        return map;
    }

    private Map<String, Object> serializeLeg(Leg leg) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("departure", leg.getDepartureAirport());
        map.put("arrival",   leg.getArrivalAirport());
        map.put("fuel",      serializeFuel(leg.getFuel()));
        map.put("profile",   serializeProfile(leg.getProfile()));

        List<Map<String, Object>> segments = new ArrayList<>();
        for (Segment seg : leg.getSegments()) {
            segments.add(serializeSegment(seg));
        }
        map.put("segments", segments);

        return map;
    }

    private Map<String, Object> serializeFuel(FuelInfo fuel) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("quantity", fuel.getQuantity());
        map.put("unit",     fuel.getUnit().name().toLowerCase());
        return map;
    }

    private Map<String, Object> serializeSegment(Segment seg) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("mode",          seg.getMode().name().toLowerCase());
        map.put("start",         serializeCoords(seg.getStart()));
        map.put("end",           serializeCoords(seg.getEnd()));
        map.put("altitudeSlots", seg.getAltitudeSlots());
        map.put("widthMeters",   seg.getWidthMeters());
        map.put("windDirectionDeg", seg.getWindDirectionDeg());
        map.put("windSpeedMs",   seg.getWindSpeedMs());
        return map;
    }

    private Map<String, Object> serializeProfile(FlightProfile profile) {
        Map<String, Object> map = new LinkedHashMap<>();

        List<Map<String, Object>> climb = new ArrayList<>();
        for (ProfileEntry entry : profile.getClimbEntries()) {
            climb.add(serializeProfileEntry(entry));
        }
        map.put("climb", climb);

        Map<String, Object> cruise = new LinkedHashMap<>();
        cruise.put("speed", profile.getCruiseSpeed());
        cruise.put("unit",  profile.getCruiseSpeedUnit());
        map.put("cruise", cruise);

        List<Map<String, Object>> descend = new ArrayList<>();
        for (ProfileEntry entry : profile.getDescendEntries()) {
            descend.add(serializeProfileEntry(entry));
        }
        map.put("descend", descend);

        return map;
    }

    private Map<String, Object> serializeProfileEntry(ProfileEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("altitude", entry.getAltitude());
        map.put("speed",    entry.getSpeed());
        if (entry.getRateDescent() != null) {
            map.put("rateDescent", entry.getRateDescent());
        }
        return map;
    }

    private Map<String, Object> serializeCoords(Coords3D coords) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("latitude",        coords.getLatitude());
        map.put("longitude",       coords.getLongitude());
        map.put("altitudeMeters",  coords.getAltitudeMeters());
        return map;
    }
}