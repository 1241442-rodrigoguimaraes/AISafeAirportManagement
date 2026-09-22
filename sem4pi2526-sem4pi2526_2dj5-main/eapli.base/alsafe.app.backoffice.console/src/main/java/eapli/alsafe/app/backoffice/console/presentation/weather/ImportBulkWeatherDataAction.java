package eapli.alsafe.app.backoffice.console.presentation.weather;

import eapli.framework.actions.Action;

public class ImportBulkWeatherDataAction implements Action {

    @Override
    public boolean execute() {
        return new ImportBulkWeatherDataUI().show();
    }
}
