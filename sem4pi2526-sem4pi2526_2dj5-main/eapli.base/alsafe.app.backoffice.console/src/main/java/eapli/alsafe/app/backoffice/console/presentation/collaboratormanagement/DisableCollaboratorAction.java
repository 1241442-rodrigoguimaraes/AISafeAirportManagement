package eapli.alsafe.app.backoffice.console.presentation.collaboratormanagement;

import eapli.framework.actions.Action;

public class DisableCollaboratorAction implements Action {
    @Override
    public boolean execute() {
        return new DisableCollaboratorUI().show();
    }
}
