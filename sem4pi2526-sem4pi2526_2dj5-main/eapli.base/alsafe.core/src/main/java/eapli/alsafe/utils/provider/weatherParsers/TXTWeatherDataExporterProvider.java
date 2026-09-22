package eapli.alsafe.utils.provider.weatherParsers;

import eapli.alsafe.utils.provider.interfaces.WeatherDataExporterProvider;
import eapli.alsafe.weather.domain.WeatherParsing.BulkImportResult;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TXTWeatherDataExporterProvider implements WeatherDataExporterProvider {

    private final String filePath = "weatherDataImportReports";
    private final String fileName;

    public TXTWeatherDataExporterProvider(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean exportWeatherData(BulkImportResult result) {
        if (result == null) {
            return false;
        }

        Path dir = Paths.get(filePath);
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            dir = Paths.get(".");
        }
        String[] parts = fileName.split("[/\\\\]");
        String targetName = parts[parts.length - 1];
        if (!targetName.toLowerCase().endsWith(".txt")) {
            targetName = targetName + ".txt";
        }

        Path target = dir.resolve(targetName);

        String content = buildContent(result);

        try {
            Files.write(target, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String buildContent(BulkImportResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("BulkImportResult:\n");
        sb.append(result.toString()).append('\n').append('\n');

        Method[] methods = result.getClass().getMethods();
        Method[] getters = Arrays.stream(methods)
                .filter(m -> m.getName().startsWith("get")
                        && m.getParameterCount() == 0
                        && m.getReturnType() != void.class
                        && m.getDeclaringClass() != Object.class)
                .toArray(Method[]::new);

        if (getters.length > 0) {
            sb.append("Detailed fields (via getters):\n");
            for (Method gm : getters) {
                try {
                    Object value = gm.invoke(result);
                    String name = gm.getName();
                    sb.append(name).append(" = ");
                    if (value == null) {
                        sb.append("null");
                    } else {
                        if (value.getClass().isArray()) {
                            Object[] arr = (Object[]) value;
                            sb.append(Arrays.stream(arr).map(o -> String.valueOf(o)).collect(Collectors.joining(", ")));
                        } else {
                            sb.append(value.toString());
                        }
                    }
                    sb.append('\n');
                } catch (IllegalAccessException | InvocationTargetException e) {
                    sb.append(gm.getName()).append(" = <unable to read>").append('\n');
                }
            }
        }

        return sb.toString();
    }
}
