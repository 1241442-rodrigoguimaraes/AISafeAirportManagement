package controller.pilot;

import eapli.alsafe.aircraftModelMagnement.domain.*;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.IATACompanyCode;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.alsafe.engineModelMagnement.Domain.*;
import eapli.alsafe.pilotmanagement.application.AddPilotController;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.pilotmanagement.repositories.PilotRepository;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AddPilotControllerTest {

    private static class FakeUserManagementService extends UserManagementService {
        private Set<Role> lastRoles;

        FakeUserManagementService() {
            super(null, null, null);
        }

        @Override
        public SystemUser registerNewUser(final String username, final String password, final String firstName,
                                          final String lastName, final String email, final Set<Role> roles) {
            lastRoles = roles;
            return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                    .with(username, password, firstName, lastName, email)
                    .withRoles(roles.toArray(new Role[0]))
                    .build();
        }
    }

    private AuthorizationService authz;
    private FakeUserManagementService userSvc;
    private PilotRepository pilotRepository;
    private CollaboratorRepositoryATCC collaboratorRepository;
    private aircraftModelRepository aircraftModelRepository;
    private AddPilotController controller;

    private static AirTransportCompany company() {
        return new AirTransportCompany(new IATACompanyCode("TP"), new ICAOCompanyCode("TAP"), "TAP Air");
    }

    private static SystemUser atccUser() {
        return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with("atcc@tap.pt", "Password1", "Air", "Collaborator", "atcc@tap.pt")
                .withRoles(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR)
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

    private void stubAuthenticatedAtcc(final AirTransportCompany company) {
        final SystemUser user = atccUser();
        final CollaboratorATCC collaborator = new CollaboratorATCC("ATCC",
                CollaboratorEmail.valueOf("atcc@tap.pt"), CollaboratorPhone.valueOf("912345678"), user, company);
        final UserSession session = mock(UserSession.class);
        when(session.authenticatedUser()).thenReturn(user);
        when(authz.session()).thenReturn(Optional.of(session));
        when(collaboratorRepository.findBySystemUser(user)).thenReturn(Optional.of(collaborator));
        when(collaboratorRepository.findCompanyByCollaborator(collaborator)).thenReturn(Optional.of(company));
    }

    @BeforeEach
    void setUp() {
        authz = mock(AuthorizationService.class);
        userSvc = new FakeUserManagementService();
        pilotRepository = mock(PilotRepository.class);
        collaboratorRepository = mock(CollaboratorRepositoryATCC.class);
        aircraftModelRepository = mock(aircraftModelRepository.class);
        controller = new AddPilotController(authz, userSvc, pilotRepository, collaboratorRepository,
                aircraftModelRepository);
    }

    @Test
    void ensureAtccCanAddPilotToOwnCompany() {
        final AirTransportCompany company = company();
        final aircraftModel model = aircraftModel();
        stubAuthenticatedAtcc(company);
        when(pilotRepository.findByEmail(CollaboratorEmail.valueOf("pilot@tap.pt"))).thenReturn(Optional.empty());
        when(pilotRepository.save(any(Pilot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Pilot pilot = controller.addPilot("John Pilot", "pilot@tap.pt", "912345679", "Password1",
                Set.of(model));

        assertNotNull(pilot);
        assertEquals(company, pilot.company());
        assertTrue(pilot.certifiedAircraftModels().contains(model));
        assertTrue(userSvc.lastRoles.contains(Roles.PILOT));
        verify(authz).ensureAuthenticatedUserHasAnyOf(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        verify(pilotRepository).save(any(Pilot.class));
    }

    @Test
    void ensureDuplicatedPilotEmailIsRejected() {
        final AirTransportCompany company = company();
        final aircraftModel model = aircraftModel();
        final Pilot existing = new Pilot("Existing Pilot", CollaboratorEmail.valueOf("pilot@tap.pt"),
                CollaboratorPhone.valueOf("912345679"), atccUser(), company, Set.of(model));
        when(pilotRepository.findByEmail(CollaboratorEmail.valueOf("pilot@tap.pt"))).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> controller.addPilot("John Pilot", "pilot@tap.pt",
                "912345679", "Password1", Set.of(model)));

        verify(pilotRepository, never()).save(any(Pilot.class));
    }

    @Test
    void ensureMissingAtccProfileIsRejected() {
        final SystemUser user = atccUser();
        final UserSession session = mock(UserSession.class);
        when(session.authenticatedUser()).thenReturn(user);
        when(authz.session()).thenReturn(Optional.of(session));
        when(collaboratorRepository.findBySystemUser(user)).thenReturn(Optional.empty());
        when(pilotRepository.findByEmail(CollaboratorEmail.valueOf("pilot@tap.pt"))).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> controller.addPilot("John Pilot", "pilot@tap.pt",
                "912345679", "Password1", Set.of(aircraftModel())));
    }

    @Test
    void ensurePilotWithoutCertificationsIsRejected() {
        stubAuthenticatedAtcc(company());
        when(pilotRepository.findByEmail(CollaboratorEmail.valueOf("pilot@tap.pt"))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> controller.addPilot("John Pilot", "pilot@tap.pt",
                "912345679", "Password1", Set.of()));

        verify(pilotRepository, never()).save(any(Pilot.class));
    }
}
