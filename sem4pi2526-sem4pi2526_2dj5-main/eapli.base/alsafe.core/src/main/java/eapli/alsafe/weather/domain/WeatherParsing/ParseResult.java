package eapli.alsafe.weather.domain.WeatherParsing;

import java.util.List;

public class ParseResult {

    private final List<ParsedWeatherDataLine> parsedLines;
    private final List<WeatherImportError> errors;

    public ParseResult(final List<ParsedWeatherDataLine> parsedLines, final List<WeatherImportError> errors) {
        this.parsedLines = parsedLines;
        this.errors = errors;
    }

    public List<ParsedWeatherDataLine> parsedLines() {
        return parsedLines;
    }

    public List<WeatherImportError> errors() {
        return errors;
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    public boolean hasParsedLines() {
        return parsedLines != null && !parsedLines.isEmpty();
    }
}
