package eapli.alsafe.app.collaborators.console.presentation.pilot;

import eapli.framework.actions.Action;

public class AddPilotAction implements Action {

    @Override
    public boolean execute() {
        return new AddPilotUI().show();
    }
}
