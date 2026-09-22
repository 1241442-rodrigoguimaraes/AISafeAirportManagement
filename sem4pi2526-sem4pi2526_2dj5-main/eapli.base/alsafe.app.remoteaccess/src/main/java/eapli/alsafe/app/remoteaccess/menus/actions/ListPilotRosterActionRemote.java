package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ListPilotRosterActionRemote implements Action {

    private final TCPClient client;

    public ListPilotRosterActionRemote(final TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        try {
            client.sendPacket(new Packet((byte) 11, 0, null));
            Packet response = client.receivePacket();

            if (response.opCode() == (byte) 101) {
                System.out.println(new String(response.payload(), StandardCharsets.UTF_8));
                return false;
            }

            String msg = new String(response.payload(), StandardCharsets.UTF_8);
            System.out.println("\n=== Pilot Roster ===");
            for (String line : msg.split("\n")) {
                if (!line.isBlank()) {
                    System.out.println(line);
                }
            }

        } catch (IOException e) {
            System.err.println("Error fetching pilot roster: " + e.getMessage());
            return false;
        }

        return false;
    }
}
