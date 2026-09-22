package domain.aircraftmodel;

import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelPhysicsData;
import org.junit.Test;

import static org.junit.Assert.*;

public class AircraftModelPhysicsDataTest {

    @Test
    public void ensureValidPhysicsDataCreated() {
        // Arrange / Act
        aircraftModelPhysicsData pd = new aircraftModelPhysicsData(
                1000.0, // emptyWeight
                2000.0, // MTOW
                1500.0, // MZFW
                500.0,  // fuel cap
                30000.0,// service ceiling
                800.0,  // cruise speed
                50.0,   // wing area
                0.02,   // drag coeff
                1.2     // lift coeff
        );

        // Assert
        assertNotNull(pd);
        assertEquals(1000.0, pd.emptyWeight(), 0.0);
        assertEquals(2000.0, pd.maximumTakeOffWeight(), 0.0);
        assertEquals(1500.0, pd.maximumZeroFuelWeight(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNegativeEmptyWeightRejected() {
        new aircraftModelPhysicsData(-1, 2000, 1500, 500, 30000, 800, 50, 0.02, 1.2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureMTOWLessThanEmptyRejected() {
        new aircraftModelPhysicsData(2000, 1500, 1500, 500, 30000, 800, 50, 0.02, 1.2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureMZFWLessThanEmptyRejected() {
        new aircraftModelPhysicsData(2000, 3000, 1500, 500, 30000, 800, 50, 0.02, 1.2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureMTOWLessThanMZFWRejected() {
        new aircraftModelPhysicsData(1000, 1400, 1500, 500, 30000, 800, 50, 0.02, 1.2);
    }
}

