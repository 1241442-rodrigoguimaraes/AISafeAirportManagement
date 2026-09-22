package eapli.alsafe.app.backoffice.console.presentation.collaboratormanagement;

import eapli.framework.actions.Action;

public class ListCollaboratorsAction implements Action {
    @Override
    public boolean execute() {
        return new ListCollaboratorsUI().show();
    }
}
