package eapli.alsafe.weather.domain;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.framework.domain.model.DomainFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;

public class WeatherDataBuilder implements DomainFactory<WeatherData> {

    private static final Logger LOGGER = LogManager.getLogger(WeatherDataBuilder.class);

    private AirControlArea airControlArea;
    private WeatherDate weatherDate;
    private WindCondition windCondition;

    public WeatherDataBuilder() {}

    public WeatherDataBuilder with(final AirControlArea airControlArea, final Calendar date,
                                   final int windDirection, final double windSpeed) {
        withAirControlArea(airControlArea);
        withDate(date);
        withWindCondition(windDirection, windSpeed);
        return this;
    }

    public WeatherDataBuilder withAirControlArea(final AirControlArea airControlArea) {
        this.airControlArea = airControlArea;
        return this;
    }

    public WeatherDataBuilder withDate(final Calendar date) {
        this.weatherDate = new WeatherDate(date);
        return this;
    }

    public WeatherDataBuilder withWindCondition(final int direction, final double speed) {
        this.windCondition = new WindCondition(direction, speed);
        return this;
    }

    @Override
    public WeatherData build() {
        final var weatherData = new WeatherData(airControlArea, weatherDate, windCondition);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Building WeatherData : [{}]", weatherData);
        }

        return weatherData;
    }
}
