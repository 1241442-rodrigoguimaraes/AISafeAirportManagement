package domain.airinfrastructure;

import eapli.alsafe.airinfrastructure.domain.FlightRouteName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FlightRouteNameTest {

    private static String name;

    @BeforeEach
    void setUp() {
        name = "TP123";
    }

    @Test
    void ensureFlightRouteNameCanBeCreated() {
        FlightRouteName flightRouteName = new FlightRouteName(name);

        assertEquals(name, flightRouteName.getName());
    }

    @Test
    void ensureFlightRouteNameCannotBeCreatedWithTheWrongFormat() {
        String invalidName = "route1";

        assertThrows(IllegalArgumentException.class, () -> new FlightRouteName(invalidName));
    }

    @Test
    void ensureFlightRouteNameCannotBeCreatedWithBlankOrNull() {
        assertThrows(IllegalArgumentException.class, () -> new FlightRouteName(""));
        assertThrows(IllegalArgumentException.class, () -> new FlightRouteName(null));
    }

    @Test
    void ensureFlightRouteNameCompareToWorks() {
        FlightRouteName flightRouteName1 = new FlightRouteName(name);
        FlightRouteName flightRouteName2 = new FlightRouteName(name);

        assertEquals(0, flightRouteName1.compareTo(flightRouteName2));
    }

    @Test
    void ensureFlightRouteNameToStringWorks() {
        FlightRouteName flightRouteName = new FlightRouteName(name);

        assertEquals(name, flightRouteName.toString());
    }
}
