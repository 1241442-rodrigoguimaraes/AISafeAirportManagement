package controller.pilot;

import eapli.alsafe.aircraftModelMagnement.domain.*;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.IATACompanyCode;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.alsafe.engineModelMagnement.Domain.*;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.flightPlan.repositories.FlightPlanRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.infrastructure.persistence.RepositoryFactory;
import eapli.alsafe.pilotmanagement.application.RemovePilotController;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.pilotmanagement.repositories.PilotRepository;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RemovePilotControllerTest {

    private AuthorizationService authz;
    private UserManagementService userSvc;
    private PilotRepository pilotRepository;
    private CollaboratorRepositoryATCC collaboratorRepository;
    private FlightPlanRepository flightPlanRepository;
    private RemovePilotController controller;

    private static AirTransportCompany company(final String iata, final String icao, final String name) {
        return new AirTransportCompany(new IATACompanyCode(iata), new ICAOCompanyCode(icao), name);
    }

    private static SystemUser user(final String email, final Role role) {
        return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with(email, "Password1", "Test", "User", email)
                .withRoles(role)
                .build();
    }

    private static aircraftModel aircraftModel() {
        final Maker maker = new Maker(new MakerName("MakerA"), new MakerCountry("PT"));
        final engineModel engine = new engineModel(new engineModelName("E1"), maker, engineType.TURBOFAN,
                new engineModelPower(1000), engineModelFuel.JET_A1, new engineModelEfficiency(0.9));
        return new aircraftModel(new aircraftModelName("A320"), maker, aircraftModelType.PASSENGER,
                engineType.TURBOFAN, new maximumRange(1000),
                new aircraftModelPhysicsData(1000, 2000, 1500, 500, 30000, 800, 50, 0.02, 1.2),
                Set.of(engine));
    }

    private static Pilot pilot(final String name, final String email, final AirTransportCompany company) {
        return new Pilot(name, CollaboratorEmail.valueOf(email), CollaboratorPhone.valueOf("912345678"),
                user(email, Roles.PILOT), company, Set.of(aircraftModel()));
    }

    private void stubAuthenticatedAtcc(final AirTransportCompany company) {
        final SystemUser atccUser = user("atcc@company.pt", Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        final CollaboratorATCC collaborator = new CollaboratorATCC("ATCC",
                CollaboratorEmail.valueOf("atcc@company.pt"), CollaboratorPhone.valueOf("912345679"), atccUser,
                company);
        final UserSession session = mock(UserSession.class);
        when(session.authenticatedUser()).thenReturn(atccUser);
        when(authz.session()).thenReturn(Optional.of(session));
        when(collaboratorRepository.findBySystemUser(atccUser)).thenReturn(Optional.of(collaborator));
        when(collaboratorRepository.findCompanyByCollaborator(collaborator)).thenReturn(Optional.of(company));
    }

    @BeforeEach
    void setUp() {
        authz = mock(AuthorizationService.class);
        userSvc = mock(UserManagementService.class);
        pilotRepository = mock(PilotRepository.class);
        collaboratorRepository = mock(CollaboratorRepositoryATCC.class);
        flightPlanRepository = mock(FlightPlanRepository.class);
        controller = new RemovePilotController(authz, userSvc, pilotRepository, collaboratorRepository,
                flightPlanRepository);
    }

    @Test
    void ensureAtccCanDeactivatePilotWithoutAssignedFlightPlans() {
        final AirTransportCompany company = company("TP", "TAP", "TAP Air");
        final Pilot activePilot = pilot("John Pilot", "pilot@tap.pt", company);
        stubAuthenticatedAtcc(company);
        when(pilotRepository.ofIdentity(1L)).thenReturn(Optional.of(activePilot));
        when(flightPlanRepository.findAssignedFlightPlansByPilot(activePilot)).thenReturn(List.of());
        doAnswer(invocation -> {
            ((SystemUser) invocation.getArgument(0)).deactivate(Calendar.getInstance());
            return invocation.getArgument(0);
        }).when(userSvc).deactivateUser(any());

        final Pilot result = controller.deactivatePilot(1L);

        assertFalse(result.isActive());
        verify(userSvc).deactivateUser(activePilot.user());
        verify(authz).ensureAuthenticatedUserHasAnyOf(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
    }

    @Test
    void ensurePilotWithAssignedFlightPlansCannotBeDeactivated() {
        final AirTransportCompany company = company("TP", "TAP", "TAP Air");
        final Pilot activePilot = pilot("John Pilot", "pilot@tap.pt", company);
        stubAuthenticatedAtcc(company);
        when(pilotRepository.ofIdentity(1L)).thenReturn(Optional.of(activePilot));
        when(flightPlanRepository.findAssignedFlightPlansByPilot(activePilot))
                .thenReturn(List.of(mock(FlightPlan.class)));

        assertThrows(IllegalStateException.class, () -> controller.deactivatePilot(1L));

        assertTrue(activePilot.isActive());
        verify(userSvc, never()).deactivateUser(any());
    }

    @Test
    void ensurePilotFromAnotherCompanyCannotBeDeactivated() {
        final AirTransportCompany company = company("TP", "TAP", "TAP Air");
        final AirTransportCompany otherCompany = company("AA", "AAA", "American Airlines");
        final Pilot foreignPilot = pilot("Other Pilot", "pilot@aa.pt", otherCompany);
        stubAuthenticatedAtcc(company);
        when(pilotRepository.ofIdentity(2L)).thenReturn(Optional.of(foreignPilot));

        assertThrows(SecurityException.class, () -> controller.deactivatePilot(2L));

        verify(userSvc, never()).deactivateUser(any());
        verify(flightPlanRepository, never()).findAssignedFlightPlansByPilot(any());
    }

    @Test
    void ensureDeactivatedPilotIsExcludedFromActivePilotList() {
        final AirTransportCompany company = company("TP", "TAP", "TAP Air");
        final Pilot activePilot = pilot("John Pilot", "pilot@tap.pt", company);
        stubAuthenticatedAtcc(company);
        when(pilotRepository.findActiveByCompany(company)).thenReturn(List.of(activePilot));

        final List<Pilot> activePilots = controller.activePilotsForCurrentCompany();

        assertEquals(1, activePilots.size());
        verify(pilotRepository).findActiveByCompany(company);
    }

    @Test
    void ensureUnauthorizedCallerIsRejected() {
        doThrow(new SecurityException("forbidden")).when(authz)
                .ensureAuthenticatedUserHasAnyOf(eq(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR));

        assertThrows(SecurityException.class, () -> controller.deactivatePilot(1L));

        verify(pilotRepository, never()).ofIdentity(any());
        verify(userSvc, never()).deactivateUser(any());
    }

    @Test
    void ensureUnknownPilotIsRejected() {
        stubAuthenticatedAtcc(company("TP", "TAP", "TAP Air"));
        when(pilotRepository.ofIdentity(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> controller.deactivatePilot(99L));
        verify(userSvc, never()).deactivateUser(any());
    }

    @Test
    void ensureAlreadyInactivePilotIsRejected() {
        final AirTransportCompany company = company("TP", "TAP", "TAP Air");
        final Pilot inactivePilot = pilot("John Pilot", "pilot@tap.pt", company);
        inactivePilot.user().deactivate(Calendar.getInstance());
        stubAuthenticatedAtcc(company);
        when(pilotRepository.ofIdentity(1L)).thenReturn(Optional.of(inactivePilot));

        assertThrows(IllegalStateException.class, () -> controller.deactivatePilot(1L));

        verify(userSvc, never()).deactivateUser(any());
        verify(flightPlanRepository, never()).findAssignedFlightPlansByPilot(any());
    }

    @Test
    void ensureMissingAuthenticatedSessionIsRejected() {
        when(authz.session()).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> controller.activePilotsForCurrentCompany());

        verify(collaboratorRepository, never()).findBySystemUser(any());
        verify(pilotRepository, never()).findActiveByCompany(any());
    }

    @Test
    void ensureMissingCollaboratorProfileIsRejected() {
        final SystemUser atccUser = user("atcc@company.pt", Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        final UserSession session = mock(UserSession.class);
        when(session.authenticatedUser()).thenReturn(atccUser);
        when(authz.session()).thenReturn(Optional.of(session));
        when(collaboratorRepository.findBySystemUser(atccUser)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> controller.activePilotsForCurrentCompany());

        verify(collaboratorRepository).findBySystemUser(atccUser);
        verify(collaboratorRepository, never()).findCompanyByCollaborator(any());
        verify(pilotRepository, never()).findActiveByCompany(any());
    }

    @Test
    void ensureCollaboratorWithoutCompanyIsRejected() {
        final AirTransportCompany company = company("TP", "TAP", "TAP Air");
        final SystemUser atccUser = user("atcc@company.pt", Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        final CollaboratorATCC collaborator = new CollaboratorATCC("ATCC",
                CollaboratorEmail.valueOf("atcc@company.pt"), CollaboratorPhone.valueOf("912345679"), atccUser,
                company);
        final UserSession session = mock(UserSession.class);
        when(session.authenticatedUser()).thenReturn(atccUser);
        when(authz.session()).thenReturn(Optional.of(session));
        when(collaboratorRepository.findBySystemUser(atccUser)).thenReturn(Optional.of(collaborator));
        when(collaboratorRepository.findCompanyByCollaborator(collaborator)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> controller.activePilotsForCurrentCompany());

        verify(collaboratorRepository).findCompanyByCollaborator(collaborator);
        verify(pilotRepository, never()).findActiveByCompany(any());
    }

    @Test
    void constructorRejectsNullDependencies() {
        assertThrows(IllegalArgumentException.class,
                () -> new RemovePilotController(null, userSvc, pilotRepository, collaboratorRepository,
                        flightPlanRepository));
        assertThrows(IllegalArgumentException.class,
                () -> new RemovePilotController(authz, null, pilotRepository, collaboratorRepository,
                        flightPlanRepository));
        assertThrows(IllegalArgumentException.class,
                () -> new RemovePilotController(authz, userSvc, null, collaboratorRepository, flightPlanRepository));
        assertThrows(IllegalArgumentException.class,
                () -> new RemovePilotController(authz, userSvc, pilotRepository, null, flightPlanRepository));
        assertThrows(IllegalArgumentException.class,
                () -> new RemovePilotController(authz, userSvc, pilotRepository, collaboratorRepository, null));
    }

    @Test
    void defaultConstructorUsesApplicationServices() {
        final RepositoryFactory repositories = mock(RepositoryFactory.class);
        try (var authzRegistry = mockStatic(AuthzRegistry.class);
             var persistenceContext = mockStatic(PersistenceContext.class)) {
            authzRegistry.when(AuthzRegistry::authorizationService).thenReturn(authz);
            authzRegistry.when(AuthzRegistry::userService).thenReturn(userSvc);
            persistenceContext.when(PersistenceContext::repositories).thenReturn(repositories);
            when(repositories.pilots()).thenReturn(pilotRepository);
            when(repositories.collaboratorsATCC()).thenReturn(collaboratorRepository);
            when(repositories.flightPlan()).thenReturn(flightPlanRepository);

            assertDoesNotThrow(() -> new RemovePilotController());
        }
    }
}
