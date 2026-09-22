package eapli.alsafe.weather.domain.WeatherParsing;

import lombok.Getter;
import lombok.Setter;

public class ParsedWeatherDataLine {
    @Getter
    @Setter
    public String code;
    @Getter
    @Setter
    public String date;
    @Getter
    @Setter
    public double windDirection;
    @Getter
    @Setter
    public double windSpeed;
    @Getter
    @Setter
    public Integer line;

    public ParsedWeatherDataLine(String code, String date, double windDirection, double windSpeed, Integer line) {
        this.code = code;
        this.date = date;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
        this.line = line;
    }

    @Override
    public String toString() {
        return code + ";" + date + ";" + windDirection + ";" + windSpeed;
    }
}
