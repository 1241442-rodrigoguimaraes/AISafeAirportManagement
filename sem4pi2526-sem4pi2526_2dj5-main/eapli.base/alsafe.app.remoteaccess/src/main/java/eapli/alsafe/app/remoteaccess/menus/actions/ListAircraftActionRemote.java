package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;
import eapli.framework.io.util.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ListAircraftActionRemote implements Action {

    private final TCPClient client;

    public ListAircraftActionRemote(final TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        System.out.println("\n=== Fleet Listing ===");
        System.out.println("1 - List full fleet");
        System.out.println("2 - Filter by aircraft model");
        System.out.println("3 - Filter by maker");
        System.out.println("4 - Filter by capacity");
        System.out.println("0 - Exit");

        final int option = Console.readInteger("Option:");

        if (option == 0) return true;

        String payloadStr = "";
        if (option == 2) {
            payloadStr = Console.readLine("Aircraft model:");
        } else if (option == 3) {
            payloadStr = Console.readLine("Maker:");
        } else if (option == 4) {
            payloadStr = String.valueOf(Console.readInteger("Capacity:"));
        } else if (option != 1) {
            System.out.println("Invalid option.");
            return false;
        }

        try {
            String data = option + (payloadStr.isEmpty() ? "" : ";" + payloadStr);
            byte[] payload = data.getBytes(StandardCharsets.UTF_8);

            client.sendPacket(new Packet((byte) 6, payload.length, payload));
            Packet response = client.receivePacket();

            if (response.opCode() == (byte) 101) {
                System.out.println("\nNo aircraft found.");
                return true;
            }

            String msg = new String(response.payload(), StandardCharsets.UTF_8);
            System.out.println("\n=== Results ===");
            System.out.println(msg);

            return true;
        } catch (IOException e) {
            System.err.println("Error fetching fleet: " + e.getMessage());
            return false;
        }
    }
}
