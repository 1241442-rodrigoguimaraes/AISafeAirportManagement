package domain.enginemodel;

import eapli.alsafe.engineModelMagnement.Domain.engineModelFuel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngineModelFuelTest {

    @Test
    void ensureEnumContainsExpectedValues() {
        // Arrange & Act
        final engineModelFuel[] vals = engineModelFuel.values();

        // Assert
        assertNotNull(vals);
        assertTrue(vals.length >= 3);
        assertTrue(java.util.Arrays.asList(vals).contains(engineModelFuel.JET_A1));
        assertTrue(java.util.Arrays.asList(vals).contains(engineModelFuel.AVGAS));
        assertTrue(java.util.Arrays.asList(vals).contains(engineModelFuel.ELECTRIC));
    }

    @Test
    void ensureValueOfWorks() {
        // Act & Assert
        assertEquals(engineModelFuel.JET_A1, engineModelFuel.valueOf("JET_A1"));
    }
}


