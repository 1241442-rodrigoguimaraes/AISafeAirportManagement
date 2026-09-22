package domain.enginemodel;

import eapli.alsafe.engineModelMagnement.Domain.engineType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngineTypeTest {

    @Test
    void ensureEnumContainsExpectedValues() {
        // Arrange & Act
        final engineType[] vals = engineType.values();

        // Assert
        assertNotNull(vals);
        assertTrue(vals.length >= 4);
        assertTrue(java.util.Arrays.asList(vals).contains(engineType.TURBOFAN));
        assertTrue(java.util.Arrays.asList(vals).contains(engineType.TURBOPROP));
        assertTrue(java.util.Arrays.asList(vals).contains(engineType.ELECTRIC_PROPELLER));
    }

    @Test
    void ensureValueOfWorks() {
        // Act & Assert
        assertEquals(engineType.TURBOFAN, engineType.valueOf("TURBOFAN"));
    }
}


