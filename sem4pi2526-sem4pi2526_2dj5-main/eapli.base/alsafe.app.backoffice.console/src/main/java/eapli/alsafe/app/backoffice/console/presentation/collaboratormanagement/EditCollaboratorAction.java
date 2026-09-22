package eapli.alsafe.app.backoffice.console.presentation.collaboratormanagement;

import eapli.framework.actions.Action;

public class EditCollaboratorAction implements Action {
    @Override
    public boolean execute() {
        return new EditCollaboratorUI().show();
    }
}
