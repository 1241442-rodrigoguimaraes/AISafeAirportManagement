package domain.airinfrastructure;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaBoundaries;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaMinimumFuel;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class AirControlAreaTest {

    private final static String AIR_CONTROL_AREA_NAME = "Air Control Area";
    private final static AirControlAreaID AIR_CONTROL_AREA_ID = new AirControlAreaID(1L);
    private final static AirControlAreaID AIR_CONTROL_AREA_ID_2 = new AirControlAreaID(2L);

    private final static Coordinate COORDINATE_ONE = new Coordinate(10.0, 15.0);
    private final static Coordinate COORDINATE_TWO = new Coordinate(20.0, 25.0);
    private final static Coordinate COORDINATE_THREE = new Coordinate(30.0, 40.0);
    private final static Coordinate COORDINATE_FOUR = new Coordinate(35.0, 45.0);

    private final static AirControlAreaBoundaries BOUNDARIES = new AirControlAreaBoundaries(List.of(COORDINATE_ONE, COORDINATE_TWO, COORDINATE_THREE, COORDINATE_FOUR));
    private final static AirControlAreaBoundaries BOUNDARIES_2 = new AirControlAreaBoundaries(List.of(COORDINATE_TWO, COORDINATE_ONE, COORDINATE_THREE, COORDINATE_FOUR));
    private final static AirControlAreaMinimumFuel MINIMUM_FUEL = new AirControlAreaMinimumFuel(100.0);

    @Test
    public void ensureAirControlAreaIsCreatedWithAllRequiredFields() {
        AirControlArea airControlArea = new AirControlArea(AIR_CONTROL_AREA_ID, AIR_CONTROL_AREA_NAME, BOUNDARIES, MINIMUM_FUEL);

        assertNotNull(airControlArea);
        assertEquals(AIR_CONTROL_AREA_ID, airControlArea.getId());
        assertEquals(AIR_CONTROL_AREA_NAME, airControlArea.getName());
        assertEquals(BOUNDARIES, airControlArea.getBoundaries());
        assertEquals(MINIMUM_FUEL, airControlArea.getMinimumFuel());
    }

    @Test
    public void ensureTwoAreasWithDifferentIdsAreNotEqual() {
        AirControlArea area1 = new AirControlArea(AIR_CONTROL_AREA_ID, AIR_CONTROL_AREA_NAME, BOUNDARIES, MINIMUM_FUEL);
        AirControlArea area2 = new AirControlArea(AIR_CONTROL_AREA_ID_2, "Other Name", BOUNDARIES, MINIMUM_FUEL);

        assertNotEquals(area1, area2);
    }

    @Test
    public void ensureGeographicBoundariesAreValid() {
        AirControlArea airControlArea = new AirControlArea(AIR_CONTROL_AREA_ID, AIR_CONTROL_AREA_NAME, BOUNDARIES, MINIMUM_FUEL);

        assertNotNull(airControlArea.getBoundaries());
        assertTrue(airControlArea.getBoundaries().getBoundaries().size() >= 3);
    }

    @Test
    public void ensureMinimumFuelIsDefined() {
        AirControlArea airControlArea = new AirControlArea(AIR_CONTROL_AREA_ID, AIR_CONTROL_AREA_NAME, BOUNDARIES, MINIMUM_FUEL);

        assertNotNull(airControlArea.getMinimumFuel());
        assertEquals(MINIMUM_FUEL, airControlArea.getMinimumFuel());
    }

    @Test
    public void ensureAirControlAreaEqualityBasedOnIdentity() {
        AirControlArea area1 = new AirControlArea(AIR_CONTROL_AREA_ID, AIR_CONTROL_AREA_NAME, BOUNDARIES, MINIMUM_FUEL);
        AirControlArea area2 = new AirControlArea(AIR_CONTROL_AREA_ID, AIR_CONTROL_AREA_NAME, BOUNDARIES, MINIMUM_FUEL);
        AirControlArea area3 = new AirControlArea(AIR_CONTROL_AREA_ID_2, AIR_CONTROL_AREA_NAME, BOUNDARIES_2, MINIMUM_FUEL);

        assertEquals(area1, area2);
        assertNotEquals(area1, area3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAirControlAreaCannotBeCreatedWithNullId() {
        new AirControlArea(null, AIR_CONTROL_AREA_NAME, BOUNDARIES, MINIMUM_FUEL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAirControlAreaCannotBeCreatedWithNullName() {
        new AirControlArea(AIR_CONTROL_AREA_ID, null, BOUNDARIES, MINIMUM_FUEL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAirControlAreaCannotBeCreatedWithNullBoundaries() {
        new AirControlArea(AIR_CONTROL_AREA_ID, AIR_CONTROL_AREA_NAME, null, MINIMUM_FUEL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAirControlAreaCannotBeCreatedWithNullMinimumFuel() {
        new AirControlArea(AIR_CONTROL_AREA_ID, AIR_CONTROL_AREA_NAME, BOUNDARIES, null);
    }

    @Test
    public void ensureOverlappingAreasAreDetected() {
        AirControlArea area1 = new AirControlArea(AIR_CONTROL_AREA_ID, AIR_CONTROL_AREA_NAME, BOUNDARIES, MINIMUM_FUEL);
        AirControlArea area2 = new AirControlArea(AIR_CONTROL_AREA_ID_2, "Other Area", BOUNDARIES, MINIMUM_FUEL);

        assertTrue(area1.overlaps(area2));
    }

    @Test
    public void ensureNonOverlappingAreasAreNotDetected() {
        AirControlAreaBoundaries boundaries3 = new AirControlAreaBoundaries(List.of(
                new Coordinate(10.0, 46.0),
                new Coordinate(10.0, 60.0),
                new Coordinate(35.0, 46.0),
                new Coordinate(35.0, 60.0)
        ));

        AirControlArea area1 = new AirControlArea(AIR_CONTROL_AREA_ID, AIR_CONTROL_AREA_NAME, BOUNDARIES, MINIMUM_FUEL);
        AirControlArea area3 = new AirControlArea(AIR_CONTROL_AREA_ID_2, "Other Area", boundaries3, MINIMUM_FUEL);

        assertFalse(area1.overlaps(area3));
    }

    @Test
    public void ensureAdjacentAreasAreNotOverlapping() {
        AirControlAreaBoundaries boundaries4 = new AirControlAreaBoundaries(List.of(
                new Coordinate(10.0, 45.0),
                new Coordinate(10.0, 60.0),
                new Coordinate(35.0, 45.0),
                new Coordinate(35.0, 60.0)
        ));

        AirControlArea area1 = new AirControlArea(AIR_CONTROL_AREA_ID, AIR_CONTROL_AREA_NAME, BOUNDARIES, MINIMUM_FUEL);
        AirControlArea area4 = new AirControlArea(AIR_CONTROL_AREA_ID_2, "Adjacent Area", boundaries4, MINIMUM_FUEL);

        assertFalse(area1.overlaps(area4));
    }
}
