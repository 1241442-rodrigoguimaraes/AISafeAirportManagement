package domain.aircraftmodel;

import eapli.alsafe.aircraftModelMagnement.domain.MakerName;
import org.junit.Test;

import static org.junit.Assert.*;

public class MakerNameTest {

    @Test
    public void ensureValidMakerNameIsCreated() {
        // Arrange
        final String name = "Rolls-Royce";

        // Act
        final MakerName makerName = new MakerName(name);

        // Assert
        assertNotNull(makerName);
        assertEquals(name, makerName.name());
        assertEquals(name, makerName.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNullNameRejected() {
        // Act
        new MakerName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureBlankNameRejected() {
        // Act
        new MakerName("   ");
    }

    @Test
    public void ensureEqualityAndHashcode() {
        // Arrange
        final MakerName n1 = new MakerName("A");
        final MakerName n2 = new MakerName("A");

        // Act / Assert
        assertEquals(n1, n2);
        assertEquals(n1.hashCode(), n2.hashCode());
    }
}

