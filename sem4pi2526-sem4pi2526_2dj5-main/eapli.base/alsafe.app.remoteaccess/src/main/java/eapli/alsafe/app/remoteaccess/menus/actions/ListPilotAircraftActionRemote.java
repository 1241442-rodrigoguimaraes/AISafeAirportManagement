package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ListPilotAircraftActionRemote implements Action {

    private final TCPClient client;

    public ListPilotAircraftActionRemote(final TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        try {
            client.sendPacket(new Packet((byte) 21, 0, new byte[0]));
            final Packet response = client.receivePacket();
            final String payload = new String(response.payload(), StandardCharsets.UTF_8);

            if (response.opCode() != (byte) 100) {
                System.out.println("Error: " + payload);
                return false;
            }

            printEntries("Available Aircraft", payload);
            return true;
        } catch (final IOException e) {
            System.out.println("Error listing aircraft: " + e.getMessage());
            return false;
        }
    }

    private void printEntries(final String title, final String payload) {
        System.out.println(title + ":");
        if (payload.isBlank()) {
            System.out.println("No records found.");
            return;
        }
        for (final String entry : payload.split("\\|")) {
            System.out.println("- " + entry);
        }
    }
}
