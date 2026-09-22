package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;
import eapli.framework.io.util.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ValidateFlightPlanActionRemote implements Action {

    private final TCPClient client;

    public ValidateFlightPlanActionRemote(final TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        try {
            new ListFlightPlansActionRemote(client).execute();
            final String flightPlanId = Console.readNonEmptyLine("Flight plan ID to validate: ", "Flight plan ID is required").trim();
            final byte[] payload = flightPlanId.getBytes(StandardCharsets.UTF_8);
            client.sendPacket(new Packet((byte) 26, payload.length, payload));

            final Packet response = client.receivePacket();
            final String message = new String(response.payload(), StandardCharsets.UTF_8);
            System.out.println(message);
            return response.opCode() == (byte) 100;
        } catch (final IOException e) {
            System.out.println("Error validating flight plan: " + e.getMessage());
            return false;
        }
    }
}
