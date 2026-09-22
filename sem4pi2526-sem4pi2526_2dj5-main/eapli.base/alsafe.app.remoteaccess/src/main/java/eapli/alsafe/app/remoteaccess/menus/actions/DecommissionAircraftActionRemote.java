package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;
import eapli.framework.io.util.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DecommissionAircraftActionRemote implements Action {

    private final TCPClient client;

    public DecommissionAircraftActionRemote(final TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        try {
            client.sendPacket(new Packet((byte) 4, 0, null));
            Packet response = client.receivePacket();
            String msg = new String(response.payload(), StandardCharsets.UTF_8);

            if (response.opCode() == (byte) 101) {
                System.out.println("No active aircraft found. Going back to the main menu...");
                return true;
            }

            System.out.println(msg);
        } catch (IOException e) {
            System.err.println("Error fetching active aircraft: " + e.getMessage());
            return false;
        }

        try {
            String id = Console.readNonEmptyLine("Enter the ID of the aircraft to decommission from the fleet: ", "Please enter a valid ID: ");

            client.sendPacket(new Packet((byte) 5, id.getBytes().length, id.getBytes()));
            Packet response = client.receivePacket();
            String msg = new String(response.payload(), StandardCharsets.UTF_8);
            System.out.println(msg);

            return true;
        } catch (IOException e) {
            System.err.println("Error reading user input: " + e.getMessage());
            return false;
        }
    }
}
