package controller.weather;

import eapli.alsafe.utils.provider.weatherParsers.CsvWeatherDataImportProvider;
import eapli.alsafe.weather.domain.WeatherParsing.ParseResult;
import eapli.alsafe.weather.domain.WeatherParsing.ParsedWeatherDataLine;
import eapli.alsafe.weather.domain.WeatherParsing.WeatherImportError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CsvWeatherDataImportProviderTest {

    @TempDir
    Path tempDir;

    @Test
    void validCsv_returnsParsedLines() throws IOException {
        Path csv = tempDir.resolve("valid.csv");
        Files.writeString(csv,
                "airControlAreaCode;date;windDirection;windSpeed\n" +
                "ACA-0001;2026-05-20;180;12.5\n" +
                "ACA-0002;2026-05-21;90;8.0\n");

        CsvWeatherDataImportProvider provider = new CsvWeatherDataImportProvider(csv.toString());
        ParseResult result = provider.getWeatherData();

        assertEquals(2, result.parsedLines().size());
        assertEquals(0, result.errors().size());
        assertEquals("ACA-0001", result.parsedLines().get(0).getCode());
        assertEquals("2026-05-20", result.parsedLines().get(0).getDate());
        assertEquals(180, result.parsedLines().get(0).getWindDirection());
        assertEquals(12.5, result.parsedLines().get(0).getWindSpeed());
    }

    @Test
    void csvWithInvalidNumber_returnsParseError() throws IOException {
        Path csv = tempDir.resolve("invalid_number.csv");
        Files.writeString(csv,
                "airControlAreaCode;date;windDirection;windSpeed\n" +
                "ACA-0001;2026-05-20;abc;12.5\n");

        CsvWeatherDataImportProvider provider = new CsvWeatherDataImportProvider(csv.toString());
        ParseResult result = provider.getWeatherData();

        assertEquals(0, result.parsedLines().size());
        assertEquals(1, result.errors().size());
        assertEquals(WeatherImportError.Category.PARSE_ERROR, result.errors().get(0).getCategory());
    }

    @Test
    void csvWithMissingFields_returnsParseError() throws IOException {
        Path csv = tempDir.resolve("missing_fields.csv");
        Files.writeString(csv,
                "airControlAreaCode;date;windDirection;windSpeed\n" +
                "ACA-0001;2026-05-20;180\n");

        CsvWeatherDataImportProvider provider = new CsvWeatherDataImportProvider(csv.toString());
        ParseResult result = provider.getWeatherData();

        assertEquals(0, result.parsedLines().size());
        assertEquals(1, result.errors().size());
        assertEquals(WeatherImportError.Category.PARSE_ERROR, result.errors().get(0).getCategory());
    }

    @Test
    void csvWithEmptyCode_returnsParseError() throws IOException {
        Path csv = tempDir.resolve("empty_code.csv");
        Files.writeString(csv,
                "airControlAreaCode;date;windDirection;windSpeed\n" +
                ";2026-05-20;180;12.5\n");

        CsvWeatherDataImportProvider provider = new CsvWeatherDataImportProvider(csv.toString());
        ParseResult result = provider.getWeatherData();

        assertEquals(0, result.parsedLines().size());
        assertEquals(1, result.errors().size());
        assertEquals(WeatherImportError.Category.PARSE_ERROR, result.errors().get(0).getCategory());
    }

    @Test
    void fileNotFound_returnsParseError() {
        CsvWeatherDataImportProvider provider = new CsvWeatherDataImportProvider("nonexistent.csv");
        ParseResult result = provider.getWeatherData();

        assertEquals(0, result.parsedLines().size());
        assertEquals(1, result.errors().size());
        assertEquals(WeatherImportError.Category.PARSE_ERROR, result.errors().get(0).getCategory());
    }

    @Test
    void emptyCsv_returnsNoLinesNoErrors() throws IOException {
        Path csv = tempDir.resolve("empty.csv");
        Files.writeString(csv,
                "airControlAreaCode;date;windDirection;windSpeed\n");

        CsvWeatherDataImportProvider provider = new CsvWeatherDataImportProvider(csv.toString());
        ParseResult result = provider.getWeatherData();

        assertEquals(0, result.parsedLines().size());
        assertEquals(0, result.errors().size());
    }

    @Test
    void csvWithMixedValidAndInvalid_returnsPartialResult() throws IOException {
        Path csv = tempDir.resolve("mixed.csv");
        Files.writeString(csv,
                "airControlAreaCode;date;windDirection;windSpeed\n" +
                "ACA-0001;2026-05-20;180;12.5\n" +
                "ACA-0002;2026-05-20;abc;8.0\n" +
                "ACA-0003;2026-05-21;90;10.0\n");

        CsvWeatherDataImportProvider provider = new CsvWeatherDataImportProvider(csv.toString());
        ParseResult result = provider.getWeatherData();

        assertEquals(2, result.parsedLines().size());
        assertEquals(1, result.errors().size());
        assertEquals(WeatherImportError.Category.PARSE_ERROR, result.errors().get(0).getCategory());
    }
}
