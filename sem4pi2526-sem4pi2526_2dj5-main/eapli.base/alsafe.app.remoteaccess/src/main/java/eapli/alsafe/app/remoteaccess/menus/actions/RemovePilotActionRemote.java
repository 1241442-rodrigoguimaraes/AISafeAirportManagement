package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;
import eapli.framework.io.util.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RemovePilotActionRemote implements Action {

    private final TCPClient client;

    public RemovePilotActionRemote(final TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        List<long[]> pilotIds = new ArrayList<>();
        List<String> pilotLabels = new ArrayList<>();

        try {
            client.sendPacket(new Packet((byte) 11, 0, null));
            Packet response = client.receivePacket();

            if (response.opCode() == (byte) 101) {
                System.out.println("There are no active pilots available to deactivate in your company's roster.");
                return false;
            }

            String msg = new String(response.payload(), StandardCharsets.UTF_8);
            for (String line : msg.split("\n")) {
                if (line.isBlank()) continue;
                Long id = parseId(line);
                if (id != null) {
                    pilotIds.add(new long[]{id});
                    pilotLabels.add(line.trim());
                }
            }

            if (pilotLabels.isEmpty()) {
                System.out.println("There are no active pilots available to deactivate in your company's roster.");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error fetching pilot roster: " + e.getMessage());
            return false;
        }

        System.out.println("\n=== Active Pilots ===");
        for (int i = 0; i < pilotLabels.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, pilotLabels.get(i));
        }

        int choice = readChoice(pilotLabels.size());
        String selectedLabel = pilotLabels.get(choice - 1);
        Long selectedId = pilotIds.get(choice - 1)[0];

        System.out.println("\nSelected: " + selectedLabel);

        if (!askYesNo()) {
            System.out.println("Operation cancelled.");
            return false;
        }

        try {
            byte[] payload = String.valueOf(selectedId).getBytes(StandardCharsets.UTF_8);
            client.sendPacket(new Packet((byte) 12, payload.length, payload));
            Packet response = client.receivePacket();

            String msg = new String(response.payload(), StandardCharsets.UTF_8);
            System.out.println(msg);

            return response.opCode() == (byte) 100;
        } catch (IOException e) {
            System.err.println("Error sending deactivation request: " + e.getMessage());
            return false;
        }
    }

    private Long parseId(String line) {
        try {
            String trimmed = line.trim();
            if (!trimmed.startsWith("#")) return null;
            int separatorIdx = trimmed.indexOf(" | ");
            String idStr = trimmed.substring(1, separatorIdx >= 0 ? separatorIdx : trimmed.length());
            return Long.parseLong(idStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int readChoice(int max) {
        while (true) {
            int choice = Console.readInteger("Select a pilot (1-" + max + "): ");
            if (choice >= 1 && choice <= max) return choice;
            System.out.println("Invalid choice. Please enter a number between 1 and " + max + ".");
        }
    }

    private boolean askYesNo() {
        while (true) {
            String answer = Console.readLine("Confirm deactivation?" + " (Y/N): ");
            if (answer.equalsIgnoreCase("Y")) return true;
            if (answer.equalsIgnoreCase("N")) return false;
            System.out.println("Please answer Y or N.");
        }
    }
}
