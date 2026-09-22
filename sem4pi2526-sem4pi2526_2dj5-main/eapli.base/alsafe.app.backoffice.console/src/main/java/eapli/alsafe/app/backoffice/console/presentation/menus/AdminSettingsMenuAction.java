package eapli.alsafe.app.backoffice.console.presentation.menus;

import eapli.framework.actions.Action;
import eapli.framework.actions.Actions;
import eapli.framework.actions.menu.Menu;
import eapli.framework.presentation.console.ShowMessageAction;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;
import eapli.framework.presentation.console.menu.MenuItemRenderer;

public class AdminSettingsMenuAction implements Action {
    private static final int SET_KITCHEN_ALERT_LIMIT_OPTION = 1;
    private static final int EXIT_OPTION = 0;

    @Override
    public boolean execute() {
        final var menu = new Menu("Administrator Settings >");

        menu.addItem(SET_KITCHEN_ALERT_LIMIT_OPTION, "Set kitchen alert limit", new ShowMessageAction("Not implemented yet"));
        menu.addItem(EXIT_OPTION, "Return", Actions.FAIL);

        final var renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
        return renderer.render();
    }
}