package eapli.alsafe.app.collaborators.console.presentation.menus;

import eapli.alsafe.app.collaborators.console.presentation.airinfrastructure.CreateFlightRouteAction;
import eapli.alsafe.app.collaborators.console.presentation.airinfrastructure.DeleteFlightRouteAction;
import eapli.framework.actions.Action;
import eapli.framework.actions.Actions;
import eapli.framework.actions.menu.Menu;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class FlightRouteMenuAction implements Action {

    private static final int CREATE_FLIGHT_ROUTE_OPTION = 1;
    private static final int DELETE_FLIGHT_ROUTE_OPTION = 2;
    private static final int EXIT_OPTION = 0;

    @Override
    public boolean execute() {
        final var menu = new Menu("Flight Route >");

        menu.addItem(CREATE_FLIGHT_ROUTE_OPTION, "Create Flight Route", new CreateFlightRouteAction());
        menu.addItem(DELETE_FLIGHT_ROUTE_OPTION, "Delete Flight Route", new DeleteFlightRouteAction());
        menu.addItem(EXIT_OPTION, "Return", Actions.FAIL);

        final var renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
        return renderer.render();
    }
}
