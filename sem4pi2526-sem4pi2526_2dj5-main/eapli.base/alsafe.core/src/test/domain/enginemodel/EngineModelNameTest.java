package domain.enginemodel;

import eapli.alsafe.engineModelMagnement.Domain.engineModelName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngineModelNameTest {

    @Test
    void ensureCanCreateValidEngineModelName() {
        // Arrange
        final String name = "CFM56-3";

        // Act
        final engineModelName nm = new engineModelName(name);

        // Assert
        assertNotNull(nm);
        assertEquals(name, nm.name());
    }

    @Test
    void ensureNullOrBlankThrows() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new engineModelName(null));
        assertThrows(IllegalArgumentException.class, () -> new engineModelName(""));
        assertThrows(IllegalArgumentException.class, () -> new engineModelName("   "));
    }

    @Test
    void ensureEqualsHashCodeAndToString() {
        // Arrange
        final engineModelName a = new engineModelName("V2500");
        final engineModelName b = new engineModelName("V2500");

        // Act & Assert
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals("V2500", a.toString());
    }
}


