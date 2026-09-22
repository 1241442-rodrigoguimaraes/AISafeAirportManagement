package eapli.alsafe.app.backoffice.console.presentation.menus;

import eapli.alsafe.app.backoffice.console.presentation.companies.RegisterAirCompanyAction;
import eapli.framework.actions.Action;
import eapli.framework.actions.Actions;
import eapli.framework.actions.menu.Menu;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class AirCompanyMenuAction implements Action {

    private static final int REGISTER_COMPANY_OPTION = 1;
    private static final int EXIT_OPTION = 0;

    @Override
    public boolean execute() {
        final var menu = new Menu("Air Transport Company >");

        menu.addItem(REGISTER_COMPANY_OPTION, "Register Air Transport Company", new RegisterAirCompanyAction());
        menu.addItem(EXIT_OPTION, "Return", Actions.FAIL);

        final var renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
        return renderer.render();
    }
}
