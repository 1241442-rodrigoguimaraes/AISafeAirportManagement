package domain.UtilsTest;

import eapli.alsafe.utils.nodes.domain.Coordinate;
import org.junit.Test;

public class CoordinateTest {

    @Test
    public void testCoordinates() {
        Coordinate coord = new Coordinate(10.0, 20.0);
        assert(coord.latitude() == 10);
        assert(coord.longitude() == 20);
    }

    @Test
    public void testEquals() {
        Coordinate coord1 = new Coordinate(10.0, 20.0);
        Coordinate coord2 = new Coordinate(10.0, 20.0);
        assert(coord1.equals(coord2));
    }

    @Test
    public void testNotEquals() {
        Coordinate coord1 = new Coordinate(10.0, 20.0);
        Coordinate coord2 = new Coordinate(15.0, 25.0);
        assert(!coord1.equals(coord2));
    }


    @Test
    public void testToString() {
        Coordinate coord = new Coordinate(10.0, 20.0);
        assert(coord.toString().equals("[10.0, 20.0]"));
    }
}
