package eapli.alsafe.simulationreport.application;

import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.io.IOException;
import java.nio.file.Path;
import java.time.YearMonth;

@UseCaseController
public class GenerateMonthlyReportController {

    private final AuthorizationService authz;
    private final ReportGenerator generator;

    public GenerateMonthlyReportController() {
        this(AuthzRegistry.authorizationService(), ReportGeneratorFactory.monthlyStatistics());
    }

    public GenerateMonthlyReportController(final AuthorizationService authz, final ReportGenerator generator) {
        if (authz == null || generator == null) {
            throw new IllegalArgumentException("Controller dependencies are required.");
        }
        this.authz = authz;
        this.generator = generator;
    }

    public Path generateMonthlyReport(final YearMonth period) throws IOException {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.FLIGHT_CONTROL_OPERATOR);
        return generator.generate(period);
    }
}
