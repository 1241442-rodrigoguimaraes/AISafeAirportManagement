package eapli.alsafe.dsl;


import eapli.alsafe.antlr.SemanticError;
import eapli.alsafe.antlr.domain.FlightPlanDSL;
import eapli.alsafe.antlr.domain.FlightProfile;
import eapli.alsafe.antlr.domain.Leg;
import eapli.alsafe.antlr.domain.ProfileEntry;
import eapli.alsafe.antlr.FlightDSLProcessor;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the full DSL processing pipeline:
 * lexical analysis → syntactic analysis → visitor → semantic validation.
 */
class FlightDSLProcessorTest {

    private static final String BASE_PATH = "src/test/resources/dsl/";

    // =========================================================================
    // Valid file — happy path
    // =========================================================================

    @Test
    void validBasicFlightPlan_shouldProduceDomainObjectsWithNoErrors() throws Exception {
        FlightDSLProcessor processor = new FlightDSLProcessor();
        FlightDSLProcessor.Result result = processor.process(Path.of(BASE_PATH + "valid_basic.fp"));

        assertTrue(result.isSyntaxOk(), "Syntax should be correct");
        assertTrue(result.isValid(),    "No semantic errors expected");

        List<FlightPlanDSL> plans = result.getFlightPlans();
        assertEquals(1, plans.size(), "Should contain exactly one flight plan");

        FlightPlanDSL plan = plans.get(0);
        assertEquals("TP123",              plan.getFlightId());
        assertEquals(FlightPlanDSL.FlightType.REGULAR, plan.getType());
        assertEquals("TP123",              plan.getRouteId());
        assertEquals("2025-05-01",         plan.getDate());
        assertEquals("09:00",              plan.getTime());
        assertEquals("CS-TUA",             plan.getAircraft());

        List<Leg> legs = plan.getLegs();
        assertEquals(1, legs.size(), "Should have one leg");

        Leg leg = legs.get(0);
        assertEquals("OPO", leg.getDepartureAirport());
        assertEquals("MAD", leg.getArrivalAirport());
        assertEquals(4200.0, leg.getFuel().getQuantity(), 0.001);
        assertEquals(1, leg.getSegments().size(), "Should have one segment");

        FlightProfile profile = leg.getProfile();
        assertNotNull(profile, "FlightProfile should not be null");
        assertEquals(2, profile.getClimbEntries().size(), "Climb should have 2 entries");
        assertEquals(0.0,   profile.getClimbEntries().get(0).getAltitude(), 0.001);
        assertEquals(210.0, profile.getClimbEntries().get(0).getSpeed(), 0.001);
        assertEquals(12000.0, profile.getClimbEntries().get(1).getAltitude(), 0.001);
        assertEquals(300.0,   profile.getClimbEntries().get(1).getSpeed(), 0.001);

        assertEquals(460.0, profile.getCruiseSpeed(), 0.001);
        assertEquals("knots", profile.getCruiseSpeedUnit());

        assertEquals(2, profile.getDescendEntries().size(), "Descend should have 2 entries");
        assertEquals(12000.0, profile.getDescendEntries().get(0).getAltitude(), 0.001);
        assertEquals(300.0,   profile.getDescendEntries().get(0).getSpeed(), 0.001);
        assertEquals(-10.0,   profile.getDescendEntries().get(0).getRateDescent(), 0.001);
        assertEquals(0.0,     profile.getDescendEntries().get(1).getAltitude(), 0.001);
        assertEquals(140.0,   profile.getDescendEntries().get(1).getSpeed(), 0.001);
        assertEquals(-5.0,    profile.getDescendEntries().get(1).getRateDescent(), 0.001);
    }

    // =========================================================================
    // Syntax errors — files that should fail parsing
    // =========================================================================

    @Test
    void missingType_shouldFailSyntax() throws Exception {
        FlightDSLProcessor.Result result = process("invalid_missing_type.fp");
        assertFalse(result.isSyntaxOk(), "Missing type field should cause a syntax error");
    }

    @Test
    void badFlightDesignator_shouldFailSyntax() throws Exception {
        FlightDSLProcessor.Result result = process("invalid_bad_designator.fp");
        assertFalse(result.isSyntaxOk(), "Bad flight designator should cause a syntax error");
    }

    @Test
    void icaoCodeInsteadOfIata_shouldFailSyntax() throws Exception {
        FlightDSLProcessor.Result result = process("invalid_bad_iata.fp");
        assertFalse(result.isSyntaxOk(), "4-letter ICAO code where IATA expected should fail");
    }

    @Test
    void missingSegment_shouldFailSyntax() throws Exception {
        FlightDSLProcessor.Result result = process("invalid_missing_segment.fp");
        assertFalse(result.isSyntaxOk(), "Leg without segments should fail parsing");
    }

    // =========================================================================
    // Semantic errors — syntactically valid but semantically wrong
    // =========================================================================

    @Test
    void duplicateFlightId_shouldReportSemanticError() throws Exception {
        FlightDSLProcessor.Result result = process("invalid_duplicate_flight_id.fp");
        assertTrue(result.isSyntaxOk(), "Syntax should still be OK");
        assertFalse(result.isValid(),   "Duplicate flight id should produce a semantic error");
        assertTrue(containsMessageFragment(result.getErrors(), "Duplicate flight identifier"),
                "Error should mention duplicate flight identifier");
    }

    @Test
    void zeroFuel_shouldReportSemanticError() throws Exception {
        FlightDSLProcessor.Result result = process("invalid_zero_fuel.fp");


        assertTrue(result.isSyntaxOk(), "Syntax should be OK for zero fuel");
        assertFalse(result.isValid(),   "Zero fuel quantity should produce a semantic error");
        assertTrue(containsMessageFragment(result.getErrors(), "strictly positive"),
                "Error should mention strictly positive fuel");
    }

