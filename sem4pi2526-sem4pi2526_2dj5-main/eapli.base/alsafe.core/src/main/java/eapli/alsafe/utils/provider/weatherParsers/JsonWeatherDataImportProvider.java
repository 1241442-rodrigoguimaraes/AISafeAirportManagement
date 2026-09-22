package eapli.alsafe.utils.provider.weatherParsers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import eapli.alsafe.utils.provider.interfaces.WeatherDataImportProvider;
import eapli.alsafe.weather.domain.WeatherParsing.ParseResult;
import eapli.alsafe.weather.domain.WeatherParsing.ParsedWeatherDataLine;
import eapli.alsafe.weather.domain.WeatherParsing.WeatherImportError;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JsonWeatherDataImportProvider implements WeatherDataImportProvider {

    private final String filePath;

    public JsonWeatherDataImportProvider(final String filePath) {
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

        try {
            final String content = Files.readString(path, StandardCharsets.UTF_8);
            final JsonArray array = new Gson().fromJson(content, JsonArray.class);
            if (array == null) {
                errors.add(new WeatherImportError(
                        WeatherImportError.Category.PARSE_ERROR,
                        content, 0, "JSON content is not an array."));
                return new ParseResult(parsedLines, errors);
            }

            int lineNumber = 0;
            for (JsonElement element : array) {
                lineNumber++;
                parseJsonObject(element, lineNumber, parsedLines, errors);
            }
        } catch (JsonSyntaxException e) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    filePath, 0, "Invalid JSON syntax: " + e.getMessage()));
        } catch (IOException e) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    filePath, 0, "Failed to read file: " + e.getMessage()));
        }

        return new ParseResult(parsedLines, errors);
    }

    private void parseJsonObject(final JsonElement element, final int lineNumber,
                                  final List<ParsedWeatherDataLine> parsedLines,
                                  final List<WeatherImportError> errors) {
        if (!element.isJsonObject()) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    element.toString(), lineNumber,
                    "Expected a JSON object at index " + lineNumber + "."));
            return;
        }

        final JsonObject obj = element.getAsJsonObject();

        final String code = getStringOrNull(obj, "airControlAreaCode");
        if (code == null || code.isEmpty()) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    obj.toString(), lineNumber,
                    "Missing or empty 'airControlAreaCode'."));
            return;
        }

        final String date = getStringOrNull(obj, "date");
        if (date == null || date.isEmpty()) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    obj.toString(), lineNumber,
                    "Missing or empty 'date'."));
            return;
        }

        final JsonElement windDirEl = obj.get("windDirection");
        final JsonElement windSpeedEl = obj.get("windSpeed");
        if (windDirEl == null || windDirEl.isJsonNull()) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    obj.toString(), lineNumber,
                    "Missing 'windDirection'."));
            return;
        }
        if (windSpeedEl == null || windSpeedEl.isJsonNull()) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    obj.toString(), lineNumber,
                    "Missing 'windSpeed'."));
            return;
        }
        try {
            final double windDirection = windDirEl.getAsDouble();
            final double windSpeed = windSpeedEl.getAsDouble();
            parsedLines.add(new ParsedWeatherDataLine(code, date, windDirection, windSpeed, lineNumber));
        } catch (NumberFormatException | UnsupportedOperationException e) {
            errors.add(new WeatherImportError(
                    WeatherImportError.Category.PARSE_ERROR,
                    obj.toString(), lineNumber,
                    "Invalid numeric value for windDirection or windSpeed: " + e.getMessage()));
        }
    }

    private String getStringOrNull(final JsonObject obj, final String key) {
        final JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) {
            return null;
        }
        return el.getAsString();
    }
}
