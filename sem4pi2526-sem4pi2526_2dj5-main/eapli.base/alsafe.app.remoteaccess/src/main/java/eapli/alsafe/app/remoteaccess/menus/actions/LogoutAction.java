package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;

import java.io.IOException;

public class LogoutAction implements Action {

    private final TCPClient client;

    public LogoutAction(final TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        try {
            client.sendPacket(new Packet((byte) 0, 0, new byte[0]));
        } catch (IOException ignored) {}

        client.disconnect();

        return true;
    }
}
