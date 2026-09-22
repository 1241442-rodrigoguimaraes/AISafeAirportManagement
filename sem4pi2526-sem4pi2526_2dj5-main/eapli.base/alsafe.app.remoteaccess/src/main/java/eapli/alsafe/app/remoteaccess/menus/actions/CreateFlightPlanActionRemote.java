package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;
import eapli.framework.io.util.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CreateFlightPlanActionRemote implements Action {

    private final TCPClient client;

    public CreateFlightPlanActionRemote(final TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        try {
            final String routeId = selectRemoteEntry((byte) 20, "Select a route");
            if (routeId == null) {
                return false;
            }

            final String aircraftRegistration = selectRemoteEntry((byte) 21, "Select an aircraft");
            if (aircraftRegistration == null) {
                return false;
            }

            final String designator = Console.readNonEmptyLine("Flight designator (e.g. TP123): ", "Flight designator is required").trim().toUpperCase();
            final LocalDateTime departureDateTime = readDepartureDateTime();
            final String fuelUnit = readFuelUnit();
            final double fuelAmount = readFuelAmount();
            final String dslContent = readDslContent();
            if (dslContent == null) {
                return false;
            }

            final String encodedContent = Base64.getEncoder().encodeToString(dslContent.getBytes(StandardCharsets.UTF_8));
            final String payloadText = routeId + ";" + aircraftRegistration + ";" + designator + ";"
                    + departureDateTime + ";" + fuelUnit + ";" + fuelAmount + ";" + encodedContent;

            final byte[] payload = payloadText.getBytes(StandardCharsets.UTF_8);
            client.sendPacket(new Packet((byte) 22, payload.length, payload));

            final Packet response = client.receivePacket();
            final String message = new String(response.payload(), StandardCharsets.UTF_8);
            System.out.println(message);
            return response.opCode() == (byte) 100;
        } catch (final IOException e) {
            System.out.println("Error creating flight plan: " + e.getMessage());
            return false;
        }
    }

    private String selectRemoteEntry(final byte opCode, final String title) throws IOException {
        client.sendPacket(new Packet(opCode, 0, new byte[0]));
        final Packet response = client.receivePacket();
        final String payload = new String(response.payload(), StandardCharsets.UTF_8);

        if (response.opCode() != (byte) 100) {
            System.out.println("Error: " + payload);
            return null;
        }
        if (payload.isBlank()) {
            System.out.println("No records available.");
            return null;
        }

        final List<String> ids = new ArrayList<>();
        final String[] entries = payload.split("\\|");
        System.out.println(title + ":");
        for (final String entry : entries) {
            final String[] parts = entry.split(":", 2);
            if (parts.length == 2) {
                ids.add(parts[0]);
                System.out.printf("%d. %s - %s%n", ids.size(), parts[0], parts[1]);
            }
        }

        if (ids.isEmpty()) {
            System.out.println("No valid records available.");
            return null;
        }

        int selected = Console.readInteger("Selection: ");
        while (selected < 1 || selected > ids.size()) {
            System.out.println("Invalid selection.");
            selected = Console.readInteger("Selection: ");
        }

        return ids.get(selected - 1);
    }

    private LocalDateTime readDepartureDateTime() {
        while (true) {
            try {
                final String value = Console.readNonEmptyLine("Departure date/time (yyyy-MM-ddTHH:mm): ", "Departure date/time is required");
                return LocalDateTime.parse(value.trim());
            } catch (final DateTimeParseException e) {
                System.out.println("Invalid date/time format. Example: 2026-06-15T10:30");
            }
        }
    }

    private String readFuelUnit() {
        String unit;
        do {
            unit = Console.readNonEmptyLine("Fuel unit (L, KG, LBS): ", "Fuel unit is required").trim().toUpperCase();
        } while (!unit.equals("L") && !unit.equals("KG") && !unit.equals("LBS"));
        return unit;
    }

    private double readFuelAmount() {
        double amount;
        do {
            amount = Console.readDouble("Fuel quantity: ");
        } while (amount <= 0);
        return amount;
    }

    private String readDslContent() {
        final String filePath = Console.readNonEmptyLine("Flight plan file path (.fp): ", "File path is required");
        try {
            return Files.readString(Path.of(filePath));
        } catch (final IOException e) {
            System.out.println("Error reading flight plan file: " + e.getMessage());
            return null;
        }
    }
}
