package domain.aircraftmodel;

import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.domain.MakerCountry;
import eapli.alsafe.aircraftModelMagnement.domain.MakerName;
import org.junit.Test;

import static org.junit.Assert.*;

public class MakerTest {

    @Test
    public void ensureValidMakerCreation() {
        // Arrange
        final MakerName name = new MakerName("Safemaker");
        final MakerCountry country = new MakerCountry("PT");

        // Act
        final Maker maker = new Maker(name, country);

        // Assert
        assertNotNull(maker);
        assertEquals(name, maker.identity());
        assertTrue(maker.hasIdentity(name));
        assertTrue(maker.sameAs(maker));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNullNameRejected() {
        // Arrange
        final MakerCountry country = new MakerCountry("PT");

        // Act
        new Maker(null, country);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNullCountryRejected() {
        // Arrange
        final MakerName name = new MakerName("X");

        // Act
        new Maker(name, null);
    }
}

