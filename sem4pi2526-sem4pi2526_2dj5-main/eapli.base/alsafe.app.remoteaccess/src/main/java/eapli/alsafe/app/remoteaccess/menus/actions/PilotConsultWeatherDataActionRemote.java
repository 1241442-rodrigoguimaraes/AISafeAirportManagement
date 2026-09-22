package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;
import eapli.framework.io.util.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PilotConsultWeatherDataActionRemote implements Action {

    private final TCPClient client;

    public PilotConsultWeatherDataActionRemote(final TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        try {
            final String areaId = selectAirControlArea();
            if (areaId == null) {
                return false;
            }

            final String startDate = Console.readLine("Start date (yyyy-MM-dd): ");
            final String endDate = Console.readLine("End date (yyyy-MM-dd): ");
            final String data = areaId + ";" + startDate + ";" + endDate;
            final byte[] payload = data.getBytes(StandardCharsets.UTF_8);

            client.sendPacket(new Packet((byte) 16, payload.length, payload));
            final Packet response = client.receivePacket();
            final String message = new String(response.payload(), StandardCharsets.UTF_8);

            if (response.opCode() != (byte) 100) {
                System.out.println("Error: " + message);
                return false;
            }

            if (message.isBlank()) {
                System.out.println("No weather data found for the selected criteria.");
            } else {
                System.out.println(message);
            }
            return true;
        } catch (final IOException e) {
            System.out.println("Error consulting weather data: " + e.getMessage());
            return false;
        }
    }

    private String selectAirControlArea() throws IOException {
        client.sendPacket(new Packet((byte) 28, 0, new byte[0]));
        final Packet response = client.receivePacket();
        final String payload = new String(response.payload(), StandardCharsets.UTF_8);

        if (response.opCode() != (byte) 100) {
            System.out.println("Error fetching areas: " + payload);
            return null;
        }
        if (payload.isBlank()) {
            System.out.println("No air control areas available.");
            return null;
        }

        final List<String> areaCodes = new ArrayList<>();
        for (final String entry : payload.split("\\|")) {
            final String[] parts = entry.split(":", 2);
            if (parts.length == 2) {
                areaCodes.add(parts[0]);
                System.out.printf("%d. %s - %s%n", areaCodes.size(), parts[0], parts[1]);
            }
        }

        if (areaCodes.isEmpty()) {
            System.out.println("No air control areas available.");
            return null;
        }

        int selected = Console.readInteger("Select an area (number): ");
        while (selected < 1 || selected > areaCodes.size()) {
            System.out.println("Invalid selection.");
            selected = Console.readInteger("Select an area (number): ");
        }

        return areaCodes.get(selected - 1);
    }
}
