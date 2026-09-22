package eapli.alsafe.app.backoffice.console.presentation.menus;

import eapli.alsafe.app.backoffice.console.presentation.weather.ConsultWeatherDataAction;
import eapli.alsafe.app.backoffice.console.presentation.weather.ImportBulkWeatherDataAction;
import eapli.alsafe.app.backoffice.console.presentation.weather.RegisterWeatherDataAction;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.actions.Actions;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

public class WeatherMenuAction extends Menu {

    private static final String MENU_TITLE = "Weather >";

    private static final int EXIT_OPTION = 0;
    private static final int REGISTER_WEATHER_DATA_OPTION = 1;
    private static final int IMPORT_BULK_WEATHER_DATA_OPTION = 2;
    private static final int CONSULT_WEATHER_DATA_OPTION = 3;

    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    public WeatherMenuAction() {
        super(MENU_TITLE);
        buildWeatherMenu();
    }

    private void buildWeatherMenu() {
        if (authz.isAuthenticatedUserAuthorizedTo(Roles.WEATHER_PERSON)) {
            addItem(MenuItem.of(REGISTER_WEATHER_DATA_OPTION, "Register Weather Data",
                    new RegisterWeatherDataAction()));
            addItem(MenuItem.of(IMPORT_BULK_WEATHER_DATA_OPTION, "Import Bulk Weather Data",
                    new ImportBulkWeatherDataAction()));
        }
        if (authz.isAuthenticatedUserAuthorizedTo(Roles.WEATHER_PERSON, Roles.PILOT, Roles.FLIGHT_CONTROL_OPERATOR)) {
            addItem(MenuItem.of(CONSULT_WEATHER_DATA_OPTION, "Consult Weather Data",
                    new ConsultWeatherDataAction()));
        }

        addItem(MenuItem.of(EXIT_OPTION, "Return", Actions.FAIL));
    }
}
