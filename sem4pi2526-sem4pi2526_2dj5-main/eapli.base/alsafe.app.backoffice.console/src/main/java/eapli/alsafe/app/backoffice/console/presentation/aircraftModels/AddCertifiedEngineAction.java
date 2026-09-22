package eapli.alsafe.app.backoffice.console.presentation.aircraftModels;

import eapli.framework.actions.Action;

public class AddCertifiedEngineAction implements Action {

    @Override
    public boolean execute() {
        return new AddCertifiedEngineUI().show();
    }
}
