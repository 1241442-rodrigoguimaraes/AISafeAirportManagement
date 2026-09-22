package domain.airinfrastructure;

import eapli.alsafe.airinfrastructure.domain.AirControlAreaMinimumFuel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AirControlAreaMinimumFuelTest {

    @Test
    void ensureCanCreateMinimumFuel() {
        AirControlAreaMinimumFuel fuel = new AirControlAreaMinimumFuel(100.0);
        assertNotNull(fuel);
        assertEquals(100.0, fuel.getMinimumFuel());
    }

    @Test
    void ensureCannotCreateWithNegativeFuel() {
        assertThrows(IllegalArgumentException.class, () -> new AirControlAreaMinimumFuel(-1.0));
    }

    @Test
    void ensureZeroFuelIsAllowed() {
        AirControlAreaMinimumFuel fuel = new AirControlAreaMinimumFuel(0.0);
        assertEquals(0.0, fuel.getMinimumFuel());
    }

    @Test
    void ensureSameFuelAmountsAreEqual() {
        AirControlAreaMinimumFuel fuel1 = new AirControlAreaMinimumFuel(50.5);
        AirControlAreaMinimumFuel fuel2 = new AirControlAreaMinimumFuel(50.5);
        assertEquals(fuel1, fuel2);
    }

    @Test
    void ensureDifferentFuelAmountsAreNotEqual() {
        AirControlAreaMinimumFuel fuel1 = new AirControlAreaMinimumFuel(50.5);
        AirControlAreaMinimumFuel fuel2 = new AirControlAreaMinimumFuel(50.6);
        assertNotEquals(fuel1, fuel2);
    }
}
