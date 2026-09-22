package domain.aircraftmodel;

import eapli.alsafe.aircraftModelMagnement.domain.MakerCountry;
import org.junit.Test;

import static org.junit.Assert.*;

public class MakerCountryTest {

    @Test
    public void ensureValidCountryCodesAccepted() {
        // Arrange / Act
        MakerCountry c1 = new MakerCountry("PT");
        MakerCountry c2 = new MakerCountry("FR");
        MakerCountry c3 = new MakerCountry("US");

        // Assert
        assertNotNull(c1);
        assertNotNull(c2);
        assertNotNull(c3);
        assertEquals("PT", c1.country());
        assertEquals("FR", c2.country());
        assertEquals("US", c3.country());
    }

    @Test
    public void ensureLowercaseAccepted() {
        // Act
        MakerCountry c = new MakerCountry("pt");

        // Assert: validation should accept lowercase; the stored value may preserve input
        assertNotNull(c);
        assertTrue(c.country().equalsIgnoreCase("PT"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureBlankRejected() {
        // Act
        new MakerCountry("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNullRejected() {
        // Act
        new MakerCountry(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureFakeCountryRejected() {
        // Act
        new MakerCountry("Atlantis");
    }
}

