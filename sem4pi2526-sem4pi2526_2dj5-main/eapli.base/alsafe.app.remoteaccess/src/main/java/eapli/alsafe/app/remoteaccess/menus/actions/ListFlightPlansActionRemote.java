package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ListFlightPlansActionRemote implements Action {

    private final TCPClient client;

    public ListFlightPlansActionRemote(final TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        try {
            client.sendPacket(new Packet((byte) 23, 0, new byte[0]));
            final Packet response = client.receivePacket();
            final String payload = new String(response.payload(), StandardCharsets.UTF_8);

            if (response.opCode() != (byte) 100) {
                System.out.println("Error: " + payload);
                return false;
            }

            printFlightPlans(payload);
            return true;
        } catch (final IOException e) {
            System.out.println("Error listing flight plans: " + e.getMessage());
            return false;
        }
    }

    private void printFlightPlans(final String payload) {
        if (payload.isBlank()) {
            System.out.println("No flight plans found.");
            return;
        }

        System.out.println("Flight Plans:");
        for (final String record : payload.split("\\|")) {
            final String[] fields = record.split(";", 7);
            if (fields.length == 7) {
                System.out.printf("- %s | Route: %s | Aircraft: %s | Departure: %s | Fuel: %s %s | Status: %s%n",
                        fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6]);
            } else {
                System.out.println("- " + record);
            }
        }
    }
}
