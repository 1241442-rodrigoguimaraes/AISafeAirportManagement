package domain.enginemodel;

import eapli.alsafe.engineModelMagnement.Domain.*;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.domain.MakerCountry;
import eapli.alsafe.aircraftModelMagnement.domain.MakerName;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EngineModelBuilderTest {

    private final Maker maker = new Maker(new MakerName("MakerA"), new MakerCountry("PT"));

    @Test
    public void ensureBuilderCreatesEngineModelWithStringValues() {
        final engineModel result = new EngineModelBuilder()
                .with("EM-100", maker, engineType.TURBOFAN, 1000, engineModelFuel.JET_A1, 0.85)
                .build();

        assertNotNull(result);
        assertEquals(new engineModelName("EM-100"), result.name());
        assertEquals(maker, result.maker());
        assertEquals(engineType.TURBOFAN, result.getType());
        assertEquals(1000, result.getPower().power(), 0.0);
        assertEquals(engineModelFuel.JET_A1, result.getFuel());
        assertEquals(0.85, result.getEfficiency().efficiency(), 0.0);
    }

    @Test
    public void ensureBuilderCreatesEngineModelStepByStep() {
        final engineModelName name = new engineModelName("EM-200");
        final engineModelPower power = new engineModelPower(2000);
        final engineModelEfficiency efficiency = new engineModelEfficiency(0.9);

        final engineModel result = new EngineModelBuilder()
                .withName(name)
                .withMaker(maker)
                .withType(engineType.TURBOPROP)
                .withPower(power)
                .withFuel(engineModelFuel.JET_A1)
                .withEfficiency(efficiency)
                .build();

        assertNotNull(result);
        assertEquals(name, result.name());
        assertEquals(maker, result.maker());
        assertEquals(engineType.TURBOPROP, result.getType());
        assertEquals(power, result.getPower());
        assertEquals(efficiency, result.getEfficiency());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureBuilderRejectsInvalidValues() {
        new EngineModelBuilder()
                .with(" ", maker, engineType.TURBOFAN, 1000, engineModelFuel.JET_A1, 0.85)
                .build();
    }
}
