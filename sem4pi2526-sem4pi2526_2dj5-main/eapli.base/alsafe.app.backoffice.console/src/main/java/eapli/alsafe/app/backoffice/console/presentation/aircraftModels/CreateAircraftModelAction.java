package eapli.alsafe.app.backoffice.console.presentation.aircraftModels;

import eapli.framework.actions.Action;

public class CreateAircraftModelAction implements Action {

    @Override
    public boolean execute() {
        return new CreateAircraftModelUI().show();
    }
}
