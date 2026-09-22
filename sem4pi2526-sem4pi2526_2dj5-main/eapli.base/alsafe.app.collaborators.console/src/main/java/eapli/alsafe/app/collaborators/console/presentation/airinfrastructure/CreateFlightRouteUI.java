package eapli.alsafe.app.collaborators.console.presentation.airinfrastructure;

import eapli.alsafe.airinfrastructure.application.CreateFlightRouteController;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CreateFlightRouteUI extends AbstractUI {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateFlightRouteUI.class);

    private final CreateFlightRouteController controller = new CreateFlightRouteController();

    @Override
    protected boolean doShow() {

        try {
            List<String> airports = new ArrayList<>(this.controller.getAllAirportsList());
            if (airports.isEmpty()) {
                System.out.println("No airports available. Please, create airports before creating a flight route.");
                return false;
            }

            final SelectWidget<String> selectorAp1 = new SelectWidget<>("Select Starting Airport:", airports);
            selectorAp1.show();
            String startingAirport = selectorAp1.selectedElement();

            if (startingAirport == null) {
                System.out.println("No airport selected. Aborting flight route creation...");
                return false;
            }

            airports.remove(startingAirport);
            if (airports.isEmpty()) {
                System.out.println("Please, create more airports before creating a flight route.");
                return false;
            }

            final SelectWidget<String> selectorAp2 = new SelectWidget<>("Select Ending Airport:", airports);
            selectorAp2.show();
            String endingAirport = selectorAp2.selectedElement();

            if (endingAirport == null) {
                System.out.println("No airport selected. Aborting flight route creation...");
                return false;
            }
            
            String numbers = Console.readNonEmptyLine("Insert the number of the route (maximum 4 characters): ", "Please, insert a valid number (maximum 4 characters): ");
            while(numbers.length() > 4 || !numbers.matches("[0-9]+")) numbers = Console.readLine("Invalid number. Please, insert the numbers correctly (maximum 4 characters).");

            System.out.println(this.controller.createFlightRoute(numbers, startingAirport, endingAirport));
            System.out.println("Flight route created successfully!");
        } catch (IntegrityViolationException | ConcurrencyException ex) {
            LOGGER.error("Error performing the operation", ex);
            System.out.println("Unfortunately, there was an unexpected error in the application. Please, try again and if the problem persists, contact your system administrator.");
        } catch (final IllegalArgumentException e) {
            System.out.println("Validation Error: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Create Flight Route";
    }
}
