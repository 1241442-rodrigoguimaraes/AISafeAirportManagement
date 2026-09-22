package eapli.alsafe.weather.application;

import eapli.alsafe.weather.domain.WeatherParsing.ParsedWeatherDataLine;
import eapli.alsafe.weather.domain.WeatherParsing.WeatherImportError;

public class WeatherImportException extends RuntimeException {

    private final WeatherImportError.Category category;
    private final String detail;

    public WeatherImportException(final WeatherImportError.Category category, final String detail) {
        super(detail);
        this.category = category;
        this.detail = detail;
    }

    public WeatherImportError toImportError(final ParsedWeatherDataLine line) {
        return new WeatherImportError(category, line.toString(), line.getLine(), detail);
    }
}
