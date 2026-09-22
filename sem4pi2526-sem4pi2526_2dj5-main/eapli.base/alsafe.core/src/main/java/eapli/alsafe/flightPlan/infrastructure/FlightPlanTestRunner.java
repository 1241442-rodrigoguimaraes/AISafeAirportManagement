package eapli.alsafe.flightPlan.infrastructure;

import java.io.IOException;

public interface FlightPlanTestRunner {

    TestResult run(String dslContent) throws IOException;

    record TestResult(boolean success, int exitCode, String message) {
        public String summary() {
            return success ? "All safety checks passed."
                           : "Exit code " + exitCode + ": " + message;
        }
    }
}
