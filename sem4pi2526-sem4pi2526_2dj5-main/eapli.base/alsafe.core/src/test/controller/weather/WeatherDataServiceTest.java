package controller.weather;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaBoundaries;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaMinimumFuel;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.weather.application.WeatherDataService;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.alsafe.weather.domain.WeatherParsing.BulkImportResult;
import eapli.alsafe.weather.domain.WeatherParsing.ParseResult;
import eapli.alsafe.weather.domain.WeatherParsing.ParsedWeatherDataLine;
import eapli.alsafe.weather.domain.WeatherParsing.WeatherImportError;
import eapli.alsafe.weather.repositories.WeatherDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class WeatherDataServiceTest {

    private static class FakeWeatherDataRepository implements WeatherDataRepository {
        private final List<WeatherData> store = new ArrayList<>();

        @Override
        public WeatherData save(WeatherData entity) {
            store.add(entity);
            return entity;
        }

        @Override
        public Iterable<WeatherData> findAll() { return store; }

        @Override
        public Optional<WeatherData> ofIdentity(Long id) { return Optional.empty(); }

        @Override
        public void delete(WeatherData entity) {}

        @Override
        public void deleteOfIdentity(Long id) {}

        @Override
        public long count() { return store.size(); }

        @Override
        public Iterable<WeatherData> findByAirControlAreaAndDateBetween(AirControlArea area, Calendar startDate, Calendar endDate) {
            return null;
        }

        @Override
        public boolean duplicates(WeatherData weatherData) {
            return false;
        }

        @Override
        public Optional<WeatherData> getAllWeatherDataFromFCOCollaborator(CollaboratorFCO collaboratorFCO) {
            return Optional.empty();
        }
    }

    private static class FakeAirControlAreaRepository implements AirControlAreaRepository {
        private final List<AirControlArea> store = new ArrayList<>();

        @Override
        public Iterable<AirControlArea> findAll() { return store; }

        @Override
        public Optional<AirControlArea> findById(AirControlAreaID id) {
            return store.stream().filter(a -> a.identity().equals(id)).findFirst();
        }

        @Override
        public Optional<AirControlArea> findByCode(String code) {
            return store.stream().filter(a -> a.identity().toString().equals(code)).findFirst();
        }

        @Override
        public AirControlArea save(AirControlArea entity) {
            store.add(entity);
            return entity;
        }

        @Override
        public Optional<AirControlArea> ofIdentity(AirControlAreaID id) { return Optional.empty(); }

        @Override
        public void delete(AirControlArea entity) {}

        @Override
        public void deleteOfIdentity(AirControlAreaID id) {}

        @Override
        public long count() { return store.size(); }
    }

    private FakeWeatherDataRepository weatherRepo;
    private FakeAirControlAreaRepository areaRepo;
    private WeatherDataService service;
    private AirControlArea area;

    @BeforeEach
    void setUp() {
        weatherRepo = new FakeWeatherDataRepository();
        areaRepo = new FakeAirControlAreaRepository();
        service = new WeatherDataService(weatherRepo, areaRepo);

        area = new AirControlArea(new AirControlAreaID(1L), "Area 1",
                new AirControlAreaBoundaries(List.of(
                        new Coordinate(0.0, 0.0), new Coordinate(1.0, 0.0),
                        new Coordinate(0.0, 1.0), new Coordinate(1.0, 1.0))),
                new AirControlAreaMinimumFuel(100.0));
        areaRepo.save(area);
    }

    @Test
    void allValidLines_importsAllSuccessfully() {
        List<ParsedWeatherDataLine> lines = List.of(
                new ParsedWeatherDataLine("ACA-0001", "2026-05-20", 180, 12.5, 1),
                new ParsedWeatherDataLine("ACA-0001", "2026-05-21", 90, 8.0, 2));
        ParseResult parseResult = new ParseResult(lines, List.of());

        BulkImportResult result = service.buildBulkWeatherData(parseResult);

        assertEquals(2, result.successCount());
        assertEquals(0, result.errors().size());
        assertEquals(2, weatherRepo.count());
    }

    @Test
    void lineWithNonExistentArea_reportsAreaNotFound() {
        List<ParsedWeatherDataLine> lines = List.of(
                new ParsedWeatherDataLine("ACA-9999", "2026-05-20", 180, 12.5, 1));
        ParseResult parseResult = new ParseResult(lines, List.of());

        BulkImportResult result = service.buildBulkWeatherData(parseResult);

        assertEquals(0, result.successCount());
        assertEquals(1, result.errors().size());
        assertEquals(WeatherImportError.Category.AREA_NOT_FOUND, result.errors().get(0).getCategory());
    }

    @Test
    void lineWithInvalidWindDirection_reportsInvalidWindDirection() {
        List<ParsedWeatherDataLine> lines = List.of(
                new ParsedWeatherDataLine("ACA-0001", "2026-05-20", 400, 12.5, 1));
        ParseResult parseResult = new ParseResult(lines, List.of());

        BulkImportResult result = service.buildBulkWeatherData(parseResult);

        assertEquals(0, result.successCount());
        assertEquals(1, result.errors().size());
        assertEquals(WeatherImportError.Category.INVALID_WIND_DIRECTION, result.errors().get(0).getCategory());
    }

    @Test
    void lineWithNegativeWindSpeed_reportsInvalidWindSpeed() {
        List<ParsedWeatherDataLine> lines = List.of(
                new ParsedWeatherDataLine("ACA-0001", "2026-05-20", 180, -5.0, 1));
        ParseResult parseResult = new ParseResult(lines, List.of());

        BulkImportResult result = service.buildBulkWeatherData(parseResult);

        assertEquals(0, result.successCount());
        assertEquals(1, result.errors().size());
        assertEquals(WeatherImportError.Category.INVALID_WIND_SPEED, result.errors().get(0).getCategory());
    }

    @Test
    void lineWithFutureDate_reportsFutureDate() {
        Calendar future = Calendar.getInstance();
        future.add(Calendar.YEAR, 1);
        int year = future.get(Calendar.YEAR);
        String futureDateStr = year + "-06-01";

        List<ParsedWeatherDataLine> lines = List.of(
                new ParsedWeatherDataLine("ACA-0001", futureDateStr, 180, 12.5, 1));
        ParseResult parseResult = new ParseResult(lines, List.of());

        BulkImportResult result = service.buildBulkWeatherData(parseResult);

        assertEquals(0, result.successCount());
        assertEquals(1, result.errors().size());
        assertEquals(WeatherImportError.Category.FUTURE_DATE, result.errors().get(0).getCategory());
    }

    @Test
    void lineWithInvalidDateFormat_reportsInvalidDate() {
        List<ParsedWeatherDataLine> lines = List.of(
                new ParsedWeatherDataLine("ACA-0001", "not-a-date", 180, 12.5, 1));
        ParseResult parseResult = new ParseResult(lines, List.of());

        BulkImportResult result = service.buildBulkWeatherData(parseResult);

        assertEquals(0, result.successCount());
        assertEquals(1, result.errors().size());
        assertEquals(WeatherImportError.Category.INVALID_DATE, result.errors().get(0).getCategory());
    }

    @Test
    void parseErrorsFromProvider_arePreservedInResult() {
        List<ParsedWeatherDataLine> lines = List.of(
                new ParsedWeatherDataLine("ACA-0001", "2026-05-20", 180, 12.5, 1));
        List<WeatherImportError> parseErrors = List.of(
                new WeatherImportError(WeatherImportError.Category.PARSE_ERROR, "raw", 2, "Bad format"));
        ParseResult parseResult = new ParseResult(lines, parseErrors);

        BulkImportResult result = service.buildBulkWeatherData(parseResult);

        assertEquals(1, result.successCount());
        assertEquals(1, result.errors().size());
        assertEquals(WeatherImportError.Category.PARSE_ERROR, result.errors().get(0).getCategory());
    }

    @Test
    void mixedValidAndInvalidLines_importsOnlyValid() {
        List<ParsedWeatherDataLine> lines = List.of(
                new ParsedWeatherDataLine("ACA-0001", "2026-05-20", 180, 12.5, 1),
                new ParsedWeatherDataLine("ACA-9999", "2026-05-20", 90, 8.0, 2),
                new ParsedWeatherDataLine("ACA-0001", "2026-05-21", 45, 15.3, 3));
        ParseResult parseResult = new ParseResult(lines, List.of());

        BulkImportResult result = service.buildBulkWeatherData(parseResult);

        assertEquals(2, result.successCount());
        assertEquals(1, result.errors().size());
        assertEquals(WeatherImportError.Category.AREA_NOT_FOUND, result.errors().get(0).getCategory());
    }

    @Test
    void noParsedLines_returnsError() {
        ParseResult parseResult = new ParseResult(List.of(), List.of());

        BulkImportResult result = service.buildBulkWeatherData(parseResult);

        assertEquals(0, result.successCount());
        assertEquals(1, result.errors().size());
        assertEquals(WeatherImportError.Category.UNKNOWN_ERROR, result.errors().get(0).getCategory());
    }
}
