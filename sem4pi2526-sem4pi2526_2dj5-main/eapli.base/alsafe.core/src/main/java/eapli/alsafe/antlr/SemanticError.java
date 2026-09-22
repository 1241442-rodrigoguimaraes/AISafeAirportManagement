package eapli.alsafe.antlr;

/**
 * Represents a single semantic validation error found in a flight plan file.
 * Semantic errors are reported after successful lexical and syntactic analysis.
 */
public class SemanticError {

    private final String flightId;
    private final String message;

    public SemanticError(String flightId, String message) {
        this.flightId = flightId;
        this.message  = message;
    }

    public String getFlightId() { return flightId; }
    public String getMessage()  { return message; }

    @Override
    public String toString() {
        return String.format("[SEMANTIC ERROR] Flight '%s': %s", flightId, message);
    }
}