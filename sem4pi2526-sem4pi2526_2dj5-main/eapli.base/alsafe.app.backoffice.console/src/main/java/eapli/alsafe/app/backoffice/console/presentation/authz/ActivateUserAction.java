package eapli.alsafe.app.backoffice.console.presentation.authz;

import eapli.framework.actions.Action;

public class ActivateUserAction implements Action {

    @Override
    public boolean execute() {
        return new ActivateUserUI().show();
    }
}
