package eapli.alsafe.app.collaborators.console.presentation.menus;

import eapli.alsafe.app.collaborators.console.presentation.simulationreport.GenerateSimulationReportAction;
import eapli.alsafe.app.collaborators.console.presentation.simulationreport.GenerateMonthlyReportAction;
import eapli.framework.actions.Action;
import eapli.framework.actions.Actions;
import eapli.framework.actions.menu.Menu;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class SimulationReportMenuAction implements Action {

    private static final int GENERATE_SIMULATION_REPORT = 1;
    private static final int GENERATE_MONTHLY_REPORT = 2;
    private static final int EXIT_OPTION = 0;

    @Override
    public boolean execute() {
        final var menu = new Menu("Simulation Reports >");

        menu.addItem(GENERATE_SIMULATION_REPORT, "Generate Simulation Report", new GenerateSimulationReportAction());
        menu.addItem(GENERATE_MONTHLY_REPORT, "Generate Monthly Statistics Report", new GenerateMonthlyReportAction());
        menu.addItem(EXIT_OPTION, "Return", Actions.FAIL);

        final var renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
        return renderer.render();
    }
}
