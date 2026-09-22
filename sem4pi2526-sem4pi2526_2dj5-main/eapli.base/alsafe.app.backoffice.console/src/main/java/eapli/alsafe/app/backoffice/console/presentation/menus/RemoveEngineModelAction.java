package eapli.alsafe.app.backoffice.console.presentation.menus;

import eapli.alsafe.app.backoffice.console.presentation.aircraftModels.RemoveEngineModelUI;
import eapli.framework.actions.Action;

public class RemoveEngineModelAction implements Action {

    @Override
    public boolean execute() {
        return new RemoveEngineModelUI().show();
    }
}


