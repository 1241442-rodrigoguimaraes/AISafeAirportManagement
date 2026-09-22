package domain.aircraftmodel;

import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftModelNameTest {

    @Test
    void ensureCanCreateValidAircraftModelName() {
        // Arrange
        final String name = "B737-800";

        // Act
        final aircraftModelName modelName = new aircraftModelName(name);

        // Assert
        assertNotNull(modelName);
        assertEquals(name, modelName.name());
    }

    @Test
    void ensureNullNameThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new aircraftModelName(null));
    }

    @Test
    void ensureEmptyNameThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new aircraftModelName(""));
    }

    @Test
    void ensureBlankNameThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new aircraftModelName("   "));
    }

    @Test
    void ensureEqualsAndHashCode() {
        // Arrange
        final aircraftModelName n1 = new aircraftModelName("A320");
        final aircraftModelName n2 = new aircraftModelName("A320");

        // Act & Assert
        assertEquals(n1, n2);
        assertEquals(n1.hashCode(), n2.hashCode());
    }

    @Test
    void ensureToStringReturnsValue() {
        // Arrange
        final aircraftModelName nm = new aircraftModelName("A380");

        // Act & Assert
        assertEquals("A380", nm.toString());
    }
}


