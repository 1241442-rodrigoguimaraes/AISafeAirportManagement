package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;
import eapli.framework.io.util.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AddAircraftActionRemote implements Action {

    private final TCPClient client;

    public AddAircraftActionRemote(TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        try {
            client.sendPacket(new Packet((byte) 2, 0, null));
            Packet response = client.receivePacket();

            if (response.opCode() == (byte) 101) {
                System.out.println("No aircraft models available. Going back to the main menu...");
                return false;
            }

            String msg = new String(response.payload(), StandardCharsets.UTF_8);
            System.out.println(msg);
        } catch (IOException e) {
            System.err.println("Error fetching aircraft models: " + e.getMessage());
            return false;
        }

        try {
            String aircraftModel = Console.readNonEmptyLine("Aircraft Model: ", "Please enter a valid model name");

            String registrationID = Console.readNonEmptyLine("Registration ID: ", "Please enter a valid registration ID");
            String country = Console.readNonEmptyLine("Country: ", "Please enter a valid country");

            int economy = Console.readInteger("Economy Seats: ");
            while (economy < 0) {
                System.out.println("Economy seats cannot be negative. Please enter a valid number.");
                economy = Console.readInteger("Economy Seats: ");
            }

            int business = Console.readInteger("Business Seats: ");
            while (business < 0) {
                System.out.println("Business seats cannot be negative. Please enter a valid number.");
                business = Console.readInteger("Business Seats: ");
            }

            int firstClass = Console.readInteger("First Class Seats: ");
            while (firstClass < 0) {
                System.out.println("First class seats cannot be negative. Please enter a valid number.");
                firstClass = Console.readInteger("First Class Seats: ");
            }

            int crewCount = Console.readInteger("Crew Count: ");
            while (crewCount < 0) {
                System.out.println("Crew count cannot be negative. Please enter a valid number.");
                crewCount = Console.readInteger("Crew Count: ");
            }

            String data = aircraftModel + ";" + registrationID + ";" + country + ";" + economy + ";" + business + ";" + firstClass + ";" + crewCount;
            byte[] payload = data.getBytes(StandardCharsets.UTF_8);

            client.sendPacket(new Packet((byte) 3, payload.length, payload));
            Packet response = client.receivePacket();

            if (response.opCode() == (byte) 101) {
                System.out.println("Error creating aircraft: " + new String(response.payload(), StandardCharsets.UTF_8));
                return false;
            }

            String msg = new String(response.payload(), StandardCharsets.UTF_8);
            System.out.println(msg);
            return true;
        } catch (IOException e) {
            System.err.println("Error reading user input: " + e.getMessage());
            return false;
        }
    }
}
