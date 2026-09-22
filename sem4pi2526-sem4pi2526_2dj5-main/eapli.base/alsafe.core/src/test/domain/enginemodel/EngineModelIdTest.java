package domain.enginemodel;

import eapli.alsafe.engineModelMagnement.Domain.engineModelId;
import eapli.alsafe.engineModelMagnement.Domain.engineModelName;
import eapli.alsafe.aircraftModelMagnement.domain.MakerName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngineModelIdTest {

    @Test
    void ensureCanCreateValidId() {
        // Arrange
        final engineModelName model = new engineModelName("CFM56-3");
        final MakerName maker = new MakerName("CFM");

        // Act
        final engineModelId id = new engineModelId(model, maker);

        // Assert
        assertNotNull(id);
        assertEquals(model, id.modelName());
        assertEquals(maker, id.makerName());
    }

    @Test
    void ensureNullArgsThrow() {
        // Arrange
        final engineModelName model = new engineModelName("PW4000");
        final MakerName maker = new MakerName("Pratt");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new engineModelId(null, maker));
        assertThrows(IllegalArgumentException.class, () -> new engineModelId(model, null));
        assertThrows(IllegalArgumentException.class, () -> new engineModelId(null, null));
    }

    @Test
    void ensureEqualsHashCodeAndCompareTo() {
        // Arrange
        final engineModelName model = new engineModelName("GE90");
        final MakerName maker = new MakerName("GE");
        final engineModelId id1 = new engineModelId(model, maker);
        final engineModelId id2 = new engineModelId(model, maker);

        // Act & Assert
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertEquals(0, id1.compareTo(id2));
        assertTrue(id1.compareTo(null) > 0);
    }

    @Test
    void ensureToStringContainsParts() {
        // Arrange
        final engineModelId id = new engineModelId(new engineModelName("CFM56"), new MakerName("CFM"));

        // Act & Assert
        final String s = id.toString();
        assertTrue(s.contains("CFM") && s.contains("CFM56"));
    }
}



