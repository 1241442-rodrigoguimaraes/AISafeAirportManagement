package eapli.alsafe.simulationreport.application;

import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.io.IOException;
import java.nio.file.Path;

@UseCaseController
public class GenerateSimulationReportController {

    private final AuthorizationService authz;
    private final GenerateSimulationReportService service;

    public GenerateSimulationReportController() {
        this(AuthzRegistry.authorizationService(), new GenerateSimulationReportService());
    }

    public GenerateSimulationReportController(final AuthorizationService authz,
                                              final GenerateSimulationReportService service) {
        if (authz == null || service == null) {
            throw new IllegalArgumentException("Controller dependencies are required.");
        }
        this.authz = authz;
        this.service = service;
    }

    public Path generateReport() throws IOException {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.FLIGHT_CONTROL_OPERATOR);
        return service.generateReport();
    }
}
