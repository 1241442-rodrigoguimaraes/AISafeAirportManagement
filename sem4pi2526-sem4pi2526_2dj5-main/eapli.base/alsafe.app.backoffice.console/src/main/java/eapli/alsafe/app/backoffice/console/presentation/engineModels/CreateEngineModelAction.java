package eapli.alsafe.app.backoffice.console.presentation.engineModels;

import eapli.framework.actions.Action;

public class CreateEngineModelAction implements Action {

    @Override
    public boolean execute() {
        return new CreateEngineModelUI().show();
    }
}
