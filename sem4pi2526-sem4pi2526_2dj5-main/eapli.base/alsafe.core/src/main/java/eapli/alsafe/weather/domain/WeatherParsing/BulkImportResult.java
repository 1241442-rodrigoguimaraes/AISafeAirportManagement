package eapli.alsafe.weather.domain.WeatherParsing;

import java.util.List;

public class BulkImportResult {

    private final int successCount;
    private final List<WeatherImportError> errors;

    public BulkImportResult(final int successCount, final List<WeatherImportError> errors) {
        this.successCount = successCount;
        this.errors = errors;
    }

    public int successCount() {
        return successCount;
    }

    public List<WeatherImportError> errors() {
        return errors;
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Import Result ===\n");
        sb.append("Successfully imported: ").append(successCount).append("\n");

        if (hasErrors()) {
            sb.append("Errors: ").append(errors.size()).append("\n");
            for (WeatherImportError err : errors) {
                sb.append("  [Line ").append(err.getLineNumber()).append("] ")
                        .append(err.getCategory()).append(": ")
                        .append(err.getDetails()).append("\n");
                if (err.getRawLine() != null && !err.getRawLine().isEmpty()) {
                    sb.append("    Raw: ").append(err.getRawLine()).append("\n");
                }
            }
        } else {
            sb.append("Errors: 0\n");
        }

        sb.append("=====================\n");
        return sb.toString();
    }
}
