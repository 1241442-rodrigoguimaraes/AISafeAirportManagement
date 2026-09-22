package domain.weatherTest;

import eapli.alsafe.weather.domain.WeatherDate;
import eapli.alsafe.weather.domain.WindCondition;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

class WeatherTest {

    @Test
    void ensureWindConditionValidatesInputs() {
        // Valid inputs
        assertDoesNotThrow(() -> new WindCondition(0, 0));
        assertDoesNotThrow(() -> new WindCondition(360, 10.5));
        assertDoesNotThrow(() -> new WindCondition(180, 50));

        // Invalid direction
        assertThrows(IllegalArgumentException.class, () -> new WindCondition(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> new WindCondition(361, 10));

        // Invalid speed
        assertThrows(IllegalArgumentException.class, () -> new WindCondition(180, -0.1));
    }

    @Test
    void ensureWeatherDateValidatesInputs() {
        // Valid date (now)
        assertDoesNotThrow(() -> new WeatherDate(Calendar.getInstance()));

        // Valid date (past)
        Calendar past = Calendar.getInstance();
        past.add(Calendar.DAY_OF_MONTH, -1);
        assertDoesNotThrow(() -> new WeatherDate(past));

        // Invalid date (future)
        Calendar future = Calendar.getInstance();
        future.add(Calendar.DAY_OF_MONTH, 1);
        assertThrows(IllegalArgumentException.class, () -> new WeatherDate(future));

        // Null date
        assertThrows(IllegalArgumentException.class, () -> new WeatherDate(null));
    }
}
