package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;
import eapli.framework.io.util.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ConsultWeatherDataActionRemote implements Action {

    private final TCPClient client;

    public ConsultWeatherDataActionRemote(TCPClient client) {
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

            for (String entry : areaEntries) {
                String[] parts = entry.split(":", 2);
                if (parts.length == 2) {
                    areaCodes.add(parts[0]);
                    System.out.printf("%d. %s - %s%n", areaCodes.size(), parts[0], parts[1]);
                }
            }

            if (areaCodes.isEmpty()) {
                System.out.println("No air control areas available.");
                return false;
            }

            int selected = Console.readInteger("Select an area (number): ");
            while (selected < 1 || selected > areaCodes.size()) {
                System.out.println("Invalid selection. Please choose a number between 1 and " + areaCodes.size());
                selected = Console.readInteger("Select an area (number): ");
            }
            String areaId = areaCodes.get(selected - 1);

            String startDate = Console.readLine("Start date (yyyy-MM-dd): ");
            String endDate = Console.readLine("End date (yyyy-MM-dd): ");

            String data = areaId + ";" + startDate + ";" + endDate;
            byte[] payload = data.getBytes(StandardCharsets.UTF_8);
            client.sendPacket(new Packet((byte) 16, payload.length, payload));

            Packet response = client.receivePacket();
            String msg = new String(response.payload(), StandardCharsets.UTF_8);
            if (msg.isEmpty()) {
                System.out.println("No weather data found for the selected criteria.");
            } else {
                String[] records = msg.split("\\|");
                for (String record : records) {
                    String[] fields = record.split(":", 4);
                    if (fields.length == 4) {
                        System.out.printf("Area: %s | Date: %s | Wind Direction: %s° | Wind Speed: %s m/s%n",
                                fields[0], fields[1], fields[2], fields[3]);
                    } else {
                        System.out.println(record);
                    }
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error consulting weather data: " + e.getMessage());
            return false;
        }
    }
}
