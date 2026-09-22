package eapli.alsafe.app.collaborators.console.presentation.menus;

import eapli.alsafe.Application;
import eapli.alsafe.app.backoffice.console.presentation.weather.ConsultWeatherDataAction;
import eapli.alsafe.app.collaborators.console.presentation.CollaboratorsBaseUI;
import eapli.alsafe.app.collaborators.console.presentation.aircraft.AddAircraftAction;
import eapli.alsafe.app.collaborators.console.presentation.aircraft.DecommissionAircraftAction;
import eapli.alsafe.app.collaborators.console.presentation.aircraft.ListFleetAction;
import eapli.alsafe.app.common.console.presentation.authz.MyUserMenu;
import eapli.alsafe.app.collaborators.console.presentation.pilot.AddPilotAction;
import eapli.alsafe.app.collaborators.console.presentation.pilot.ListPilotRosterAction;
import eapli.alsafe.app.collaborators.console.presentation.pilot.RemovePilotAction;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.presentation.console.ExitWithMessageAction;
import eapli.framework.presentation.console.menu.HorizontalMenuRenderer;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.MenuRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

/**
 * @author Paulo Gandra Sousa
 */
public class CollaboratorsMainMenu extends CollaboratorsBaseUI {

    private static final String SEPARATOR_LABEL = "--------------";

    private static final int EXIT_OPTION = 0;

    // MAIN MENU
    private static final int MY_USER_OPTION = 1;
    private static final int ADD_AIRCRAFT_OPTION = 2;
    private static final int DECOMMISSION_AIRCRAFT_OPTION = 3;
    private static final int LIST_FLEET_OPTION = 4;
    private static final int FLIGHT_ROUTE_OPTION = 5;
    private static final int ADD_PILOT_OPTION = 6;
    private static final int LIST_PILOTS = 7;
    private static final int REMOVE_PILOT_OPTION = 8;
    private static final int WEATHER_SEARCH = 2;
    private static final int CREATE_FLIGHT_PLAN_OPTION = 3;
    private static final int SIMULATION_REPORTS_OPTION = 4;

    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    @Override
    public boolean show() {
        drawFormTitle();
        return doShow();
    }

    /**
     * @return true if the user selected the exit option
     */
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

    private Menu buildMainMenu() {
        final Menu mainMenu = new Menu();

        final Menu myUserMenu = new MyUserMenu();
        mainMenu.addSubMenu(MY_USER_OPTION, myUserMenu);

        if (!Application.settings().isMenuLayoutHorizontal()) {
            mainMenu.addItem(MenuItem.separator(SEPARATOR_LABEL));
        }

        if (authz.isAuthenticatedUserAuthorizedTo(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR)) {
            mainMenu.addItem(ADD_AIRCRAFT_OPTION, "Add Aircraft to Fleet", new AddAircraftAction());
            mainMenu.addItem(DECOMMISSION_AIRCRAFT_OPTION, "Decommission Aircraft", new DecommissionAircraftAction());
            mainMenu.addItem(LIST_FLEET_OPTION, "List Fleet", new ListFleetAction());
            mainMenu.addItem(FLIGHT_ROUTE_OPTION, "Flight Routes", new FlightRouteMenuAction());
            mainMenu.addItem(ADD_PILOT_OPTION, "Add Pilot", new AddPilotAction());
            mainMenu.addItem(LIST_PILOTS, "List Pilots", new ListPilotRosterAction());
            mainMenu.addItem(REMOVE_PILOT_OPTION, "Remove Pilot", new RemovePilotAction());
        }

        if (authz.isAuthenticatedUserAuthorizedTo(Roles.PILOT, Roles.FLIGHT_CONTROL_OPERATOR)) {
            mainMenu.addItem(WEATHER_SEARCH,"Consult Weather Data", new ConsultWeatherDataAction());
        }

        if(authz.isAuthenticatedUserAuthorizedTo(Roles.PILOT)) {
            mainMenu.addItem(CREATE_FLIGHT_PLAN_OPTION, "Create Flight Plan", new FlightPlanMenuAction());
        }

        if (authz.isAuthenticatedUserAuthorizedTo(Roles.FLIGHT_CONTROL_OPERATOR)) {
            mainMenu.addItem(SIMULATION_REPORTS_OPTION, "Simulation Reports", new SimulationReportMenuAction());
        }

        if (!Application.settings().isMenuLayoutHorizontal()) {
            mainMenu.addItem(MenuItem.separator(SEPARATOR_LABEL));
        }

        mainMenu.addItem(EXIT_OPTION, "Exit", new ExitWithMessageAction("Bye, Bye"));

        return mainMenu;
    }
}
