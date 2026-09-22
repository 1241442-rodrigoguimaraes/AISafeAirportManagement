package eapli.alsafe.app.collaborators.console.presentation.menus;


import eapli.alsafe.app.collaborators.console.presentation.flightplan.*;
import eapli.framework.actions.Action;
import eapli.framework.actions.Actions;
import eapli.framework.actions.menu.Menu;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class FlightPlanMenuAction implements Action {

    private static final int CREATE_FLIGHT_PLAN = 1;
    private static final int SUBMIT_FLIGHT_PLAN = 2;
    private static final int CANCEL_FLIGHT_PLAN = 3;
    private static final int VALIDATE_FLIGHT_PLAN = 4;
    private static final int LIST_FLIGHT_PLAN = 5;
    private static final int INSERT_WEATHER_DATA = 6;
    private static final int EXIT_OPTION = 0;

    @Override
    public boolean execute() {
        final var menu = new Menu("Flight Plan >");

        menu.addItem(CREATE_FLIGHT_PLAN, "Create Flight Plan", new CreateFlightPlanAction());
        menu.addItem(SUBMIT_FLIGHT_PLAN, "Submit Flight Plan", new SubmitFlightPlanAction());
        menu.addItem(CANCEL_FLIGHT_PLAN, "Cancel Flight Plan", new CancelFlightPlanAction());
        menu.addItem(VALIDATE_FLIGHT_PLAN, "Validate Flight Plan", new ValidateFlightPlanAction());
        menu.addItem(LIST_FLIGHT_PLAN, "List Flight Plan", new ListFlightPlanAction());
        menu.addItem(INSERT_WEATHER_DATA, "Insert Weather Data in Flight Plan", new InsertWeatherDataInFlightAction());
        menu.addItem(EXIT_OPTION, "Return", Actions.FAIL);

        final var renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
        return renderer.render();
    }
}
