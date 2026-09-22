package eapli.alsafe.app.backoffice.console.presentation.collaboratormanagement;

import eapli.framework.actions.Action;

public class AddCollaboratorAction implements Action {
    @Override
    public boolean execute() {
        return new AddCollaboratorUI().show();
    }
}
