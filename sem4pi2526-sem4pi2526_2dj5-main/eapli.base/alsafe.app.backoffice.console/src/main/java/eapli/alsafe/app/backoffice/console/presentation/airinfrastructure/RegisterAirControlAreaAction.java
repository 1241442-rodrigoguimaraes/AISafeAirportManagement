package eapli.alsafe.app.backoffice.console.presentation.airinfrastructure;

import eapli.framework.actions.Action;

public class RegisterAirControlAreaAction implements Action {

    @Override
    public boolean execute() {
        return new RegisterAirControlAreaUI().show();
    }
}
