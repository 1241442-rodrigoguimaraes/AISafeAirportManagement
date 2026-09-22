package eapli.alsafe.utils.provider.interfaces;

import eapli.alsafe.weather.domain.WeatherParsing.ParseResult;

public interface WeatherDataImportProvider {
    ParseResult getWeatherData();
}
