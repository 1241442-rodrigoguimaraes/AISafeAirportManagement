package eapli.alsafe.app.collaborators.console.presentation.flightplan;

import eapli.framework.actions.Action;

public class CancelFlightPlanAction implements Action {

    @Override
    public boolean execute() {
        return new CancelFlightPlanUI().show();
    }
}
