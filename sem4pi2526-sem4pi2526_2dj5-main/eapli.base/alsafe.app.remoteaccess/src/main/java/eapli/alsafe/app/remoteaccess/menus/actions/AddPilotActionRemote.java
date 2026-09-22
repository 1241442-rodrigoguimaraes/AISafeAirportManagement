package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;
import eapli.framework.io.util.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AddPilotActionRemote implements Action {

    private final TCPClient client;

    public AddPilotActionRemote(TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        List<String> availableModels;
        try {
            client.sendPacket(new Packet((byte) 2, 0, null));
            Packet response = client.receivePacket();

            if (response.opCode() == (byte) 101) {
                System.out.println("No aircraft models available. Going back to the main menu...");
                return false;
            }

            availableModels = parseModels(new String(response.payload(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Error fetching aircraft models: " + e.getMessage());
            return false;
        }

        try {
            Set<String> selectedModels = selectModels(availableModels);
            if (selectedModels.isEmpty()) {
                return false;
            }

            String name = Console.readNonEmptyLine("Pilot's name: ", "Please, insert a valid name: ");
            String email = Console.readNonEmptyLine("Pilot's email: ", "Please, insert a valid email: ");
            String phone = Console.readNonEmptyLine("Pilot's phone: ", "Please, insert a valid phone: ");
            String password = Console.readNonEmptyLine("Pilot's password: ", "Please, insert a valid password: ");

            String data = name + ";" + email + ";" + phone + ";" + password + ";" + String.join(",", selectedModels);
            byte[] payload = data.getBytes(StandardCharsets.UTF_8);
            client.sendPacket(new Packet((byte) 10, payload.length, payload));
            Packet response = client.receivePacket();

            if (response.opCode() == (byte) 101) {
                System.out.println("Error creating pilot: " + new String(response.payload(), StandardCharsets.UTF_8));
                return false;
            }

            String msg = new String(response.payload(), StandardCharsets.UTF_8);
            System.out.println(msg);
            return true;
        } catch (Exception e) {
            System.err.println("Error reading input: " + e.getMessage());
            return false;
        }
    }

    private List<String> parseModels(String serverResponse) {
        List<String> models = new ArrayList<>();
        for (String line : serverResponse.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            int separatorIdx = trimmed.indexOf(" - ");
            String modelName = separatorIdx >= 0 ? trimmed.substring(separatorIdx + 3).trim() : trimmed;
            if (!modelName.isEmpty()) {
                models.add(modelName);
            }
        }
        return models;
    }

    private Set<String> selectModels(List<String> available) {
        printNumberedList(available);

        Set<String> selected = new LinkedHashSet<>();
        do {
            int choice = readModelChoice(available.size());
            String model = available.get(choice - 1);
            if (!selected.add(model)) {
                System.out.println("'" + model + "' is already selected.");
            } else {
                System.out.println("Selected: " + model);
                System.out.println("Currently selected: " + selected);
            }
        } while (askYesNo());

        return selected;
    }

    private void printNumberedList(List<String> models) {
        System.out.println("\nAvailable aircraft models:");
        for (int i = 0; i < models.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, models.get(i));
        }
    }

    private int readModelChoice(int max) {
        while (true) {
            int choice = Console.readInteger("Select a model (1-" + max + "): ");
            if (choice >= 1 && choice <= max) {
                return choice;
            }
            System.out.println("Invalid choice. Please enter a number between 1 and " + max + ".");
        }
    }

    private boolean askYesNo() {
        while (true) {
            String answer = Console.readLine("Add another model?" + " (Y/N): ");
            if (answer.equalsIgnoreCase("Y")) return true;
            if (answer.equalsIgnoreCase("N")) return false;
            System.out.println("Please answer Y or N.");
        }
    }
}
