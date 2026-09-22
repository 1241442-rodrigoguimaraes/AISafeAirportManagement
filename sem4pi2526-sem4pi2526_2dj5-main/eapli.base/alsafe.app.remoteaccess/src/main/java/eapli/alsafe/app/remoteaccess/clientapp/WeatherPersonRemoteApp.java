package eapli.alsafe.app.remoteaccess.clientapp;

import eapli.alsafe.app.common.console.BaseApp;
import eapli.alsafe.app.remoteaccess.menus.AuthenticationMenu;
import eapli.alsafe.app.remoteaccess.menus.mainmenus.WeatherPersonMainMenu;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.alsafe.usermanagement.domain.AlSafePasswordPolicy;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.pubsub.EventDispatcher;

public final class WeatherPersonRemoteApp extends BaseApp {

    private static final String SERVER_IP = "127.0.0.1";

    private static final int SERVER_PORT = 2223;

    private WeatherPersonRemoteApp() {}

    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("Remote Weather Person App");
        System.out.println("(C) 2026");
        System.out.println("=====================================");

        new WeatherPersonRemoteApp().run(args);
    }

    @Override
    protected void doMain(final String[] args) {
        AuthzRegistry.configure(PersistenceContext.repositories().users(),new AlSafePasswordPolicy(), new PlainTextEncoder());
        boolean keepRunning = true;
        while (keepRunning) {
            TCPClient client = new TCPClient(SERVER_IP, SERVER_PORT);

            boolean authenticated = AuthenticationMenu.execute(client, Roles.WEATHER_PERSON);

            if (authenticated) {
                boolean wantsLogout = new WeatherPersonMainMenu(client).show();
                if (!wantsLogout) keepRunning = false;
            } else {
                keepRunning = false;
            }
        }
    }

    @Override
    protected String appTitle() {
        return "Weather Person Menu";
    }

    @Override
    protected String appGoodbye() {
        return "Weather Person Menu";
    }

    @Override
    protected void doSetupEventHandlers(final EventDispatcher dispatcher) {}
}
