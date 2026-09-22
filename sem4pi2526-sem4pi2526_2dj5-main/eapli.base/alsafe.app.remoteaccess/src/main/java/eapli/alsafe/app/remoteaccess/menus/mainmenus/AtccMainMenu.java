package eapli.alsafe.app.remoteaccess.menus.mainmenus;

import eapli.alsafe.Application;
import eapli.alsafe.app.remoteaccess.menus.ClientBaseUI;
import eapli.alsafe.app.remoteaccess.menus.actions.*;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.presentation.console.menu.HorizontalMenuRenderer;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.MenuRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class AtccMainMenu extends ClientBaseUI {

    private final TCPClient client;

    private boolean loggedOut = false;

    private static final String SEPARATOR_LABEL = "--------------";

    private static final int EXIT_OPTION = 0;

    private static final int ADD_AIRCRAFT_OPTION = 1;

    private static final int DECOMMISSION_AIRCRAFT_OPTION = 2;

    private static final int LIST_AIRCRAFT_OPTION = 3;

    private static final int CREATE_FLIGHT_ROUTE_OPTION = 4;

    private static final int DELETE_FLIGHT_ROUTE_OPTION = 5;

    private static final int ADD_PILOT_OPTION = 6;

    private static final int LIST_PILOT_ROSTER_OPTION = 7;

    private static final int REMOVE_PILOT_OPTION = 8;

    private static final int LOGOUT_OPTION = 9;

    public AtccMainMenu(final TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean show() {
        drawFormTitle();
        return doShow();
    }

    @Override
    public boolean doShow() {
        boolean exit;

        do {
            final var menu = buildMainMenu();
            final MenuRenderer renderer = Application.settings().isMenuLayoutHorizontal()
                    ? new HorizontalMenuRenderer(menu, MenuItemRenderer.DEFAULT)
                    : new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);

            exit = renderer.render();

        } while (!exit && !loggedOut);

        return loggedOut;
    }

    private Menu buildMainMenu() {
        final Menu mainMenu = new Menu();

        if (!Application.settings().isMenuLayoutHorizontal()) mainMenu.addItem(MenuItem.separator(SEPARATOR_LABEL));

        mainMenu.addItem(ADD_AIRCRAFT_OPTION, "Add Aircraft to a Company", () -> {
            new AddAircraftActionRemote(client).execute();
            return false;
        });
        mainMenu.addItem(DECOMMISSION_AIRCRAFT_OPTION, "Decommission an Aircraft", () -> {
            new DecommissionAircraftActionRemote(client).execute();
            return false;
        });
        mainMenu.addItem(LIST_AIRCRAFT_OPTION, "List Aircraft of a Fleet", () -> {
            new ListAircraftActionRemote(client).execute();
            return false;
        });
        mainMenu.addItem(CREATE_FLIGHT_ROUTE_OPTION, "Create a Flight Route", () -> {
            new CreateFlightRouteActionRemote(client).execute();
            return false;
        });
        mainMenu.addItem(DELETE_FLIGHT_ROUTE_OPTION, "Delete a Flight Route", () -> {
            new DeleteFlightRouteActionRemote(client).execute();
            return false;
        });
        mainMenu.addItem(ADD_PILOT_OPTION, "Add a Pilot", () -> {
            new AddPilotActionRemote(client).execute();
            return false;
        });
        mainMenu.addItem(LIST_PILOT_ROSTER_OPTION, "List Pilot Roster", () -> {
            new ListPilotRosterActionRemote(client).execute();
            return false;
        });
        mainMenu.addItem(REMOVE_PILOT_OPTION, "Remove a Pilot", () -> {
            new RemovePilotActionRemote(client).execute();
            return false;
        });
        mainMenu.addItem(LOGOUT_OPTION, "Logout", () -> {
            boolean result = new LogoutAction(client).execute();
            if (result) this.loggedOut = true;
            return true;
        });

        if (!Application.settings().isMenuLayoutHorizontal()) mainMenu.addItem(MenuItem.separator(SEPARATOR_LABEL));

        mainMenu.addItem(EXIT_OPTION, "Exit", () -> {
            if (client != null) client.disconnect();
            return true;
        });

        return mainMenu;
    }
}
