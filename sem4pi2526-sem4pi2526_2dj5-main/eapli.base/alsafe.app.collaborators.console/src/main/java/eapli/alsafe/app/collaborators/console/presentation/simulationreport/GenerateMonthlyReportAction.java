package eapli.alsafe.app.collaborators.console.presentation.simulationreport;

import eapli.framework.actions.Action;

public class GenerateMonthlyReportAction implements Action {

    @Override
    public boolean execute() {
        return new GenerateMonthlyReportUI().show();
    }
}
