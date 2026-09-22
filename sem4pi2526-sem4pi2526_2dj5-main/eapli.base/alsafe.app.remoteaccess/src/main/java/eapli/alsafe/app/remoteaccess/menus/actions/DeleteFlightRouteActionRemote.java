package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;
import eapli.framework.io.util.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class DeleteFlightRouteActionRemote implements Action {

    private final TCPClient client;

    public DeleteFlightRouteActionRemote(final TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        String date = Console.readNonEmptyLine(
                "Enter the date from which you would like to delete a flight route onwards (DD/MM/YYYY): ",
                "The date cannot be empty: ");

        while (!isValidDate(date)) {
            date = Console.readLine("Invalid date format or the date is in the past. Please, enter the date in the format DD/MM/YYYY: ");
        }

        List<String> routes;
        try {
            byte[] payload = date.getBytes(StandardCharsets.UTF_8);
            client.sendPacket(new Packet((byte) 9, payload.length, payload));
            Packet response = client.receivePacket();

            if (response.opCode() == (byte) 101) {
                String error = response.payload() != null ? new String(response.payload(), StandardCharsets.UTF_8) : "Unknown error";
                System.out.println("Error fetching flight routes: " + error);
                return false;
            }

            routes = parseRouteList(new String(response.payload(), StandardCharsets.UTF_8));

            if (routes.isEmpty()) {
                System.out.println("No deletable flight routes found for the given date.");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error fetching flight routes: " + e.getMessage());
            return false;
        }

        System.out.println("\n=== Deletable Flight Routes ===");
        for (int i = 0; i < routes.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, routes.get(i));
        }

        int choice = readChoice(routes.size());
        String selectedRoute = routes.get(choice - 1);
        System.out.println("\nSelected: " + selectedRoute);

        try {
            String payloadStr = date + ";" + selectedRoute;
            byte[] payload = payloadStr.getBytes(StandardCharsets.UTF_8);
            client.sendPacket(new Packet((byte) 9, payload.length, payload));
            Packet response = client.receivePacket();

            String msg = response.payload() != null ? new String(response.payload(), StandardCharsets.UTF_8) : "";
            System.out.println(msg);

            return response.opCode() == (byte) 100;
        } catch (IOException e) {
            System.err.println("Error deleting flight route: " + e.getMessage());
            return false;
        }
    }

    private boolean isValidDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate parsed = LocalDate.parse(date, formatter);
            return !parsed.isBefore(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private List<String> parseRouteList(String msg) {
        List<String> result = new ArrayList<>();
        for (String line : msg.split("\n")) {
            if (!line.isBlank()) result.add(line.trim());
        }
        return result;
    }

    private int readChoice(int max) {
        while (true) {
            int choice = Console.readInteger("Select a flight route (1-" + max + "): ");
            if (choice >= 1 && choice <= max) return choice;
            System.out.println("Invalid choice. Please enter a number between 1 and " + max + ".");
        }
    }
}
