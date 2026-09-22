package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;
import eapli.framework.io.util.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class CreateFlightRouteActionRemote implements Action {

    private final TCPClient client;

    public CreateFlightRouteActionRemote(final TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        try {
            client.sendPacket(new Packet((byte) 7, 0, null));
            Packet airportResp = client.receivePacket();

            if (airportResp.opCode() == (byte) 101) {
                System.out.println("Not enough airports available. Please, create airports before creating a flight route.");
                return false;
            }

            String msg = new String(airportResp.payload(), StandardCharsets.UTF_8);
            System.out.println(msg);
        } catch (IOException e) {
            System.err.println("Error fetching airports: " + e.getMessage());
            return false;
        }

        try {
            String first = Console.readNonEmptyLine("Type the name of the starting airport: ", "Please, insert a valid name:");
            String last = Console.readNonEmptyLine("Type the name of the ending airport: ", "Please, insert a valid name:");
            while (Objects.equals(last, first)) {
                last = Console.readLine("The starting and ending airports cannot be the same. Please, insert a different name: ");
            }

            String numbers = Console.readNonEmptyLine("Insert the number of the route (maximum 4 characters): ", "Please, insert a valid number (maximum 4 characters): ");
            while(numbers.length() > 4 || !numbers.matches("[0-9]+")) numbers = Console.readLine("Invalid number. Please, insert the numbers correctly (maximum 4 characters).");

            String payloadStr = numbers + ";" + first + ";" + last;
            byte[] payload = payloadStr.getBytes(StandardCharsets.UTF_8);
            client.sendPacket(new Packet((byte) 8, payload.length, payload));
            Packet response = client.receivePacket();

            if (response.opCode() == (byte) 101) {
                String error = response.payload() != null ? new String(response.payload(), StandardCharsets.UTF_8) : "Unknown error";
                System.out.println("Error creating flight route: " + error);
                return false;
            }

            String msg = new String(response.payload(), StandardCharsets.UTF_8);
            System.out.println(msg);
            return true;
        } catch (IOException e) {
            System.err.println("Error creating flight route: " + e.getMessage());
            return false;
        }
    }
}
