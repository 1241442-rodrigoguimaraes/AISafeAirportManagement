package eapli.alsafe.app.remoteaccess.menus;

import eapli.alsafe.app.remoteaccess.menus.actions.AuthenticateAction;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.infrastructure.authz.domain.model.Role;

public class AuthenticationMenu {

    public static boolean execute(TCPClient client, Role... requiredRoles) {
        boolean authenticated = new AuthenticateAction(client, requiredRoles).execute();

        if (!authenticated) {
            client.disconnect();
        }

        return authenticated;
    }
}