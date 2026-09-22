package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.framework.actions.Action;
import eapli.framework.io.util.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImportBulkWeatherDataActionRemote implements Action {

    private final TCPClient client;

    public ImportBulkWeatherDataActionRemote(TCPClient client) {
        this.client = client;
    }

    @Override
    public boolean execute() {
        try {
            String filePathInput = Console.readNonEmptyLine("File path: ", "Please enter a file path");

            Path filePath = Paths.get(filePathInput);
            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                System.out.println("File not found: " + filePathInput);
                return false;
            }

            String fileName = filePath.getFileName().toString();
            int dotIdx = fileName.lastIndexOf('.');
            if (dotIdx < 0 || dotIdx == fileName.length() - 1) {
                System.out.println("File must have an extension (e.g. .csv, .json, .xml, .xlsx)");
                return false;
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);

            byte[] payload = new byte[fileNameBytes.length + 1 + fileBytes.length];
            System.arraycopy(fileNameBytes, 0, payload, 0, fileNameBytes.length);
            payload[fileNameBytes.length] = '\n';
            System.arraycopy(fileBytes, 0, payload, fileNameBytes.length + 1, fileBytes.length);

            client.sendPacket(new Packet((byte) 13, payload.length, payload));
            Packet response = client.receivePacket();

            String msg = new String(response.payload(), StandardCharsets.UTF_8);
            System.out.println(msg);
            return true;
        } catch (IOException e) {
            System.err.println("Error importing bulk weather data: " + e.getMessage());
            return false;
        }
    }
}
