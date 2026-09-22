package eapli.alsafe.app.backoffice.console.presentation.menus;

import eapli.alsafe.app.backoffice.console.presentation.authz.AddUserUI;
import eapli.alsafe.app.backoffice.console.presentation.authz.ActivateUserAction;
import eapli.alsafe.app.backoffice.console.presentation.authz.DeactivateUserAction;
import eapli.alsafe.app.backoffice.console.presentation.authz.ListUsersAction;
import eapli.alsafe.app.backoffice.console.presentation.alsafeuser.AcceptRefuseSignupRequestAction;
import eapli.framework.actions.Action;
import eapli.framework.actions.Actions;
import eapli.framework.actions.menu.Menu;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;
import eapli.framework.presentation.console.menu.MenuItemRenderer;

public class UserMenuAction implements Action {
    private static final int ADD_USER_OPTION = 1;
    private static final int LIST_USERS_OPTION = 2;
    private static final int ACTIVATE_USER_OPTION = 3;
    private static final int DEACTIVATE_USER_OPTION = 4;
    private static final int ACCEPT_REFUSE_SIGNUP_REQUEST_OPTION = 5;
    private static final int EXIT_OPTION = 0;

    @Override
    public boolean execute() {
        final var menu = new Menu("Users >");

        menu.addItem(ADD_USER_OPTION, "Add User", new AddUserUI()::show);
        menu.addItem(LIST_USERS_OPTION, "List all Users", new ListUsersAction());
        menu.addItem(ACTIVATE_USER_OPTION, "Activate User", new ActivateUserAction());
        menu.addItem(DEACTIVATE_USER_OPTION, "Deactivate User", new DeactivateUserAction());
        menu.addItem(ACCEPT_REFUSE_SIGNUP_REQUEST_OPTION, "Accept/Refuse Signup Request",
                new AcceptRefuseSignupRequestAction());
        menu.addItem(EXIT_OPTION, "Return", Actions.FAIL);

        final var renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
        return renderer.render();
    }
}
