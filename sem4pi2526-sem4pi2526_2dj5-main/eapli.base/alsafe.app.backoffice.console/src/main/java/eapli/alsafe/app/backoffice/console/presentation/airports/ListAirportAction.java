package eapli.alsafe.app.backoffice.console.presentation.airports;

import eapli.framework.actions.Action;

public class ListAirportAction implements Action {

    @Override
    public boolean execute() {
        new ListAirportUI().show();
        return false;
    }
}
