package controller.weather;

import eapli.alsafe.weather.application.ImportBulkWeatherDataController;
import eapli.alsafe.weather.application.WeatherDataService;
import eapli.alsafe.weather.domain.WeatherParsing.BulkImportResult;
import eapli.alsafe.weather.domain.WeatherParsing.ParseResult;
import eapli.alsafe.weather.domain.WeatherParsing.WeatherImportError;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImportBulkWeatherDataControllerTest {

    private static class FakeAuthorizationService extends AuthorizationService {
        private boolean shouldThrow;

        public FakeAuthorizationService(boolean shouldThrow) {
            this.shouldThrow = shouldThrow;
        }

        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... actions) {
            if (shouldThrow) {
                throw new SecurityException("User is not authorized.");
            }
        }
    }

    private static class FakeWeatherDataService extends WeatherDataService {
        private final BulkImportResult result;

        public FakeWeatherDataService(BulkImportResult result) {
            super(null, null);
            this.result = result;
        }

        @Override
        public BulkImportResult buildBulkWeatherData(ParseResult parseResult) {
            return result;
        }
    }

    private ImportBulkWeatherDataController controller;

    @BeforeEach
    void setUp() {
        BulkImportResult successResult = new BulkImportResult(2, List.of());
        WeatherDataService fakeService = new FakeWeatherDataService(successResult);
        controller = new ImportBulkWeatherDataController(
                new FakeAuthorizationService(false), fakeService);
    }

    @Test
    void validImport_returnsBulkImportResult() {
        BulkImportResult result = controller.importBulkWeatherData("test.csv");

        assertNotNull(result);
        assertEquals(2, result.successCount());
        assertFalse(result.hasErrors());
    }

    @Test
    void importWithErrors_returnsBulkImportResultWithErrors() {
        List<WeatherImportError> errors = List.of(
                new WeatherImportError(WeatherImportError.Category.AREA_NOT_FOUND,
                        "ACA-9999", 1, "Area not found"));
        BulkImportResult errorResult = new BulkImportResult(1, errors);
        WeatherDataService fakeService = new FakeWeatherDataService(errorResult);
        controller = new ImportBulkWeatherDataController(
                new FakeAuthorizationService(false), fakeService);

        BulkImportResult result = controller.importBulkWeatherData("test.csv");

        assertEquals(1, result.successCount());
        assertTrue(result.hasErrors());
        assertEquals(1, result.errors().size());
        assertEquals(WeatherImportError.Category.AREA_NOT_FOUND, result.errors().get(0).getCategory());
    }

    @Test
    void unauthorizedUser_throwsSecurityException() {
        controller = new ImportBulkWeatherDataController(
                new FakeAuthorizationService(true), null);

        assertThrows(SecurityException.class,
                () -> controller.importBulkWeatherData("test.csv"));
    }
}
