package eapli.alsafe.app.backoffice.console.presentation.weather;

import eapli.alsafe.weather.domain.WeatherParsing.BulkImportResult;
import eapli.alsafe.weather.application.ImportBulkWeatherDataController;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

public class ImportBulkWeatherDataUI extends AbstractUI {

    private final ImportBulkWeatherDataController theController =
            new ImportBulkWeatherDataController();

    @Override
    protected boolean doShow() {
        while (true) {
            try {
                final String filePath = Console.readLine("File path:");

                final BulkImportResult result = theController.importBulkWeatherData(filePath);
                System.out.println();
                System.out.println(result);

                if (theController.exportBulkImportResult(result, filePath)) {
                    System.out.println("\nImport result exported successfully.");
                } else {
                    System.out.println("\nFailed to export import result.");
                }

                return false;
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println();
            }
        }
    }

    @Override
    public String headline() {
        return "Import Bulk Weather Data";
    }
}
