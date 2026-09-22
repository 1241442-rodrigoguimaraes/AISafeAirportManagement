package domain.enginemodel;

import eapli.alsafe.engineModelMagnement.Domain.*;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.domain.MakerCountry;
import eapli.alsafe.aircraftModelMagnement.domain.MakerName;
import org.junit.Test;

import static org.junit.Assert.*;

public class EngineModelTest {

    private final MakerName MAKER_NAME = new MakerName("MakerA");
    private final Maker MAKER = new Maker(MAKER_NAME, new MakerCountry("PT"));

    @Test
    public void ensureValidEngineModelCreation() {
        // Arrange
        final engineModelName name = new engineModelName("EM-100");
        final engineModelPower power = new engineModelPower(1000.0);
        final engineModelEfficiency eff = new engineModelEfficiency(0.85);

        // Act
        final engineModel em = new engineModel(name, MAKER, engineType.TURBOFAN, power, engineModelFuel.JET_A1, eff);

        // Assert
        assertNotNull(em);
        assertEquals(name, em.name());
        assertEquals(MAKER, em.maker());
        assertEquals(engineType.TURBOFAN, em.getType());
        assertEquals(power, em.getPower());
        assertEquals(eff, em.getEfficiency());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNullArgumentsRejected() {
        // Act
        new engineModel(null, MAKER, engineType.TURBOFAN, new engineModelPower(1000), engineModelFuel.JET_A1, new engineModelEfficiency(0.9));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureBlankModelNameRejected() {
        // Act
        new engineModelName("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureInvalidPowerRejected() {
        // Act
        new engineModelPower(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureInvalidEfficiencyRejected() {
        // Act
        new engineModelEfficiency(0);
    }

    @Test
    public void ensureIdentityEqualityAndSameAs() {
        // Arrange
        final engineModelName name = new engineModelName("EM-200");
        final engineModelPower power = new engineModelPower(2000.0);
        final engineModelEfficiency eff = new engineModelEfficiency(0.9);

        // Create two engine models with same id (same name + maker)
        final engineModel a = new engineModel(name, MAKER, engineType.TURBOFAN, power, engineModelFuel.JET_A1, eff);
        final engineModel b = new engineModel(name, MAKER, engineType.TURBOFAN, power, engineModelFuel.JET_A1, eff);

        // Act / Assert
        assertEquals(a.identity(), b.identity());
        assertTrue(a.sameAs(b));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}


