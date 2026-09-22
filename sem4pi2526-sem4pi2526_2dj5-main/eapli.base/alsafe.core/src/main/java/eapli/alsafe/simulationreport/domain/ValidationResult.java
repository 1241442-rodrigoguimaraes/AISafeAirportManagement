package eapli.alsafe.simulationreport.domain;

public enum ValidationResult {
    PASS,
    FAIL;

    public static ValidationResult fromText(final String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Validation result is required.");
        }

        return ValidationResult.valueOf(text.trim().toUpperCase());
    }
}
