package domain.aircraftmodel;

import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelId;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelName;
import eapli.alsafe.aircraftModelMagnement.domain.MakerName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftModelIdTest {

    @Test
    void ensureCanCreateValidId() {
        // Arrange
        final MakerName maker = new MakerName("Boeing");
        final aircraftModelName model = new aircraftModelName("B737");

        // Act
        final aircraftModelId id = new aircraftModelId(maker, model);

        // Assert
        assertNotNull(id);
        assertEquals(maker, id.makerName());
        assertEquals(model, id.modelName());
    }

    @Test
    void ensureNullArgsThrow() {
        // Arrange
        final MakerName maker = new MakerName("Airbus");
        final aircraftModelName model = new aircraftModelName("A320");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new aircraftModelId(null, model));
        assertThrows(IllegalArgumentException.class, () -> new aircraftModelId(maker, null));
        assertThrows(IllegalArgumentException.class, () -> new aircraftModelId(null, null));
    }

    @Test
    void ensureEqualsHashCodeAndCompareTo() {
        // Arrange
        final MakerName maker = new MakerName("Boeing");
        final aircraftModelName model = new aircraftModelName("B737");
        final aircraftModelId id1 = new aircraftModelId(maker, model);
        final aircraftModelId id2 = new aircraftModelId(maker, model);

        // Act & Assert
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertEquals(0, id1.compareTo(id2));
        assertTrue(id1.compareTo(null) > 0);
    }

    @Test
    void ensureToStringContainsParts() {
        // Arrange
        final aircraftModelId id = new aircraftModelId(new MakerName("Boeing"), new aircraftModelName("B737"));

        // Act & Assert
        final String s = id.toString();
        assertTrue(s.contains("Boeing") && s.contains("B737"));
    }
}


