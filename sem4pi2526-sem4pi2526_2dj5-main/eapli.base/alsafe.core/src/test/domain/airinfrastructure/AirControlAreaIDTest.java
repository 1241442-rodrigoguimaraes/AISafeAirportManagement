package domain.airinfrastructure;

import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AirControlAreaIDTest {

    @Test
    void ensureCanCreateID() {
        final AirControlAreaID id = new AirControlAreaID(1L);

        assertNotNull(id);
        assertEquals("ACA-0001", id.getId());
    }

    @Test
    void ensureIDIsFormattedCorrectly() {
        assertEquals("ACA-0001", new AirControlAreaID(1L).getId());
        assertEquals("ACA-0010", new AirControlAreaID(10L).getId());
        assertEquals("ACA-0100", new AirControlAreaID(100L).getId());
        assertEquals("ACA-1000", new AirControlAreaID(1000L).getId());
        assertEquals("ACA-9999", new AirControlAreaID(9999L).getId());
    }

    @Test
    void ensureSameIDsAreEqual() {
        final AirControlAreaID id1 = new AirControlAreaID(1L);
        final AirControlAreaID id2 = new AirControlAreaID(1L);

        assertEquals(id1, id2);
    }

    @Test
    void ensureDifferentIDsAreNotEqual() {
        final AirControlAreaID id1 = new AirControlAreaID(1L);
        final AirControlAreaID id2 = new AirControlAreaID(2L);

        assertNotEquals(id1, id2);
    }

    @Test
    void ensureToStringReturnsFormattedId() {
        final AirControlAreaID id = new AirControlAreaID(1L);

        assertEquals("ACA-0001", id.toString());
    }

    @Test
    void ensureCompareToWorks() {
        final AirControlAreaID id1 = new AirControlAreaID(1L);
        final AirControlAreaID id2 = new AirControlAreaID(2L);

        assertTrue(id1.compareTo(id2) < 0);
        assertTrue(id2.compareTo(id1) > 0);
        assertEquals(0, id1.compareTo(new AirControlAreaID(1L)));
    }

    @Test
    void ensureIDExceedsFourDigitsWhenSequenceIsLarge() {
        final AirControlAreaID id = new AirControlAreaID(10000L);

        assertEquals("ACA-10000", id.getId());
    }
}
