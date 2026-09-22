package domain.aircraftmodel;

import eapli.alsafe.aircraftModelMagnement.domain.maximumRange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaximumRangeTest {

    @Test
    void ensureCanCreateValidRange() {
        // Arrange
        final double value = 10000.0;

        // Act
        final maximumRange r = new maximumRange(value);

        // Assert
        assertNotNull(r);
        assertEquals(value, r.range());
    }

    @Test
    void ensureZeroOrNegativeRangeThrows() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new maximumRange(0));
        assertThrows(IllegalArgumentException.class, () -> new maximumRange(-1));
    }

    @Test
    void ensureToStringContainsValue() {
        // Arrange
        final maximumRange r = new maximumRange(12345.6);

        // Act & Assert
        assertTrue(r.toString().contains("12345") || r.toString().contains("12345.6"));
    }
}


