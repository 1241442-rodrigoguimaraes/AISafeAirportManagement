package eapli.alsafe.utils.provider.interfaces;

import eapli.alsafe.weather.domain.WeatherParsing.BulkImportResult;

public interface WeatherDataExporterProvider {
    boolean exportWeatherData(BulkImportResult result);
    String buildContent(BulkImportResult result);
}
