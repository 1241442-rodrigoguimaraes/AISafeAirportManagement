package controller.weather;

import eapli.alsafe.utils.provider.weatherParsers.XmlWeatherDataImportProvider;
import eapli.alsafe.weather.domain.WeatherParsing.ParseResult;
import eapli.alsafe.weather.domain.WeatherParsing.ParsedWeatherDataLine;
import eapli.alsafe.weather.domain.WeatherParsing.WeatherImportError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class XmlWeatherDataImportProviderTest {

    @TempDir
    Path tempDir;

    @Test
    void validXml_returnsParsedLines() throws IOException {
        final Path file = tempDir.resolve("data.xml");
        Files.writeString(file, """
                <?xml version="1.0" encoding="UTF-8"?>
                <weatherDataRecords>
                    <record>
                        <airControlAreaCode>ACA-0001</airControlAreaCode>
                        <date>2026-05-20</date>
                        <windDirection>180</windDirection>
                        <windSpeed>12.5</windSpeed>
                    </record>
                    <record>
                        <airControlAreaCode>ACA-0002</airControlAreaCode>
                        <date>2026-05-21</date>
                        <windDirection>90</windDirection>
                        <windSpeed>8.0</windSpeed>
                    </record>
                </weatherDataRecords>
                """);

        final XmlWeatherDataImportProvider provider = new XmlWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertEquals(2, result.parsedLines().size());
        assertFalse(result.hasErrors());
        assertEquals("ACA-0001", result.parsedLines().get(0).getCode());
        assertEquals("ACA-0002", result.parsedLines().get(1).getCode());
    }

    @Test
    void malformedXml_returnsParseError() throws IOException {
        final Path file = tempDir.resolve("bad.xml");
        Files.writeString(file, "<root><unclosed>");

        final XmlWeatherDataImportProvider provider = new XmlWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertFalse(result.hasParsedLines());
        assertTrue(result.hasErrors());
        assertEquals(WeatherImportError.Category.PARSE_ERROR, result.errors().get(0).getCategory());
    }

    @Test
    void xmlWithMissingElement_returnsParseError() throws IOException {
        final Path file = tempDir.resolve("missing.xml");
        Files.writeString(file, """
                <?xml version="1.0" encoding="UTF-8"?>
                <weatherDataRecords>
                    <record>
                        <airControlAreaCode>ACA-0001</airControlAreaCode>
                        <date>2026-05-20</date>
                        <windDirection>180</windDirection>
                        <windSpeed>12.5</windSpeed>
                    </record>
                    <record>
                        <date>2026-05-21</date>
                        <windDirection>90</windDirection>
                        <windSpeed>8.0</windSpeed>
                    </record>
                </weatherDataRecords>
                """);

        final XmlWeatherDataImportProvider provider = new XmlWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertEquals(1, result.parsedLines().size());
        assertEquals(1, result.errors().size());
    }

    @Test
    void xmlWithInvalidNumber_returnsParseError() throws IOException {
        final Path file = tempDir.resolve("badnum.xml");
        Files.writeString(file, """
                <?xml version="1.0" encoding="UTF-8"?>
                <weatherDataRecords>
                    <record>
                        <airControlAreaCode>ACA-0001</airControlAreaCode>
                        <date>2026-05-20</date>
                        <windDirection>notanumber</windDirection>
                        <windSpeed>12.5</windSpeed>
                    </record>
                </weatherDataRecords>
                """);

        final XmlWeatherDataImportProvider provider = new XmlWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertFalse(result.hasParsedLines());
        assertTrue(result.hasErrors());
    }

    @Test
    void xmlEmptyRecords_returnsNoLinesOrError() throws IOException {
        final Path file = tempDir.resolve("empty.xml");
        Files.writeString(file, """
                <?xml version="1.0" encoding="UTF-8"?>
                <weatherDataRecords>
                </weatherDataRecords>
                """);

        final XmlWeatherDataImportProvider provider = new XmlWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertFalse(result.hasParsedLines());
        assertFalse(result.hasErrors());
    }

    @Test
    void fileNotFound_returnsParseError() {
        final XmlWeatherDataImportProvider provider =
                new XmlWeatherDataImportProvider(tempDir.resolve("nonexistent.xml").toString());
        final ParseResult result = provider.getWeatherData();

        assertFalse(result.hasParsedLines());
        assertTrue(result.hasErrors());
        assertEquals(WeatherImportError.Category.PARSE_ERROR, result.errors().get(0).getCategory());
    }

    @Test
    void xmlWithMixedValidAndInvalid_returnsPartialResult() throws IOException {
        final Path file = tempDir.resolve("mixed.xml");
        Files.writeString(file, """
                <?xml version="1.0" encoding="UTF-8"?>
                <weatherDataRecords>
                    <record>
                        <airControlAreaCode>ACA-0001</airControlAreaCode>
                        <date>2026-05-20</date>
                        <windDirection>180</windDirection>
                        <windSpeed>12.5</windSpeed>
                    </record>
                    <record>
                        <airControlAreaCode></airControlAreaCode>
                        <date>2026-05-21</date>
                        <windDirection>90</windDirection>
                        <windSpeed>8.0</windSpeed>
                    </record>
                    <record>
                        <airControlAreaCode>ACA-0003</airControlAreaCode>
                        <date>2026-05-22</date>
                        <windDirection>45</windDirection>
                        <windSpeed>15.3</windSpeed>
                    </record>
                </weatherDataRecords>
                """);

        final XmlWeatherDataImportProvider provider = new XmlWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertEquals(2, result.parsedLines().size());
        assertEquals(1, result.errors().size());
    }

    @Test
    void xmlWithoutWeatherDataRecordsRoot_returnsNoRecords() throws IOException {
        final Path file = tempDir.resolve("other.xml");
        Files.writeString(file, """
                <?xml version="1.0" encoding="UTF-8"?>
                <someOtherRoot>
                    <record>
                        <airControlAreaCode>ACA-0001</airControlAreaCode>
                        <date>2026-05-20</date>
                        <windDirection>180</windDirection>
                        <windSpeed>12.5</windSpeed>
                    </record>
                </someOtherRoot>
                """);

        final XmlWeatherDataImportProvider provider = new XmlWeatherDataImportProvider(file.toString());
        final ParseResult result = provider.getWeatherData();

        assertTrue(result.hasParsedLines());
        assertEquals(1, result.parsedLines().size());
    }
}
