package controller.weather;

import eapli.alsafe.utils.provider.weatherParsers.JsonWeatherDataImportProvider;
import eapli.alsafe.weather.domain.WeatherParsing.ParseResult;
import eapli.alsafe.weather.domain.WeatherParsing.ParsedWeatherDataLine;
import eapli.alsafe.weather.domain.WeatherParsing.WeatherImportError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JsonWeatherDataImportProviderTest {

    @TempDir
    Path tempDir;

    @Test
    void validJson_returnsParsedLines() throws IOException {
        final Path file = tempDir.resolve("data.json");
        Files.writeString(file, """
                [
                  {"airControlAreaCode":"ACA-0001","date":"2026-05-20","windDirection":180,"windSpeed":12.5},
                  {"airControlAreaCode":"ACA-0002","date":"2026-05-21","windDirection":90,"windSpeed":8.0}
                ]
                """);

        final JsonWeatherDataImportProvider provider = new JsonWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertEquals(2, result.parsedLines().size());
        assertFalse(result.hasErrors());
        assertEquals("ACA-0001", result.parsedLines().get(0).getCode());
        assertEquals("ACA-0002", result.parsedLines().get(1).getCode());
    }

    @Test
    void jsonWithInvalidSyntax_returnsParseError() throws IOException {
        final Path file = tempDir.resolve("invalid.json");
        Files.writeString(file, "{ bad json }");

        final JsonWeatherDataImportProvider provider = new JsonWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertFalse(result.hasParsedLines());
        assertTrue(result.hasErrors());
        assertEquals(WeatherImportError.Category.PARSE_ERROR, result.errors().get(0).getCategory());
    }

    @Test
    void jsonWithMissingField_returnsParseError() throws IOException {
        final Path file = tempDir.resolve("missing.json");
        Files.writeString(file, """
                [
                  {"airControlAreaCode":"ACA-0001","date":"2026-05-20","windDirection":180,"windSpeed":12.5},
                  {"airControlAreaCode":"ACA-0002","date":"2026-05-21","windSpeed":8.0}
                ]
                """);

        final JsonWeatherDataImportProvider provider = new JsonWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertTrue(result.hasErrors());
        assertEquals(1, result.parsedLines().size());
        assertEquals(1, result.errors().size());
    }

    @Test
    void jsonWithInvalidNumber_returnsParseError() throws IOException {
        final Path file = tempDir.resolve("badnum.json");
        Files.writeString(file, """
                [
                  {"airControlAreaCode":"ACA-0001","date":"2026-05-20","windDirection":"abc","windSpeed":12.5}
                ]
                """);

        final JsonWeatherDataImportProvider provider = new JsonWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertFalse(result.hasParsedLines());
        assertTrue(result.hasErrors());
    }

    @Test
    void jsonEmptyArray_returnsNoLinesNoErrors() throws IOException {
        final Path file = tempDir.resolve("empty.json");
        Files.writeString(file, "[]");

        final JsonWeatherDataImportProvider provider = new JsonWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertFalse(result.hasParsedLines());
        assertFalse(result.hasErrors());
    }

    @Test
    void fileNotFound_returnsParseError() {
        final JsonWeatherDataImportProvider provider =
                new JsonWeatherDataImportProvider(tempDir.resolve("nonexistent.json").toString());
        final ParseResult result = provider.getWeatherData();

        assertFalse(result.hasParsedLines());
        assertTrue(result.hasErrors());
        assertEquals(WeatherImportError.Category.PARSE_ERROR, result.errors().get(0).getCategory());
    }

    @Test
    void jsonWithMixedValidAndInvalid_returnsPartialResult() throws IOException {
        final Path file = tempDir.resolve("mixed.json");
        Files.writeString(file, """
                [
                  {"airControlAreaCode":"ACA-0001","date":"2026-05-20","windDirection":180,"windSpeed":12.5},
                  {"airControlAreaCode":"","date":"2026-05-21","windDirection":90,"windSpeed":8.0},
                  {"airControlAreaCode":"ACA-0003","date":"2026-05-22","windDirection":45,"windSpeed":15.3}
                ]
                """);

        final JsonWeatherDataImportProvider provider = new JsonWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertEquals(2, result.parsedLines().size());
        assertEquals(1, result.errors().size());
    }
}
