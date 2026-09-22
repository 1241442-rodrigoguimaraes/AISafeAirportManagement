package domain.airinfrastructure;

import eapli.alsafe.airinfrastructure.domain.AirControlAreaBoundaries;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AirControlAreaBoundariesTest {

    @Test
    void ensureBoundariesCanBeCreated() {
        List<Coordinate> coords = Arrays.asList(
                new Coordinate(0.0, 0.0),
                new Coordinate(0.0, 1.0),
                new Coordinate(1.0, 0.0),
                new Coordinate(1.0, 1.0)
        );
        AirControlAreaBoundaries boundaries = new AirControlAreaBoundaries(coords);
        assertNotNull(boundaries);
        assertEquals(coords, boundaries.getBoundaries());
    }

    @Test
    void ensureCannotCreateWithNullBoundaries() {
        assertThrows(IllegalArgumentException.class, () -> new AirControlAreaBoundaries(null));
    }

    @Test
    void ensureCannotCreateWithLessThanFourCoordinates() {
        List<Coordinate> coords = Arrays.asList(
                new Coordinate(0.0, 0.0),
                new Coordinate(0.0, 1.0),
                new Coordinate(1.0, 0.0)
        );
        assertThrows(IllegalArgumentException.class, () -> new AirControlAreaBoundaries(coords));
    }

    @Test
    void ensureEmptyBoundariesThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new AirControlAreaBoundaries(new ArrayList<>()));
    }

    @Test
    void ensureSameBoundariesAreEqual() {
        List<Coordinate> coords1 = Arrays.asList(
                new Coordinate(0.0, 0.0),
                new Coordinate(0.0, 1.0),
                new Coordinate(1.0, 0.0),
                new Coordinate(1.0, 1.0)
        );
        List<Coordinate> coords2 = Arrays.asList(
                new Coordinate(0.0, 0.0),
                new Coordinate(0.0, 1.0),
                new Coordinate(1.0, 0.0),
                new Coordinate(1.0, 1.0)
        );
        AirControlAreaBoundaries b1 = new AirControlAreaBoundaries(coords1);
        AirControlAreaBoundaries b2 = new AirControlAreaBoundaries(coords2);
        assertEquals(b1, b2);
    }
}