    @Test
    void legSequenceMismatch_shouldReportSemanticError() throws Exception {
        FlightDSLProcessor.Result result = process("invalid_leg_sequence.fp");
        assertTrue(result.isSyntaxOk(), "Syntax should be OK");
        assertFalse(result.isValid(),   "Mismatched leg sequence should produce a semantic error");
        assertTrue(containsMessageFragment(result.getErrors(), "does not match"),
                "Error should mention the airport mismatch");
    }

    @Test
    void sameStartEndCoords_shouldReportSemanticError() throws Exception {
        FlightDSLProcessor.Result result = process("invalid_same_coords.fp");
        assertTrue(result.isSyntaxOk(), "Syntax should be OK");
        assertFalse(result.isValid(),   "Identical start/end coordinates should produce a semantic error");
        assertTrue(containsMessageFragment(result.getErrors(), "start and end coordinates are identical"),
                "Error should mention identical coordinates");
    }

    @Test
    void invalidWindDirection_shouldReportSemanticError() throws Exception {
        FlightDSLProcessor.Result result = process("invalid_wind_direction.fp");
        assertTrue(result.isSyntaxOk(), "Syntax should be OK");
        assertFalse(result.isValid(),   "Wind direction outside 0..360 should produce a semantic error");
        assertTrue(containsMessageFragment(result.getErrors(), "Wind direction must be between 0 and 360"),
                "Error should mention wind direction bounds");
    }

    @Test
    void negativeAltitudeInProfile_shouldFailSyntax() throws Exception {
        FlightDSLProcessor.Result result = process("invalid_negative_altitude_profile.fp");
        assertFalse(result.isSyntaxOk(), "Negative numeric literal where positive expected should fail syntax");
    }

    @Test
    void invalidTime_shouldFailSyntax() throws Exception {
        FlightDSLProcessor.Result result = process("invalid_bad_time.fp");
        assertFalse(result.isSyntaxOk(), "Invalid time literal should fail lexer and cause a syntax error");
    }

    @Test
    void validMultipleFlights_shouldProduceDomainObjectsWithNoErrors() throws Exception {
        FlightDSLProcessor.Result result = process("valid_multiple_flights.fp");
        assertTrue(result.isSyntaxOk(), "Syntax should be correct");
        assertTrue(result.isValid(),    "No semantic errors expected");

        List<FlightPlanDSL> plans = result.getFlightPlans();
        assertEquals(2, plans.size(), "Should contain two flight plans");

        assertEquals("TP123", plans.get(0).getFlightId());
        assertEquals(FlightPlanDSL.FlightType.REGULAR, plans.get(0).getType());

        assertEquals("EK312", plans.get(1).getFlightId());
        assertEquals(FlightPlanDSL.FlightType.CHARTER, plans.get(1).getType());
        assertEquals("CS-TUB", plans.get(1).getAircraft());
    }

    @Test
    void invalidProfileRules_shouldReportSemanticErrors() throws Exception {
        FlightDSLProcessor.Result result = process("invalid_profile_rules.fp");
        assertTrue(result.isSyntaxOk(), "Syntax should be OK");
        assertFalse(result.isValid(),   "Invalid profile values should produce semantic errors");
        assertTrue(containsMessageFragment(result.getErrors(), "Climb profile altitudes must be in ascending order"),
                "Error should mention climb altitude order");
        assertTrue(containsMessageFragment(result.getErrors(), "Rate of descent must be negative"),
                "Error should mention rate of descent sign");
    }


    // =========================================================================
    // JSON export
    // =========================================================================

    @Test
    void validFlightPlan_shouldExportJsonFile() throws Exception {
        Path input     = Path.of(BASE_PATH + "valid_basic.fp");
        Path outputDir = Path.of("flightPlanJson");
        Files.createDirectories(outputDir);
        Path output = outputDir.resolve("valid_basic.json");
        Files.deleteIfExists(output);

        FlightDSLProcessor.Result result = new FlightDSLProcessor().process(input, output);

        assertTrue(result.isValid(),       "Flight plan should be valid");
        assertTrue(Files.exists(output),   "JSON file should have been created");

        ObjectMapper mapper = new ObjectMapper();
        var root = mapper.readTree(output.toFile());
        assertTrue(root.isArray(),                                    "Root should be a JSON array");
        assertEquals(1, root.size(),                                  "Should have one flight plan");
        assertEquals("TP123",   root.get(0).get("flightId").asText());
        assertEquals("regular", root.get(0).get("type").asText());
        assertEquals("OPO",     root.get(0).get("legs").get(0).get("departure").asText());
        assertEquals("MAD",     root.get(0).get("legs").get(0).get("arrival").asText());
    }

    @Test
    void invalidFlightPlan_shouldNotExportJsonFile() throws Exception {
        Path input  = Path.of(BASE_PATH + "invalid_missing_type.fp");
        Path output = Path.of(BASE_PATH + "invalid_missing_type.json");
        Files.deleteIfExists(output);

        FlightDSLProcessor.Result result = new FlightDSLProcessor().process(input, output);

        assertFalse(result.isValid(),          "Flight plan should be invalid");
        assertFalse(Files.exists(output),      "JSON file must NOT be created for invalid input");
        assertNull(result.getJsonOutputPath(), "JSON output path should be null on failure");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private FlightDSLProcessor.Result process(String filename) throws Exception {
        return new FlightDSLProcessor().process(Path.of(BASE_PATH + filename));
    }

    private boolean containsMessageFragment(List<SemanticError> errors, String fragment) {
        return errors.stream().anyMatch(e -> e.getMessage().contains(fragment));
    }
}
