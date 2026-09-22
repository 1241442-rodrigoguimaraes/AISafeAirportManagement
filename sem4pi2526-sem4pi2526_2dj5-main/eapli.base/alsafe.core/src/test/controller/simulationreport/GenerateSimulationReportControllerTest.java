package controller.simulationreport;

import eapli.alsafe.simulationreport.application.GenerateSimulationReportController;
import eapli.alsafe.simulationreport.application.GenerateSimulationReportService;
import eapli.alsafe.simulationreport.domain.SimulationReportParser;
import eapli.alsafe.simulationreport.domain.SimulationReportWriter;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class GenerateSimulationReportControllerTest {

    @TempDir
    Path tempDir;

    @Test
    void flightControlOperatorCanGenerateSimulationReport() throws Exception {
        writeSourceReport();
        final AuthorizationService authz = mock(AuthorizationService.class);
        final var service = service();
        final var controller = new GenerateSimulationReportController(authz, service);

        final Path generated = controller.generateReport();

        verify(authz).ensureAuthenticatedUserHasAnyOf(Roles.FLIGHT_CONTROL_OPERATOR);
        assertEquals("simulation_report_20260613_171620.txt", generated.getFileName().toString());
        assertTrue(Files.exists(generated));
        assertTrue(Files.readString(generated, StandardCharsets.UTF_8).contains("SIMULATION RESULT: PASS"));
    }

    @Test
    void unauthorizedUserCannotGenerateReport() throws Exception {
        final AuthorizationService authz = mock(AuthorizationService.class);
        doThrow(new SecurityException("denied")).when(authz)
                .ensureAuthenticatedUserHasAnyOf(Roles.FLIGHT_CONTROL_OPERATOR);
        final GenerateSimulationReportService service = mock(GenerateSimulationReportService.class);
        final var controller = new GenerateSimulationReportController(authz, service);

        assertThrows(SecurityException.class, controller::generateReport);
        verify(service, never()).generateReport();
    }

    @Test
    void missingSourceReportFailsWithoutCreatingArchive() {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final var controller = new GenerateSimulationReportController(authz, service());

        assertThrows(IOException.class, controller::generateReport);
        assertFalse(Files.exists(tempDir.resolve("simulation_report_20260613_171620.txt")));
    }

    private GenerateSimulationReportService service() {
        return new GenerateSimulationReportService(
                tempDir,
                Clock.fixed(Instant.parse("2026-06-13T17:16:20Z"), ZoneOffset.UTC),
                new SimulationReportParser(),
                new SimulationReportWriter());
    }

    private void writeSourceReport() throws IOException {
        Files.writeString(tempDir.resolve("simulation_report.txt"), String.join("\n",
                "# Final Simulation Report",
                "Total flights: 1",
                "Total safety violations: 0",
                "Validation result: PASS",
                "Flight EK312 (id: 218733259)",
                "- Status: COMPLETED",
                "## Safety Violation Events",
                "No safety violations were recorded."),
                StandardCharsets.UTF_8);
    }
}
