package eapli.alsafe.app.tester.console.presentation.menus;

import eapli.alsafe.Application;
import eapli.alsafe.app.backoffice.console.presentation.menus.AdminSettingsMenuAction;
import eapli.alsafe.app.backoffice.console.presentation.menus.AirCompanyMenuAction;
import eapli.alsafe.app.backoffice.console.presentation.menus.AirControlAreaMenuAction;
import eapli.alsafe.app.backoffice.console.presentation.menus.AirportMenuAction;
import eapli.alsafe.app.backoffice.console.presentation.menus.CollaboratorMenuAction;
import eapli.alsafe.app.backoffice.console.presentation.menus.UserMenuAction;
import eapli.alsafe.app.backoffice.console.presentation.menus.AircraftModelMenuAction;
import eapli.alsafe.app.backoffice.console.presentation.menus.WeatherMenuAction;
import eapli.alsafe.app.backoffice.console.presentation.engineModels.CreateEngineModelAction;
import eapli.alsafe.app.collaborators.console.presentation.aircraft.AddAircraftAction;
import eapli.alsafe.app.collaborators.console.presentation.aircraft.DecommissionAircraftAction;
import eapli.alsafe.app.collaborators.console.presentation.aircraft.ListFleetAction;
import eapli.alsafe.app.common.console.presentation.authz.MyUserMenu;
import eapli.alsafe.app.collaborators.console.presentation.menus.FlightRouteMenuAction;
import eapli.alsafe.app.collaborators.console.presentation.menus.FlightPlanMenuAction;
import eapli.alsafe.app.collaborators.console.presentation.menus.SimulationReportMenuAction;
import eapli.alsafe.app.collaborators.console.presentation.pilot.AddPilotAction;
import eapli.alsafe.app.collaborators.console.presentation.pilot.ListPilotRosterAction;
import eapli.alsafe.app.collaborators.console.presentation.pilot.RemovePilotAction;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.ExitWithMessageAction;
import eapli.framework.presentation.console.menu.HorizontalMenuRenderer;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.MenuRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class TesterMainMenu extends AbstractUI {

    private static final int EXIT_OPTION = 0;

    private static final int MY_USER_OPTION = 1;
    private static final int USERS_OPTION = 2;
    private static final int AREAS_OPTION = 3;
    private static final int AIRPORTS_OPTION = 4;
    private static final int COMPANIES_OPTION = 5;
    private static final int COLLABORATORS_OPTION = 6;
    private static final int ENGINE_MODELS_OPTION = 7;
    private static final int AIRCRAFT_MODELS_OPTION = 8;
    private static final int WEATHER_OPTION = 9;
    private static final int AIRCRAFT_OPTION = 10;
    private static final int FLIGHT_ROUTE_OPTION = 11;
    private static final int PILOT_OPTION = 12;
    private static final int FLIGHT_PLAN_CREATION = 13;
    private static final int SIMULATION_REPORTS_OPTION = 14;
    private static final int SETTINGS_OPTION = 15;

    private static final String SEPARATOR_LABEL = "--------------";

    @Override
    public boolean show() {
        drawFormTitle();
        return doShow();
    }

    @Override
    public boolean doShow() {
        final var menu = buildMainMenu();
        final MenuRenderer renderer;
        if (Application.settings().isMenuLayoutHorizontal()) {
            renderer = new HorizontalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
        } else {
            renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
        }
        return renderer.render();
    }

    @Override
    public String headline() {
        return "Tester App (H2 Local) — All features enabled";
    }

    private Menu buildMainMenu() {
        final var mainMenu = new Menu();

        mainMenu.addSubMenu(MY_USER_OPTION, new MyUserMenu());

        if (!Application.settings().isMenuLayoutHorizontal()) {
            mainMenu.addItem(MenuItem.separator(SEPARATOR_LABEL));
        }

        mainMenu.addItem(USERS_OPTION, "Users >", new UserMenuAction());
        mainMenu.addItem(AREAS_OPTION, "Air Control Areas >", new AirControlAreaMenuAction());
        mainMenu.addItem(AIRPORTS_OPTION, "Airports >", new AirportMenuAction());
        mainMenu.addItem(COMPANIES_OPTION, "Air Transport Companies >", new AirCompanyMenuAction());
        mainMenu.addItem(COLLABORATORS_OPTION, "Collaborators >", new CollaboratorMenuAction());
        mainMenu.addItem(ENGINE_MODELS_OPTION, "Engine Models >", new CreateEngineModelAction());
        mainMenu.addItem(AIRCRAFT_MODELS_OPTION, "Aircraft Models >", new AircraftModelMenuAction());
        mainMenu.addSubMenu(WEATHER_OPTION, new WeatherMenuAction());
        mainMenu.addItem(AIRCRAFT_OPTION, "Aircraft >", this::showAircraftSubMenu);
        mainMenu.addItem(FLIGHT_ROUTE_OPTION, "Flight Routes >", new FlightRouteMenuAction());
        mainMenu.addItem(PILOT_OPTION, "Pilots >", this::showPilotSubMenu);
        mainMenu.addItem(FLIGHT_PLAN_CREATION, "Create Flight Plan >", new FlightPlanMenuAction());
        mainMenu.addItem(SIMULATION_REPORTS_OPTION, "Simulation Reports >", new SimulationReportMenuAction());
        mainMenu.addItem(SETTINGS_OPTION, "Settings >", new AdminSettingsMenuAction());

        if (!Application.settings().isMenuLayoutHorizontal()) {
            mainMenu.addItem(MenuItem.separator(SEPARATOR_LABEL));
        }

        mainMenu.addItem(EXIT_OPTION, "Exit", new ExitWithMessageAction("Bye, Bye"));

        return mainMenu;
    }

    private boolean showAircraftSubMenu() {
        final var menu = new Menu("Aircraft >");
        menu.addItem(1, "Add Aircraft to Fleet", new AddAircraftAction());
        menu.addItem(2, "Decommission Aircraft", new DecommissionAircraftAction());
        menu.addItem(3, "List Fleet", new ListFleetAction());
        menu.addItem(0, "Return", () -> true);

        final var renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
        return renderer.render();
    }

    private boolean showPilotSubMenu() {
        final var menu = new Menu("Pilots >");
        menu.addItem(1, "Add Pilot", new AddPilotAction());
        menu.addItem(2, "List Pilots", new ListPilotRosterAction());
        menu.addItem(3, "Remove Pilot", new RemovePilotAction());
        menu.addItem(0, "Return", () -> true);

        final var renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
        return renderer.render();
    }
}
