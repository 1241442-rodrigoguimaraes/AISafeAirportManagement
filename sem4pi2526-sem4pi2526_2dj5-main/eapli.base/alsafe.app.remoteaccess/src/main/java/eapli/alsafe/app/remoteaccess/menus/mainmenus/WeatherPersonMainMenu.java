package eapli.alsafe.app.remoteaccess.menus.mainmenus;

import eapli.alsafe.Application;
import eapli.alsafe.app.remoteaccess.menus.ClientBaseUI;
import eapli.alsafe.app.remoteaccess.menus.actions.ConsultWeatherDataActionRemote;
import eapli.alsafe.app.remoteaccess.menus.actions.ImportBulkWeatherDataActionRemote;
import eapli.alsafe.app.remoteaccess.menus.actions.LogoutAction;
import eapli.alsafe.app.remoteaccess.menus.actions.RegisterWeatherDataActionRemote;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.presentation.console.menu.HorizontalMenuRenderer;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.MenuRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class WeatherPersonMainMenu extends ClientBaseUI {

    private final TCPClient client;

    private boolean loggedOut = false;

    private static final String SEPARATOR_LABEL = "--------------";

    private static final int EXIT_OPTION = 0;

    private static final int IMPORT_BULK_WEATHER_OPTION = 1;

    private static final int REGISTER_WEATHER_OPTION = 2;

    private static final int CONSULT_WEATHER_OPTION = 3;

    private static final int LOGOUT_OPTION = 4;

    public WeatherPersonMainMenu(final TCPClient client) {
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

        mainMenu.addItem(IMPORT_BULK_WEATHER_OPTION, "Import Bulk Weather Data", () -> {
            new ImportBulkWeatherDataActionRemote(client).execute();
            return false;
        });
        mainMenu.addItem(REGISTER_WEATHER_OPTION, "Register Weather Data", () -> {
            new RegisterWeatherDataActionRemote(client).execute();
            return false;
        });
        mainMenu.addItem(CONSULT_WEATHER_OPTION, "Consult Weather Data", () -> {
            new ConsultWeatherDataActionRemote(client).execute();
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
