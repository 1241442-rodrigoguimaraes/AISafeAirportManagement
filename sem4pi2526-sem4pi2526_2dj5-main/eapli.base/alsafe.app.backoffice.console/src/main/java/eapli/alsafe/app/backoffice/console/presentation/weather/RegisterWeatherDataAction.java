package eapli.alsafe.app.backoffice.console.presentation.weather;

import eapli.framework.actions.Action;

public class RegisterWeatherDataAction implements Action {
    @Override
    public boolean execute() {
        return new RegisterWeatherDataUI().show();
    }
}
