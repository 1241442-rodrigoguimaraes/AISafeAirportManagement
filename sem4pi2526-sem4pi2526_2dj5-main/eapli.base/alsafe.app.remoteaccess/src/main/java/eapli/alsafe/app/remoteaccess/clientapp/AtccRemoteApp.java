package eapli.alsafe.app.remoteaccess.clientapp;

import eapli.alsafe.app.common.console.BaseApp;
import eapli.alsafe.app.remoteaccess.menus.AuthenticationMenu;
import eapli.alsafe.app.remoteaccess.menus.mainmenus.AtccMainMenu;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.alsafe.usermanagement.domain.AlSafePasswordPolicy;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.pubsub.EventDispatcher;

public class AtccRemoteApp extends BaseApp {

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 2223;

    private AtccRemoteApp() {}

    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("Remote Atcc App");
        System.out.println("(C) 2026");
        System.out.println("=====================================");

        new AtccRemoteApp().run(args);
    }

    @Override
    protected void doMain(final String[] args) {
        AuthzRegistry.configure(PersistenceContext.repositories().users(),new AlSafePasswordPolicy(), new PlainTextEncoder());
        boolean keepRunning = true;
        while (keepRunning) {
            TCPClient client = new TCPClient(SERVER_IP, SERVER_PORT);

            boolean authenticated = AuthenticationMenu.execute(client, Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR, Roles.ADMIN, Roles.BACKOFFICE_OPERATOR);

            if (authenticated) {
                boolean wantsLogout = new AtccMainMenu(client).show();
                if (!wantsLogout) keepRunning = false;
            } else {
                client.disconnect();
                keepRunning = false;
            }
        }
    }

    @Override
    protected String appTitle() {
        return "ATCC Menu";
    }

    @Override
    protected String appGoodbye() {
        return "ATCC Menu";
    }

    @Override
    protected void doSetupEventHandlers(final EventDispatcher dispatcher) {}
}
