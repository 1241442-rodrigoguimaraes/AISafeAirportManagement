package eapli.alsafe.app.collaborators.console.presentation.aircraft;

import eapli.framework.actions.Action;

public class DecommissionAircraftAction implements Action {
    @Override
    public boolean execute() {
        return new DecommissionAircraftUI().show();
    }
}
