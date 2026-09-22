package eapli.alsafe.app.backoffice.console.presentation.menus;

import eapli.alsafe.app.backoffice.console.presentation.aircraftModels.AddCertifiedEngineAction;
import eapli.alsafe.app.backoffice.console.presentation.aircraftModels.CreateAircraftModelAction;
import eapli.framework.actions.Action;
import eapli.framework.actions.Actions;
import eapli.framework.actions.menu.Menu;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class AircraftModelMenuAction implements Action {

    private static final int CREATE_AIRCRAFT_MODEL_OPTION = 1;
    private static final int ADD_CERTIFIED_ENGINE_OPTION = 2;
    private static final int REMOVE_ENGINE_OPTION = 3;
    private static final int EXIT_OPTION = 0;

    @Override
    public boolean execute() {
        final var menu = new Menu("Aircraft Model >");

        menu.addItem(CREATE_AIRCRAFT_MODEL_OPTION, "Create Aircraft Model", new CreateAircraftModelAction());
        menu.addItem(ADD_CERTIFIED_ENGINE_OPTION, "Add Engine to an Aircraft Model", new AddCertifiedEngineAction());
        menu.addItem(REMOVE_ENGINE_OPTION, "Remove Engine of an Aircraft Model", new RemoveEngineModelAction());
        menu.addItem(EXIT_OPTION, "Return", Actions.FAIL);

        final var renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
        return renderer.render();
    }
}
