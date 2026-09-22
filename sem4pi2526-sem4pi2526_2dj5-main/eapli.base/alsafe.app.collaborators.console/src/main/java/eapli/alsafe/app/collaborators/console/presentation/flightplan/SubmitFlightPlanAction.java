package eapli.alsafe.app.collaborators.console.presentation.flightplan;

import eapli.framework.actions.Action;

public class SubmitFlightPlanAction implements Action {

    @Override
    public boolean execute() {
        return new SubmitFlightPlanUI().show();
    }
}
