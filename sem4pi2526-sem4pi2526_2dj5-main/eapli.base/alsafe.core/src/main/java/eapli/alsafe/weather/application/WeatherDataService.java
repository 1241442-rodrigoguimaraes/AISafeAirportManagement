package eapli.alsafe.weather.application;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.alsafe.weather.domain.WeatherDataBuilder;
import eapli.alsafe.weather.domain.WeatherParsing.BulkImportResult;
import eapli.alsafe.weather.domain.WeatherParsing.ParseResult;
import eapli.alsafe.weather.domain.WeatherParsing.ParsedWeatherDataLine;
import eapli.alsafe.weather.domain.WeatherParsing.WeatherImportError;
import eapli.alsafe.weather.repositories.WeatherDataRepository;
import eapli.framework.time.util.Calendars;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

public class WeatherDataService {

    private final WeatherDataRepository weatherDataRepository;
    private final AirControlAreaRepository airControlAreaRepository;

    public WeatherDataService(final WeatherDataRepository weatherDataRepository,
                              final AirControlAreaRepository airControlAreaRepository) {
        this.weatherDataRepository = weatherDataRepository;
        this.airControlAreaRepository = airControlAreaRepository;
    }

    public BulkImportResult buildBulkWeatherData(final ParseResult parseResult) {
        final List<WeatherImportError> allErrors = new ArrayList<>();
        int successCount = 0;

        if (parseResult.hasErrors()) {
            allErrors.addAll(parseResult.errors());
        }

        if (!parseResult.hasParsedLines()) {
            if (!parseResult.hasErrors()) {
                allErrors.add(new WeatherImportError(
                        WeatherImportError.Category.UNKNOWN_ERROR,
                        "", -1, "No data provided. The weather data import provider did not return any data."));
            }
            return new BulkImportResult(successCount, allErrors);
        }

        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (ParsedWeatherDataLine parsedLine : parseResult.parsedLines()) {
            try {
                processLine(parsedLine, dateFormat);
                successCount++;
            } catch (WeatherImportException e) {
                allErrors.add(e.toImportError(parsedLine));
            }
        }

        return new BulkImportResult(successCount, allErrors);
    }

    private void processLine(final ParsedWeatherDataLine parsedLine,
                             final SimpleDateFormat dateFormat) {
        final AirControlArea area = findByCode(parsedLine.getCode())
                .orElseThrow(() -> new WeatherImportException(
                        WeatherImportError.Category.AREA_NOT_FOUND,
                        "The area code provided does not match any existing area in the system."));

        final Calendar date;
        try {
            date = Calendars.fromDate(dateFormat.parse(parsedLine.getDate()));
            if (date.after(Calendar.getInstance())) {
                throw new WeatherImportException(
                        WeatherImportError.Category.FUTURE_DATE,
                        "Date cannot be in the future: " + parsedLine.getDate());
            }
        } catch (ParseException e) {
            throw new WeatherImportException(
                    WeatherImportError.Category.INVALID_DATE,
                    "Invalid date format: " + parsedLine.getDate());
        }

        final int windDirection = getWindDirection(parsedLine);
        final double windSpeed = getWindSpeed(parsedLine);

        final WeatherData weatherData = new WeatherDataBuilder()
                .with(area, date, windDirection, windSpeed)
                .build();

        if (weatherDataRepository.duplicates(weatherData)) {
            throw new WeatherImportException(
                    WeatherImportError.Category.DUPLICATE_ENTRY,
                    "Duplicate weather data entry for area " + parsedLine.getCode() + " on date " + parsedLine.getDate());
        }else weatherDataRepository.save(weatherData);
    }

    private static double getWindSpeed(ParsedWeatherDataLine parsedLine) {
        final double windSpeed;
        try {
            windSpeed = parsedLine.getWindSpeed();
            if (windSpeed < 0) {
                throw new WeatherImportException(
                        WeatherImportError.Category.INVALID_WIND_SPEED,
                        "Wind speed must be greater than or equal to 0.");
            }
        }catch (WeatherImportException e){
            throw new WeatherImportException(
                    WeatherImportError.Category.INVALID_WIND_SPEED,
                    "Invalid wind speed: " + parsedLine.getWindSpeed());
        }
        return windSpeed;
    }

    private static int getWindDirection(ParsedWeatherDataLine parsedLine) {
        final int windDirection;
        try {
            windDirection =  (int) parsedLine.getWindDirection();
            if (windDirection < 0 || windDirection > 360) {
                throw new WeatherImportException(
                        WeatherImportError.Category.INVALID_WIND_DIRECTION,
                        "Wind direction must be between 0 and 360 degrees.");
            }
        }catch (WeatherImportException e){
            throw new WeatherImportException(
                    WeatherImportError.Category.INVALID_WIND_DIRECTION,
                    "Invalid wind direction: " + parsedLine.getWindDirection());
        }
        return windDirection;
    }

    private Optional<AirControlArea> findByCode(final String code) {
        return airControlAreaRepository.findByCode(code);
    }
}
