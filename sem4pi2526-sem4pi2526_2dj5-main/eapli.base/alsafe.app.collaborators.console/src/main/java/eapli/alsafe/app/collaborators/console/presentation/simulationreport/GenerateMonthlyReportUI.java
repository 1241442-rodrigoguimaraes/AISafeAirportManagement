package eapli.alsafe.app.collaborators.console.presentation.simulationreport;

import eapli.alsafe.simulationreport.application.GenerateMonthlyReportController;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.io.IOException;
import java.nio.file.Path;
import java.time.YearMonth;

@SuppressWarnings("squid:S106")
public class GenerateMonthlyReportUI extends AbstractUI {

    private final GenerateMonthlyReportController controller = new GenerateMonthlyReportController();

    @Override
    protected boolean doShow() {
        try {
            final int year = Console.readInteger("Year (e.g. 2026): ");
            final int month = readMonth();
            final Path reportPath = controller.generateMonthlyReport(YearMonth.of(year, month));
            System.out.printf("%nMonthly statistics report generated successfully: %s%n", reportPath);
        } catch (final SecurityException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (final IOException e) {
            System.out.println("Error generating monthly statistics report: " + e.getMessage());
        }

        return false;
    }

    private int readMonth() {
        int month = Console.readInteger("Month (1-12): ");
        while (month < 1 || month > 12) {
            month = Console.readInteger("Invalid month. Please enter a number between 1 and 12: ");
        }
        return month;
    }

    @Override
    public String headline() {
        return "Generate Monthly Statistics Report";
    }
}
