package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;
import eapli.framework.io.util.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SubmitFlightPlanActionRemote implements Action {

    private final TCPClient client;

    public SubmitFlightPlanActionRemote(final TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        return sendFlightPlanOperation((byte) 24, "Flight plan ID to submit: ");
    }

    private boolean sendFlightPlanOperation(final byte opCode, final String prompt) {
        try {
            new ListFlightPlansActionRemote(client).execute();
            final String flightPlanId = Console.readNonEmptyLine(prompt, "Flight plan ID is required").trim();
            final byte[] payload = flightPlanId.getBytes(StandardCharsets.UTF_8);
            client.sendPacket(new Packet(opCode, payload.length, payload));

            final Packet response = client.receivePacket();
            final String message = new String(response.payload(), StandardCharsets.UTF_8);
            System.out.println(message);
            return response.opCode() == (byte) 100;
        } catch (final IOException e) {
            System.out.println("Error submitting flight plan: " + e.getMessage());
            return false;
        }
    }
}
