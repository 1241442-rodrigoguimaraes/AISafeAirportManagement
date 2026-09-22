package eapli.alsafe.app.remoteaccess.clientapp;

import eapli.alsafe.app.common.console.BaseApp;
import eapli.alsafe.app.remoteaccess.menus.AuthenticationMenu;
import eapli.alsafe.app.remoteaccess.menus.mainmenus.PilotMainMenu;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.alsafe.usermanagement.domain.AlSafePasswordPolicy;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.pubsub.EventDispatcher;

public final class PilotRemoteApp extends BaseApp {

    private static final String SERVER_IP = "127.0.0.1";

    private static final int SERVER_PORT = 2223;

    private PilotRemoteApp() {}

    public static void main(final String[] args) {
        System.out.println("=====================================");
        System.out.println("Remote Pilot App");
        System.out.println("(C) 2026");
        System.out.println("=====================================");

        new PilotRemoteApp().run(args);
    }

    @Override
    protected void doMain(final String[] args) {
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new AlSafePasswordPolicy(), new PlainTextEncoder());

        boolean keepRunning = true;
        while (keepRunning) {
            final TCPClient client = new TCPClient(SERVER_IP, SERVER_PORT);
            final boolean authenticated = AuthenticationMenu.execute(client, Roles.PILOT);

            if (authenticated) {
                final boolean wantsLogout = new PilotMainMenu(client).show();
                if (!wantsLogout) {
                    keepRunning = false;
                }
            } else {
                keepRunning = false;
            }
        }
    }

    @Override
    protected String appTitle() {
        return "Pilot Menu";
    }

    @Override
    protected String appGoodbye() {
        return "Pilot Menu";
    }

    @Override
    protected void doSetupEventHandlers(final EventDispatcher dispatcher) {}
}
