package eapli.alsafe.app.collaborators.console.presentation.pilot;

import eapli.framework.actions.Action;

public class RemovePilotAction implements Action {

    @Override
    public boolean execute() {
        return new RemovePilotUI().show();
    }
}
