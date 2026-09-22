package domain.enginemodel;

import eapli.alsafe.engineModelMagnement.Domain.engineModelPower;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngineModelPowerTest {

    @Test
    void ensureCanCreateValidPower() {
        // Arrange
        final double p = 50000.0;

        // Act
        final engineModelPower power = new engineModelPower(p);

        // Assert
        assertNotNull(power);
        assertEquals(p, power.power());
    }

    @Test
    void ensureZeroOrNegativeThrows() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new engineModelPower(0));
        assertThrows(IllegalArgumentException.class, () -> new engineModelPower(-1));
    }

    @Test
    void ensureToStringContainsValue() {
        // Arrange
        final engineModelPower pw = new engineModelPower(62000.5);

        // Act & Assert
        assertTrue(pw.toString().contains("62000") || pw.toString().contains("62000.5"));
    }
}


