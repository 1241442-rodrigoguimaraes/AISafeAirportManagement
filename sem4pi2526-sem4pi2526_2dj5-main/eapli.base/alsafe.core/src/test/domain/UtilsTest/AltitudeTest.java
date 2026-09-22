package domain.UtilsTest;

import eapli.alsafe.utils.nodes.domain.Altitude;
import org.junit.Test;

public class AltitudeTest {
    @Test
    public void testAltitude() {
        Altitude alt = new Altitude(100.0);
        assert(alt.meters() == 100);
    }

    @Test
    public void testEquals() {
        Altitude alt1 = new Altitude(100.0);
        Altitude alt2 = new Altitude(100.0);
        assert(alt1.equals(alt2));
    }

    @Test
    public void testNotEquals() {
        Altitude alt1 = new Altitude(100.0);
        Altitude alt2 = new Altitude(150.0);
        assert(!alt1.equals(alt2));
    }

    @Test
    public void testToString() {
        Altitude alt = new Altitude(100.0);
        assert(alt.toString().equals("100.0m"));
    }
}
