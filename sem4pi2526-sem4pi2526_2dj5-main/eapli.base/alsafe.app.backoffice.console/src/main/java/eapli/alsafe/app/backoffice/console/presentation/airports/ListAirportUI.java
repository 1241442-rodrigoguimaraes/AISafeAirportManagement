package eapli.alsafe.app.backoffice.console.presentation.airports;

import eapli.alsafe.airports.application.RegisterAirportController;
import eapli.alsafe.airports.domain.Airport;
import eapli.framework.presentation.console.AbstractUI;

public class ListAirportUI extends AbstractUI {
    private final RegisterAirportController controller = new RegisterAirportController();

    @Override
    public boolean doShow() {
        for (Airport airport : controller.allAirports()) {
            System.out.println(airport);
        }
        return false;
    }

    @Override
    public String headline() {
        return "List of Airports:";
    }
}

