package domain.flightPlan;

import eapli.alsafe.flightPlan.domain.FuelQuantity;
import eapli.alsafe.flightPlan.domain.FuelUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FuelQuantityTest {

    private static final double DELTA = 0.001;

    @Test
    void ensureValidFuelQuantityInLiters() {
        FuelQuantity fq = new FuelQuantity(1000, FuelUnit.L);
        assertEquals(1000, fq.getFuelQuantity(), DELTA);
        assertEquals(FuelUnit.L, fq.getFuelUnit());
    }

    @Test
    void ensureValidFuelQuantityInKilograms() {
        FuelQuantity fq = new FuelQuantity(500, FuelUnit.KG);
        assertEquals(500, fq.getFuelQuantity(), DELTA);
        assertEquals(FuelUnit.KG, fq.getFuelUnit());
    }

    @Test
    void ensureValidFuelQuantityInPounds() {
        FuelQuantity fq = new FuelQuantity(2000, FuelUnit.LBS);
        assertEquals(2000, fq.getFuelQuantity(), DELTA);
        assertEquals(FuelUnit.LBS, fq.getFuelUnit());
    }

    @Test
    void ensureNegativeFuelQuantityIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FuelQuantity(-100, FuelUnit.L));
    }

    @Test
    void ensureZeroFuelQuantityIsAllowed() {
        FuelQuantity fq = new FuelQuantity(0, FuelUnit.L);
        assertEquals(0, fq.getFuelQuantity(), DELTA);
    }

    @Test
    void ensureNullFuelUnitIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FuelQuantity(100, null));
    }

    @Test
    void ensureToLitersFromLitersReturnsSame() {
        FuelQuantity fq = new FuelQuantity(100, FuelUnit.L);
        FuelQuantity converted = FuelQuantity.toLiters(fq);
        assertEquals(100, converted.getFuelQuantity(), DELTA);
        assertEquals(FuelUnit.L, converted.getFuelUnit());
    }

    @Test
    void ensureToLitersFromKilograms() {
        FuelQuantity fq = new FuelQuantity(100, FuelUnit.KG);
        FuelQuantity converted = FuelQuantity.toLiters(fq);
        double expected = 100 / FuelQuantity.LITERS_PER_KG;
        assertEquals(expected, converted.getFuelQuantity(), DELTA);
        assertEquals(FuelUnit.L, converted.getFuelUnit());
    }

    @Test
    void ensureToLitersFromPounds() {
        FuelQuantity fq = new FuelQuantity(100, FuelUnit.LBS);
        FuelQuantity converted = FuelQuantity.toLiters(fq);
        double expected = 100 * FuelQuantity.LBS_PER_L;
        assertEquals(expected, converted.getFuelQuantity(), DELTA);
        assertEquals(FuelUnit.L, converted.getFuelUnit());
    }

    @Test
    void ensureToKilogramsFromKilogramsReturnsSame() {
        FuelQuantity fq = new FuelQuantity(100, FuelUnit.KG);
        FuelQuantity converted = FuelQuantity.toKilograms(fq);
        assertEquals(100, converted.getFuelQuantity(), DELTA);
        assertEquals(FuelUnit.KG, converted.getFuelUnit());
    }

    @Test
    void ensureToKilogramsFromLiters() {
        FuelQuantity fq = new FuelQuantity(100, FuelUnit.L);
        FuelQuantity converted = FuelQuantity.toKilograms(fq);
        double expected = 100 * FuelQuantity.LITERS_PER_KG;
        assertEquals(expected, converted.getFuelQuantity(), DELTA);
        assertEquals(FuelUnit.KG, converted.getFuelUnit());
    }

    @Test
    void ensureToKilogramsFromPounds() {
        FuelQuantity fq = new FuelQuantity(100, FuelUnit.LBS);
        FuelQuantity converted = FuelQuantity.toKilograms(fq);
        double expected = 100 * FuelQuantity.KG_PER_LBS;
        assertEquals(expected, converted.getFuelQuantity(), DELTA);
        assertEquals(FuelUnit.KG, converted.getFuelUnit());
    }

    @Test
    void ensureToPoundsFromPoundsReturnsSame() {
        FuelQuantity fq = new FuelQuantity(100, FuelUnit.LBS);
        FuelQuantity converted = FuelQuantity.toPounds(fq);
        assertEquals(100, converted.getFuelQuantity(), DELTA);
        assertEquals(FuelUnit.LBS, converted.getFuelUnit());
    }

    @Test
    void ensureToPoundsFromKilograms() {
        FuelQuantity fq = new FuelQuantity(100, FuelUnit.KG);
        FuelQuantity converted = FuelQuantity.toPounds(fq);
        double expected = 100 / FuelQuantity.KG_PER_LBS;
        assertEquals(expected, converted.getFuelQuantity(), DELTA);
        assertEquals(FuelUnit.LBS, converted.getFuelUnit());
    }

    @Test
    void ensureToPoundsFromLiters() {
        FuelQuantity fq = new FuelQuantity(100, FuelUnit.L);
        FuelQuantity converted = FuelQuantity.toPounds(fq);
        double expected = 100 / FuelQuantity.LBS_PER_L;
        assertEquals(expected, converted.getFuelQuantity(), DELTA);
        assertEquals(FuelUnit.LBS, converted.getFuelUnit());
    }
}
