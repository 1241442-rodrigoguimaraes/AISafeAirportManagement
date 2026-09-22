package eapli.alsafe.simulationreport.application;

import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenerateMonthlyReportControllerTest {

    @Test
    void flightControlOperatorCanGenerateMonthlyReportWithInjectedStrategy() throws Exception {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final ReportGenerator generator = mock(ReportGenerator.class);
        final YearMonth period = YearMonth.of(2026, 6);
        when(generator.generate(period)).thenReturn(Path.of("monthly_statistics_report_2026_06.txt"));
        final var controller = new GenerateMonthlyReportController(authz, generator);

        final Path result = controller.generateMonthlyReport(period);

        verify(authz).ensureAuthenticatedUserHasAnyOf(Roles.FLIGHT_CONTROL_OPERATOR);
        verify(generator).generate(period);
        assertEquals(Path.of("monthly_statistics_report_2026_06.txt"), result);
    }

    @Test
    void unauthorizedUserCannotGenerateMonthlyReport() throws Exception {
        final AuthorizationService authz = mock(AuthorizationService.class);
        doThrow(new SecurityException("denied")).when(authz)
                .ensureAuthenticatedUserHasAnyOf(Roles.FLIGHT_CONTROL_OPERATOR);
        final ReportGenerator generator = mock(ReportGenerator.class);
        final var controller = new GenerateMonthlyReportController(authz, generator);

        assertThrows(SecurityException.class, () -> controller.generateMonthlyReport(YearMonth.of(2026, 6)));
        verify(generator, never()).generate(YearMonth.of(2026, 6));
    }

    @Test
    void generatorIOExceptionIsPropagated() throws Exception {
        final AuthorizationService authz = mock(AuthorizationService.class);
        final ReportGenerator generator = mock(ReportGenerator.class);
        final YearMonth period = YearMonth.of(2026, 6);
        when(generator.generate(period)).thenThrow(new IOException("disk full"));
        final var controller = new GenerateMonthlyReportController(authz, generator);

        assertThrows(IOException.class, () -> controller.generateMonthlyReport(period));
    }
}
