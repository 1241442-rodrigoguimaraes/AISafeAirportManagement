package eapli.alsafe.simulationreport.domain;

import java.nio.file.Path;

public record MonthlyReportWarning(Path sourcePath, String reason) {

    public MonthlyReportWarning {
        if (sourcePath == null) {
            throw new IllegalArgumentException("Warning source path is required.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Warning reason is required.");
        }
        reason = reason.trim();
    }
}
