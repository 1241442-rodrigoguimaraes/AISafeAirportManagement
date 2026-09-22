package domain.enginemodel;

import eapli.alsafe.engineModelMagnement.Domain.engineModelEfficiency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngineModelEfficiencyTest {

    @Test
    void ensureCanCreateValidEfficiency() {
        // Arrange
        final double e = 0.85;

        // Act
        final engineModelEfficiency eff = new engineModelEfficiency(e);

        // Assert
        assertNotNull(eff);
        assertEquals(e, eff.efficiency());
    }

    @Test
    void ensureZeroOrNegativeThrows() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new engineModelEfficiency(0));
        assertThrows(IllegalArgumentException.class, () -> new engineModelEfficiency(-0.1));
    }

    @Test
    void ensureToStringContainsValue() {
        // Arrange
        final engineModelEfficiency eff = new engineModelEfficiency(0.92);

        // Act & Assert
        assertTrue(eff.toString().contains("0.92") || eff.toString().contains("0.9"));
    }
}


