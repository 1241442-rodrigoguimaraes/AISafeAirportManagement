package domain.aircraftmodel;

import eapli.alsafe.aircraftModelMagnement.domain.*;
import eapli.alsafe.engineModelMagnement.Domain.*;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.domain.MakerCountry;
import eapli.alsafe.aircraftModelMagnement.domain.MakerName;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class AircraftModelBuilderTest {

    private final Maker maker = new Maker(new MakerName("MakerA"), new MakerCountry("PT"));

    private engineModel engine() {
        return new engineModel(new engineModelName("E1"), maker, engineType.TURBOFAN,
                new engineModelPower(1000), engineModelFuel.JET_A1, new engineModelEfficiency(0.9));
    }

    private aircraftModelPhysicsData physicsData() {
        return new aircraftModelPhysicsData(1000, 2000, 1500, 500, 30000, 800, 50, 0.02, 1.2);
    }

    @Test
    public void ensureBuilderCreatesAircraftModelWithRawValues() {
        final engineModel engine = engine();

        final aircraftModel result = new AircraftModelBuilder()
                .with("AM-100", maker, aircraftModelType.PASSENGER, engineType.TURBOFAN, 1000,
                        1000, 2000, 1500, 500, 30000, 800, 50, 0.02, 1.2, Set.of(engine))
                .build();

        assertNotNull(result);
        assertEquals(new aircraftModelName("AM-100"), result.name());
        assertEquals(maker, result.maker());
        assertEquals(aircraftModelType.PASSENGER, result.type());
        assertEquals(engineType.TURBOFAN, result.motorization());
        assertTrue(result.certifiedEngines().contains(engine));
    }

    @Test
    public void ensureBuilderCreatesAircraftModelStepByStep() {
        final aircraftModelName name = new aircraftModelName("AM-200");
        final maximumRange range = new maximumRange(2000);
        final aircraftModelPhysicsData physicsData = physicsData();
        final engineModel engine = engine();

        final aircraftModel result = new AircraftModelBuilder()
                .withName(name)
                .withMaker(maker)
                .withType(aircraftModelType.CARGO)
                .withMotorization(engineType.TURBOFAN)
                .withMaximumRange(range)
                .withPhysicsData(physicsData)
                .addCertifiedEngine(engine)
                .build();

        assertNotNull(result);
        assertEquals(name, result.name());
        assertEquals(maker, result.maker());
        assertEquals(aircraftModelType.CARGO, result.type());
        assertEquals(range.range(), result.maximumRange().range(), 0.0);
        assertEquals(physicsData, result.physicsData());
        assertTrue(result.certifiedEngines().contains(engine));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureBuilderRejectsInvalidValues() {
        new AircraftModelBuilder()
                .with(" ", maker, aircraftModelType.PASSENGER, engineType.TURBOFAN, 1000,
                        1000, 2000, 1500, 500, 30000, 800, 50, 0.02, 1.2, Set.of(engine()))
                .build();
    }
}
