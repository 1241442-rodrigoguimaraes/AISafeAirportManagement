package eapli.alsafe.app.remoteaccess.menus.mainmenus;

import eapli.alsafe.Application;
import eapli.alsafe.app.remoteaccess.menus.ClientBaseUI;
import eapli.alsafe.app.remoteaccess.menus.actions.CancelFlightPlanActionRemote;
import eapli.alsafe.app.remoteaccess.menus.actions.CreateFlightPlanActionRemote;
import eapli.alsafe.app.remoteaccess.menus.actions.InsertWeatherDataInFlightActionRemote;
import eapli.alsafe.app.remoteaccess.menus.actions.ListFlightPlansActionRemote;
import eapli.alsafe.app.remoteaccess.menus.actions.LogoutAction;
import eapli.alsafe.app.remoteaccess.menus.actions.PilotConsultWeatherDataActionRemote;
import eapli.alsafe.app.remoteaccess.menus.actions.SubmitFlightPlanActionRemote;
import eapli.alsafe.app.remoteaccess.menus.actions.ValidateFlightPlanActionRemote;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.presentation.console.menu.HorizontalMenuRenderer;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.MenuRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class PilotMainMenu extends ClientBaseUI {

    private static final String SEPARATOR_LABEL = "--------------";

    private static final int EXIT_OPTION = 0;
    private static final int CREATE_FLIGHT_PLAN_OPTION = 1;
    private static final int LIST_FLIGHT_PLANS_OPTION = 2;
    private static final int SUBMIT_FLIGHT_PLAN_OPTION = 3;
    private static final int CANCEL_FLIGHT_PLAN_OPTION = 4;
    private static final int INSERT_WEATHER_DATA_OPTION = 5;
    private static final int VALIDATE_FLIGHT_PLAN_OPTION = 6;
    private static final int CONSULT_WEATHER_OPTION = 7;
    private static final int LOGOUT_OPTION = 8;

    private final TCPClient client;
    private boolean loggedOut = false;

    public PilotMainMenu(final TCPClient client) {
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

        if (!Application.settings().isMenuLayoutHorizontal()) {
            mainMenu.addItem(MenuItem.separator(SEPARATOR_LABEL));
        }

        mainMenu.addItem(CREATE_FLIGHT_PLAN_OPTION, "Create Flight Plan", () -> {
            new CreateFlightPlanActionRemote(client).execute();
            return false;
        });
        mainMenu.addItem(LIST_FLIGHT_PLANS_OPTION, "List Flight Plans", () -> {
            new ListFlightPlansActionRemote(client).execute();
            return false;
        });
        mainMenu.addItem(SUBMIT_FLIGHT_PLAN_OPTION, "Submit Flight Plan", () -> {
            new SubmitFlightPlanActionRemote(client).execute();
            return false;
        });
        mainMenu.addItem(CANCEL_FLIGHT_PLAN_OPTION, "Cancel Flight Plan", () -> {
            new CancelFlightPlanActionRemote(client).execute();
            return false;
        });
        mainMenu.addItem(INSERT_WEATHER_DATA_OPTION, "Insert Weather Data in Flight Plan", () -> {
            new InsertWeatherDataInFlightActionRemote(client).execute();
            return false;
        });
        mainMenu.addItem(VALIDATE_FLIGHT_PLAN_OPTION, "Validate Flight Plan", () -> {
            new ValidateFlightPlanActionRemote(client).execute();
            return false;
        });
        mainMenu.addItem(CONSULT_WEATHER_OPTION, "Consult Weather Data", () -> {
            new PilotConsultWeatherDataActionRemote(client).execute();
            return false;
        });
        mainMenu.addItem(LOGOUT_OPTION, "Logout", () -> {
            final boolean result = new LogoutAction(client).execute();
            if (result) {
                this.loggedOut = true;
            }
            return true;
        });

        if (!Application.settings().isMenuLayoutHorizontal()) {
            mainMenu.addItem(MenuItem.separator(SEPARATOR_LABEL));
        }

        mainMenu.addItem(EXIT_OPTION, "Exit", () -> {
            if (client != null) {
                client.disconnect();
            }
            return true;
        });

        return mainMenu;
    }
}
