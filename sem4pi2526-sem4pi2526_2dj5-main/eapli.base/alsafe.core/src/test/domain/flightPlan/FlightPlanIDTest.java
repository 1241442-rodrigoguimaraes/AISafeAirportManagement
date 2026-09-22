package domain.flightPlan;

import eapli.alsafe.flightPlan.domain.FlightPlanID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanIDTest {

    @Test
    void ensureValidFlightPlanIDWithNumberOnly() {
        FlightPlanID id = new FlightPlanID("TP123");
        assertEquals("TP123", id.toString());
    }

    @Test
    void ensureValidFlightPlanIDWithSuffix() {
        FlightPlanID id = new FlightPlanID("TP123A");
        assertEquals("TP123A", id.toString());
    }

    @Test
    void ensureValidFlightPlanIDWithSingleDigit() {
        FlightPlanID id = new FlightPlanID("TP1");
        assertEquals("TP1", id.toString());
    }

    @Test
    void ensureValidFlightPlanIDWithFourDigits() {
        FlightPlanID id = new FlightPlanID("TP1234");
        assertEquals("TP1234", id.toString());
    }

    @Test
    void ensureValidFlightPlanIDIsCaseInsensitive() {
        FlightPlanID id = new FlightPlanID("tp123a");
        assertEquals("tp123a", id.toString());
    }

    @Test
    void ensureNullFlightPlanIDIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FlightPlanID(null));
    }

    @Test
    void ensureBlankFlightPlanIDIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FlightPlanID("  "));
    }

    @Test
    void ensureFlightPlanIDWithoutLettersIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FlightPlanID("12345"));
    }

    @Test
    void ensureFlightPlanIDWithSingleLetterIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FlightPlanID("A123"));
    }

    @Test
    void ensureFlightPlanIDWithThreeLettersIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FlightPlanID("ABC123"));
    }

    @Test
    void ensureFlightPlanIDWithFiveDigitsIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FlightPlanID("TP12345"));
    }

    @Test
    void ensureFlightPlanIDWithSpecialCharsIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FlightPlanID("TP-12"));
    }

    @Test
    void ensureValidFormatReturnsTrue() {
        assertTrue(FlightPlanID.validFlightPlanIDFormat("PT123"));
    }

    @Test
    void ensureInvalidFormatReturnsFalse() {
        assertFalse(FlightPlanID.validFlightPlanIDFormat("INVALID"));
    }

    @Test
    void ensureCompareToWorks() {
        FlightPlanID id1 = new FlightPlanID("TP123");
        FlightPlanID id2 = new FlightPlanID("TP124");
        assertTrue(id1.compareTo(id2) < 0);
        assertEquals(0, id1.compareTo(new FlightPlanID("TP123")));
        assertTrue(id2.compareTo(id1) > 0);
    }
}
