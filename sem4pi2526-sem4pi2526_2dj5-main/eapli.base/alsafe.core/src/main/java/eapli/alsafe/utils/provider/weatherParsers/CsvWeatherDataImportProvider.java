package eapli.alsafe.utils.provider.weatherParsers;

import eapli.alsafe.utils.provider.interfaces.WeatherDataImportProvider;
import eapli.alsafe.weather.domain.WeatherParsing.ParseResult;
import eapli.alsafe.weather.domain.WeatherParsing.ParsedWeatherDataLine;
import eapli.alsafe.weather.domain.WeatherParsing.WeatherImportError;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CsvWeatherDataImportProvider implements WeatherDataImportProvider {

    private final String filePath;

    public CsvWeatherDataImportProvider(final String filePath) {
        this.filePath = Objects.requireNonNull(filePath, "File path cannot be null.");
    }

    @Override
    public ParseResult getWeatherData() {
        final List<ParsedWeatherDataLine> parsedLines = new ArrayList<>();
        final List<WeatherImportError> errors = new ArrayList<>();

        final Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    filePath, 0, "File not found: " + filePath));
            return new ParseResult(parsedLines, errors);
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            reader.readLine();

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                parseCsvRow(line, lineNumber, parsedLines, errors);
            }
        } catch (IOException e) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    filePath, 0, "Failed to read file: " + e.getMessage()));
        }

        return new ParseResult(parsedLines, errors);
    }

    private void parseCsvRow(final String line, final int lineNumber,
                             final List<ParsedWeatherDataLine> parsedLines,
                             final List<WeatherImportError> errors) {
        Objects.requireNonNull(line, "line");

        final String[] row = line.split(";", -1);

        if (row.length < 4) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    line, lineNumber,
                    "Insufficient fields. Expected: airControlAreaCode;date;windDirection;windSpeed"));
            return;
        }

        final String code = row[0].trim();
        if (code.isEmpty()) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    line, lineNumber,
                    "Air control area code is empty."));
            return;
        }

        try {
            final double windDirection = Double.parseDouble(row[2].trim());
            final double windSpeed = Double.parseDouble(row[3].trim());
            parsedLines.add(new ParsedWeatherDataLine(code, row[1].trim(),
                    windDirection, windSpeed, lineNumber));
        } catch (NumberFormatException e) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    line, lineNumber,
                    "Invalid numeric value: " + e.getMessage()));
        }
    }
}
