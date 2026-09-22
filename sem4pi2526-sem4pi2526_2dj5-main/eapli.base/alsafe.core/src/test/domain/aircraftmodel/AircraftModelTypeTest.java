package domain.aircraftmodel;

import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftModelTypeTest {

    @Test
    void ensureEnumContainsExpectedValues() {
        // Arrange & Act
        final aircraftModelType[] values = aircraftModelType.values();

        // Assert
        assertNotNull(values);
        assertTrue(values.length >= 3);
        assertTrue(java.util.Arrays.asList(values).contains(aircraftModelType.PASSENGER));
        assertTrue(java.util.Arrays.asList(values).contains(aircraftModelType.CARGO));
        assertTrue(java.util.Arrays.asList(values).contains(aircraftModelType.MIXED));
    }

    @Test
    void ensureValueOfWorks() {
        // Act & Assert
        assertEquals(aircraftModelType.PASSENGER, aircraftModelType.valueOf("PASSENGER"));
    }
}


