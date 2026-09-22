package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;
import eapli.framework.io.util.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RegisterWeatherDataActionRemote implements Action {

    private final TCPClient client;

    public RegisterWeatherDataActionRemote(TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        try {
            client.sendPacket(new Packet((byte) 15, 0, null));
            Packet areaResponse = client.receivePacket();

            if (areaResponse.opCode() == (byte) 101) {
                String errorMsg = new String(areaResponse.payload(), StandardCharsets.UTF_8);
                System.out.println("Error fetching areas: " + errorMsg);
                return false;
            }

            String areasPayload = new String(areaResponse.payload(), StandardCharsets.UTF_8);
            if (areasPayload.isEmpty()) {
                System.out.println("No air control areas available.");
                return false;
            }

            String[] areaEntries = areasPayload.split("\\|");
            List<String> areaCodes = new ArrayList<>();
            List<String> areaNames = new ArrayList<>();

            for (String entry : areaEntries) {
                String[] parts = entry.split(":", 2);
                if (parts.length == 2) {
                    areaCodes.add(parts[0]);
                    areaNames.add(parts[1]);
                }
            }

            if (areaCodes.isEmpty()) {
                System.out.println("No air control areas available.");
                return false;
            }

            System.out.println("\nAvailable Air Control Areas:");
            for (int i = 0; i < areaCodes.size(); i++) {
                System.out.printf("%d. %s - %s%n", i + 1, areaCodes.get(i), areaNames.get(i));
            }

            int selected = Console.readInteger("Select an area (number): ");
            while (selected < 1 || selected > areaCodes.size()) {
                System.out.println("Invalid selection. Please choose a number between 1 and " + areaCodes.size());
                selected = Console.readInteger("Select an area (number): ");
            }
            String areaId = areaCodes.get(selected - 1);

            String date = Console.readLine("Date (yyyy-MM-dd): ");
            int windDirection = Console.readInteger("Wind direction (0-360°): ");
            double windSpeed = Console.readDouble("Wind speed (m/s): ");

            String data = areaId + ";" + date + ";" + windDirection + ";" + windSpeed;
            byte[] payload = data.getBytes(StandardCharsets.UTF_8);
            client.sendPacket(new Packet((byte) 14, payload.length, payload));

            Packet response = client.receivePacket();
            String msg = new String(response.payload(), StandardCharsets.UTF_8);
            System.out.println(msg);
            return true;
        } catch (IOException e) {
            System.err.println("Error during weather data registration: " + e.getMessage());
            return false;
        }
    }
}
