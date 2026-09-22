package eapli.alsafe.app.collaborators.console.presentation.pilot;

import eapli.framework.actions.Action;

public class ListPilotRosterAction implements Action {

    @Override
    public boolean execute() {
        return new ListPilotRosterUI().show();
    }
}
