package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;
import eapli.framework.io.util.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class InsertWeatherDataInFlightActionRemote implements Action {

    private final TCPClient client;

    public InsertWeatherDataInFlightActionRemote(final TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        try {
            final String flightPlanId = selectFlightPlan();
            if (flightPlanId == null) {
                return false;
            }

            final String weatherDataId = selectWeatherData();
            if (weatherDataId == null) {
                return false;
            }

            final String payloadText = flightPlanId + ";" + weatherDataId;
            final byte[] payload = payloadText.getBytes(StandardCharsets.UTF_8);
            client.sendPacket(new Packet((byte) 30, payload.length, payload));

            final Packet response = client.receivePacket();
            final String message = new String(response.payload(), StandardCharsets.UTF_8);
            System.out.println(message);
            return response.opCode() == (byte) 100;
        } catch (final IOException e) {
            System.out.println("Error inserting weather data: " + e.getMessage());
            return false;
        }
    }

    private String selectFlightPlan() throws IOException {
        client.sendPacket(new Packet((byte) 23, 0, new byte[0]));
        final Packet response = client.receivePacket();
        final String payload = new String(response.payload(), StandardCharsets.UTF_8);

        if (response.opCode() != (byte) 100) {
            System.out.println("Error fetching flight plans: " + payload);
            return null;
        }
        if (payload.isBlank()) {
            System.out.println("No flight plans found.");
            return null;
        }

        final List<String> ids = new ArrayList<>();
        System.out.println("Select a flight plan:");
        for (final String record : payload.split("\\|")) {
            final String[] fields = record.split(";", 7);
            if (fields.length >= 1) {
                ids.add(fields[0]);
                System.out.printf("%d. %s%n", ids.size(), record);
            }
        }

        return selectId(ids);
    }

    private String selectWeatherData() throws IOException {
        client.sendPacket(new Packet((byte) 29, 0, new byte[0]));
        final Packet response = client.receivePacket();
        final String payload = new String(response.payload(), StandardCharsets.UTF_8);

        if (response.opCode() != (byte) 100) {
            System.out.println("Error fetching weather data: " + payload);
            return null;
        }
        if (payload.isBlank()) {
            System.out.println("No weather data available in the system.");
            return null;
        }

        final List<String> ids = new ArrayList<>();
        System.out.println("Select weather data:");
        for (final String entry : payload.split("\\|")) {
            final String[] parts = entry.split(":", 2);
            if (parts.length == 2) {
                ids.add(parts[0]);
                System.out.printf("%d. %s - %s%n", ids.size(), parts[0], parts[1]);
            }
        }

        return selectId(ids);
    }

    private String selectId(final List<String> ids) {
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
}
