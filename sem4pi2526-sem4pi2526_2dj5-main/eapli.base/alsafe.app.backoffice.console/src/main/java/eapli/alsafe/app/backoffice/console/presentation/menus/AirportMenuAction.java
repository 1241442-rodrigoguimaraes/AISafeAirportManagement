package eapli.alsafe.app.backoffice.console.presentation.menus;

import eapli.alsafe.app.backoffice.console.presentation.airports.ListAirportAction;
import eapli.alsafe.app.backoffice.console.presentation.airports.RegisterAirportAction;
import eapli.framework.actions.Action;
import eapli.framework.actions.Actions;
import eapli.framework.actions.menu.Menu;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class AirportMenuAction implements Action {

    private static final int REGISTER_AIRPORT_OPTION = 1;
    private static final int LIST_AIRPORTS_OPTION = 2;
    private static final int EXIT_OPTION = 0;

    @Override
    public boolean execute() {
        final var menu = new Menu("Airports >");

        menu.addItem(REGISTER_AIRPORT_OPTION, "Register Airport", new RegisterAirportAction());
        menu.addItem(LIST_AIRPORTS_OPTION, "List all Airports", new ListAirportAction());
        menu.addItem(EXIT_OPTION, "Return", Actions.FAIL);

        final var renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
        return renderer.render();
    }
}
