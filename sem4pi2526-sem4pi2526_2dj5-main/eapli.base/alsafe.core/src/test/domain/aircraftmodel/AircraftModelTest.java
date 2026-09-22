package domain.aircraftmodel;

import eapli.alsafe.aircraftModelMagnement.domain.*;
import eapli.alsafe.engineModelMagnement.Domain.*;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class AircraftModelTest {

    private final MakerName MAKER_NAME = new MakerName("MakerA");
    private final Maker MAKER = new Maker(MAKER_NAME, new MakerCountry("PT"));

    private aircraftModelPhysicsData validPhysics() {
        return new aircraftModelPhysicsData(1000, 2000, 1500, 500, 30000, 800, 50, 0.02, 1.2);
    }

    @Test
    public void ensureValidAircraftModelCreation() {
        // Arrange
        final aircraftModelName name = new aircraftModelName("AM-1");
        final maximumRange range = new maximumRange(1000);
        final aircraftModelPhysicsData pd = validPhysics();

        final engineModel certified = new engineModel(new engineModelName("E1"), MAKER, engineType.TURBOFAN, new engineModelPower(1000), engineModelFuel.JET_A1, new engineModelEfficiency(0.9));

        // Act
        final aircraftModel am = new aircraftModel(name, MAKER, aircraftModelType.PASSENGER, engineType.TURBOFAN, range, pd, Set.of(certified));

        // Assert
        assertNotNull(am);
        assertEquals(name, am.name());
        assertEquals(MAKER, am.maker());
        assertEquals(aircraftModelType.PASSENGER, am.type());
        assertEquals(engineType.TURBOFAN, am.motorization());
        assertEquals(range.range(), am.maximumRange().range(), 0.0);
        assertEquals(pd, am.physicsData());
        assertTrue(am.certifiedEngines().contains(certified));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNullNameRejected() {
        new aircraftModel(null, MAKER, aircraftModelType.PASSENGER, engineType.TURBOFAN, new maximumRange(1000), validPhysics(), Set.of(new engineModel(new engineModelName("E1"), MAKER, engineType.TURBOFAN, new engineModelPower(1000), engineModelFuel.JET_A1, new engineModelEfficiency(0.9))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNullMakerRejected() {
        new aircraftModel(new aircraftModelName("AM"), null, aircraftModelType.PASSENGER, engineType.TURBOFAN, new maximumRange(1000), validPhysics(), Set.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNullTypeRejected() {
        new aircraftModel(new aircraftModelName("AM"), MAKER, null, engineType.TURBOFAN, new maximumRange(1000), validPhysics(), Set.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNullMotorizationRejected() {
        new aircraftModel(new aircraftModelName("AM"), MAKER, aircraftModelType.PASSENGER, null, new maximumRange(1000), validPhysics(), Set.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNullMaxRangeRejected() {
        new aircraftModel(new aircraftModelName("AM"), MAKER, aircraftModelType.PASSENGER, engineType.TURBOFAN, null, validPhysics(), Set.of(new engineModel(new engineModelName("E1"), MAKER, engineType.TURBOFAN, new engineModelPower(1000), engineModelFuel.JET_A1, new engineModelEfficiency(0.9))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNullPhysicsRejected() {
        new aircraftModel(new aircraftModelName("AM"), MAKER, aircraftModelType.PASSENGER, engineType.TURBOFAN, new maximumRange(1000), null, Set.of(new engineModel(new engineModelName("E1"), MAKER, engineType.TURBOFAN, new engineModelPower(1000), engineModelFuel.JET_A1, new engineModelEfficiency(0.9))));
    }

    @Test
    public void ensureEmptyCertifiedSetAllowed() {
        final aircraftModel am = new aircraftModel(new aircraftModelName("AM"), MAKER, aircraftModelType.PASSENGER, engineType.TURBOFAN, new maximumRange(1000), validPhysics(), Set.of());
        // empty certified set is currently allowed by constructor; assert no certified engines
        assertNotNull(am);
        assertTrue(am.certifiedEngines().isEmpty());
    }

    @Test
    public void ensurePassengerSeatsComputed() {
        // Arrange
        final aircraftModelName name = new aircraftModelName("AM-1");
        final maximumRange range = new maximumRange(1000);
        final aircraftModelPhysicsData pd = new aircraftModelPhysicsData(1000, 2000, 1500, 500, 30000, 800, 50, 0.02, 1.2);
        final engineModel certified = new engineModel(new engineModelName("E1"), MAKER, engineType.TURBOFAN, new engineModelPower(1000), engineModelFuel.JET_A1, new engineModelEfficiency(0.9));

        final aircraftModel am = new aircraftModel(name, MAKER, aircraftModelType.PASSENGER, engineType.TURBOFAN, range, pd, Set.of(certified));

        // Act
        final int seats = am.maxPassengerSeats();

        // payload = MZFW - empty = 500 -> seats ~ floor(500/90) = 5
        assertEquals(5, seats);
    }

    @Test
    public void ensureEqualityAndHashcodeByIdentity() {
        // Arrange
        final aircraftModelName name = new aircraftModelName("AM-X");
        final maximumRange range = new maximumRange(1000);
        final aircraftModelPhysicsData pd = validPhysics();
        final engineModel certified = new engineModel(new engineModelName("E1"), MAKER, engineType.TURBOFAN, new engineModelPower(1000), engineModelFuel.JET_A1, new engineModelEfficiency(0.9));

        final aircraftModel a = new aircraftModel(name, MAKER, aircraftModelType.PASSENGER, engineType.TURBOFAN, range, pd, Set.of(certified));
        final aircraftModel b = new aircraftModel(name, MAKER, aircraftModelType.PASSENGER, engineType.TURBOFAN, range, pd, Set.of(certified));

        assertEquals(a.identity(), b.identity());
        assertTrue(a.sameAs(b));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}



