package eapli.alsafe.app.backoffice.console.presentation.companies;

import eapli.framework.actions.Action;

public class RegisterAirCompanyAction implements Action {

    @Override
    public boolean execute() {
        return new RegisterAirCompanyUI().show();
    }
}
