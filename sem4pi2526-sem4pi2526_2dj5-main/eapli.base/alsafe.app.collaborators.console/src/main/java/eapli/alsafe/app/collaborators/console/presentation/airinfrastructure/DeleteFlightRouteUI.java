package eapli.alsafe.app.collaborators.console.presentation.airinfrastructure;

import eapli.alsafe.airinfrastructure.application.DeleteFlightRouteController;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DeleteFlightRouteUI extends AbstractUI {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFlightRouteUI.class);

    private final DeleteFlightRouteController controller = new DeleteFlightRouteController();

    @Override
    protected boolean doShow() {

        try {
            String date = Console.readNonEmptyLine("Enter the date from which you would like to delete a flight route onwards (DD/MM/YYYY): ", "The date cannot be empty: ");

            while (!checkDate(date)) {
                date = Console.readLine("Invalid date format or the date is in the past. Please, enter the date in the format DD/MM/YYYY: ");
            }

            List<String> flightRoutes = new ArrayList<>(this.controller.getCompanyRoutes(date));

            final SelectWidget<String> selectorAp = new SelectWidget<>("Select a flight route to delete:", flightRoutes);
            selectorAp.show();
            String flightRoute = selectorAp.selectedElement();

            if (flightRoute == null) {
                System.out.println("No flight route selected. Aborting flight route deletion...");
                return false;
            }

            if (this.controller.deleteFlightRoute(flightRoute) == 0) {
                System.out.println("Flight route deleted successfully!");
            } else {
                System.out.println("Error deleting flight route.");
            }
        } catch (final IllegalArgumentException | IntegrityViolationException e) {
            System.out.println("Validation Error: " + e.getMessage());
        } catch (Exception ex) {
            LOGGER.error("Error performing the operation", ex);
            System.out.println("Unfortunately, there was an unexpected error in the application. Please, try again and if the problem persists, contact your system administrator.");
        }

        return false;
    }

    private boolean checkDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate parsed = LocalDate.parse(date, formatter);
        return !parsed.isBefore(LocalDate.now());
    }

    @Override
    public String headline() {
        return "Delete Flight Route";
    }
}
