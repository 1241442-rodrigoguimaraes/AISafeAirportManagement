package eapli.alsafe.remoteaccess.server.requesthandlers.weatherperson;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;
import eapli.alsafe.weather.application.ImportBulkWeatherDataController;
import eapli.alsafe.weather.domain.WeatherParsing.BulkImportResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class ImportBulkWeatherDataRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(Packet request) {
        try {
            byte[] payload = request.payload();

            int newlineIdx = -1;
            for (int i = 0; i < payload.length; i++) {
                if (payload[i] == '\n') {
                    newlineIdx = i;
                    break;
                }
            }

            if (newlineIdx < 0) {
                String errorMsg = "Invalid payload format. Expected: filename.ext\\n<file_content>";
                return new Packet((byte) 101, errorMsg.getBytes().length, errorMsg.getBytes());
            }

            String fileName = new String(payload, 0, newlineIdx, StandardCharsets.UTF_8);

            int dotIdx = fileName.lastIndexOf('.');
            if (dotIdx < 0 || dotIdx == fileName.length() - 1) {
                String errorMsg = "Filename has no extension: " + fileName;
                return new Packet((byte) 101, errorMsg.getBytes().length, errorMsg.getBytes());
            }
            String extension = fileName.substring(dotIdx + 1).toLowerCase();

            byte[] fileContent = Arrays.copyOfRange(payload, newlineIdx + 1, payload.length);

            ImportBulkWeatherDataController ctrl = new ImportBulkWeatherDataController();
            String tempFilePath = writeTempFile(extension, fileContent);
            BulkImportResult result = ctrl.importBulkWeatherData(tempFilePath);
            Files.deleteIfExists(Path.of(tempFilePath));

            if (result != null) {
                return new Packet((byte) 100, result.toString().getBytes().length, result.toString().getBytes());
            } else {
                byte[] fail = "Failed to import weather data.".getBytes(StandardCharsets.UTF_8);
                return new Packet((byte) 101, fail.length, fail);
            }
        } catch (Exception e) {
            byte[] err = e.getMessage().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, err.length, err);
        }
    }

    private String writeTempFile(String extension, byte[] content) {
        try {
            String normalizedExtension = extension.startsWith(".") ? extension : "." + extension;
            Path tempFile = Files.createTempFile("weather-bulk-", normalizedExtension);
            Files.write(tempFile, content);
            return tempFile.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to create temporary file", e);
        }
    }
}
