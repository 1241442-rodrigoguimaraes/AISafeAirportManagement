package eapli.alsafe.weather.application;

import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.alsafe.utils.provider.interfaces.WeatherDataExporterProvider;
import eapli.alsafe.utils.provider.interfaces.WeatherDataImportProvider;
import eapli.alsafe.utils.provider.weatherParsers.CsvWeatherDataImportProvider;
import eapli.alsafe.utils.provider.weatherParsers.ExcelWeatherDataImportProvider;
import eapli.alsafe.utils.provider.weatherParsers.JsonWeatherDataImportProvider;
import eapli.alsafe.utils.provider.weatherParsers.TXTWeatherDataExporterProvider;
import eapli.alsafe.utils.provider.weatherParsers.XmlWeatherDataImportProvider;
import eapli.alsafe.weather.domain.WeatherParsing.BulkImportResult;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class ImportBulkWeatherDataController {

    private final AuthorizationService authz;
    private final WeatherDataService weatherDataService;

    public ImportBulkWeatherDataController() {
        this.authz = AuthzRegistry.authorizationService();
        this.weatherDataService = new WeatherDataService(
                PersistenceContext.repositories().weatherData(),
                PersistenceContext.repositories().areas());
    }

    public ImportBulkWeatherDataController(final AuthorizationService authz,
                                            final WeatherDataService weatherDataService) {
        this.authz = authz;
        this.weatherDataService = weatherDataService;
    }

    public BulkImportResult importBulkWeatherData(final String filePath) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.WEATHER_PERSON);

        final WeatherDataImportProvider provider = providerForFile(filePath);
        final var parseResult = provider.getWeatherData();

        return weatherDataService.buildBulkWeatherData(parseResult);
    }

    private WeatherDataImportProvider providerForFile(final String filePath) {
        final String ext = extensionOf(filePath);
        return switch (ext) {
            case "csv" -> new CsvWeatherDataImportProvider(filePath);
            case "json" -> new JsonWeatherDataImportProvider(filePath);
            case "xml" -> new XmlWeatherDataImportProvider(filePath);
            case "xlsx" -> new ExcelWeatherDataImportProvider(filePath);
            default -> throw new IllegalArgumentException(
                    "Unsupported format: ." + ext + ". Supported: .csv, .json, .xml, .xlsx");
        };
    }

    private String extensionOf(final String filePath) {
        final int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filePath.length() - 1) {
            return "";
        }
        return filePath.substring(dotIndex + 1).toLowerCase();
    }

    public boolean exportBulkImportResult(BulkImportResult result, String filePath) {
        WeatherDataExporterProvider exporterProvider = new TXTWeatherDataExporterProvider(filePath);
        return exporterProvider.exportWeatherData(result);
    }
}
