package eapli.alsafe.weather.domain.WeatherParsing;

import lombok.Getter;
import lombok.Setter;

public class WeatherImportError {
    public enum Category {
        PARSE_ERROR,
        INVALID_WIND_DIRECTION,
        INVALID_WIND_SPEED,
        INVALID_DATE,
        FUTURE_DATE,
        AREA_NOT_FOUND,
        DUPLICATE_ENTRY,
        UNKNOWN_ERROR
    }

    @Getter
    @Setter
    public final Category category;
    @Getter
    @Setter
    public final String rawLine;
    @Getter
    @Setter
    public int lineNumber;
    @Getter
    @Setter
    public String details;

    public WeatherImportError(Category category, String rawLine, int lineNumber, String details) {
        this.category = category;
        this.rawLine = rawLine;
        this.lineNumber = lineNumber;
        this.details = details;
    }
}
