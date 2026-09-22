package controller.collaborator;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaBoundaries;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaMinimumFuel;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.collaboratormanagement.application.CollaboratorDTO;
import eapli.alsafe.collaboratormanagement.application.DisableCollaboratorController;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryFCO;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.IATACompanyCode;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.alsafe.companies.repositories.AirCompanyRepository;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for US064 — {@link DisableCollaboratorController}.
 */
class DisableCollaboratorControllerTest {

    private AuthorizationService authz;
    private UserManagementService userSvc;
    private AirCompanyRepository companyRepository;
    private AirControlAreaRepository areaRepository;
    private CollaboratorRepositoryFCO collaboratorRepositoryFCO;
    private CollaboratorRepositoryATCC collaboratorRepositoryATCC;
    private DisableCollaboratorController controller;

    private static SystemUser activeUser(final String username) {
        return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with(username, "Password1", "First", "Last", username)
                .withRoles(Set.of(Roles.FLIGHT_CONTROL_OPERATOR).toArray(new Role[0]))
                .build();
    }

    private static AirControlArea dummyArea() {
        final List<Coordinate> coords = List.of(
                new Coordinate(10.0, 10.0),
                new Coordinate(20.0, 10.0),
                new Coordinate(10.0, 20.0),
                new Coordinate(20.0, 20.0));
        return new AirControlArea(
                new AirControlAreaID(1L),
                "Portugal",
                new AirControlAreaBoundaries(coords),
                new AirControlAreaMinimumFuel(100.0));
    }

    private static AirTransportCompany dummyCompany() {
        return new AirTransportCompany(new IATACompanyCode("TP"), new ICAOCompanyCode("TAP"), "TAP Air");
    }

    @BeforeEach
    void setUp() {
        authz = mock(AuthorizationService.class);
        userSvc = mock(UserManagementService.class);
        companyRepository = mock(AirCompanyRepository.class);
        areaRepository = mock(AirControlAreaRepository.class);
        collaboratorRepositoryFCO = mock(CollaboratorRepositoryFCO.class);
        collaboratorRepositoryATCC = mock(CollaboratorRepositoryATCC.class);
        controller = new DisableCollaboratorController(authz, userSvc, companyRepository, areaRepository,
                collaboratorRepositoryFCO, collaboratorRepositoryATCC);
    }

    @Test
    void disableCollaboratorFCO_activeUser_deactivatesAndReturnsTrue() {
        final AirControlArea area = dummyArea();
        final SystemUser user = activeUser("fco@area.com");
        final CollaboratorFCO collaborator = new CollaboratorFCO("FCO One",
                CollaboratorEmail.valueOf("fco@area.com"),
                CollaboratorPhone.valueOf("+351911111111"), user, area);
        final CollaboratorDTO dto = CollaboratorDTO.fromCollaboratorFCO(collaborator);

        when(collaboratorRepositoryFCO.findByEmail(CollaboratorEmail.valueOf("fco@area.com")))
                .thenReturn(Optional.of(collaborator));

        assertTrue(controller.disableCollaboratorFCO(dto));
        verify(userSvc).deactivateUser(user);
    }

    @Test
    void disableCollaboratorFCO_alreadyInactive_returnsFalseWithoutDeactivate() {
        final AirControlArea area = dummyArea();
        final SystemUser inactive = mock(SystemUser.class);
        when(inactive.isActive()).thenReturn(false);
        final CollaboratorFCO collaborator = new CollaboratorFCO("Inactive FCO",
                CollaboratorEmail.valueOf("inactive@area.com"),
                CollaboratorPhone.valueOf("+351912222222"), inactive, area);
        final CollaboratorDTO dto = CollaboratorDTO.fromCollaboratorFCO(collaborator);

        when(collaboratorRepositoryFCO.findByEmail(CollaboratorEmail.valueOf("inactive@area.com")))
                .thenReturn(Optional.of(collaborator));

        assertFalse(controller.disableCollaboratorFCO(dto));
        verify(userSvc, never()).deactivateUser(any());
    }

    @Test
    void disableCollaboratorATCC_activeUser_deactivatesAndReturnsTrue() {
        final AirTransportCompany company = dummyCompany();
        final SystemUser user = activeUser("atcc@tap.com");
        final CollaboratorATCC collaborator = new CollaboratorATCC("ATCC One",
                CollaboratorEmail.valueOf("atcc@tap.com"),
                CollaboratorPhone.valueOf("+351922222222"), user, company);
        final CollaboratorDTO dto = CollaboratorDTO.fromCollaboratorATCC(collaborator);

        when(collaboratorRepositoryATCC.findByEmail(CollaboratorEmail.valueOf("atcc@tap.com")))
                .thenReturn(Optional.of(collaborator));

        assertTrue(controller.disableCollaboratorATCC(dto));
        verify(userSvc).deactivateUser(user);
    }

    @Test
    void getActiveCollaboratorsOfCompany_usesFindActiveByCompany() {
        final AirTransportCompany company = dummyCompany();
        final CollaboratorATCC active = new CollaboratorATCC("ATCC",
                CollaboratorEmail.valueOf("a@tap.com"),
                CollaboratorPhone.valueOf("+351933333333"),
                activeUser("a@tap.com"), company);

        when(collaboratorRepositoryATCC.findActiveByCompany(company)).thenReturn(List.of(active));

        final List<CollaboratorDTO> result = toList(controller.getActiveCollaboratorsOfCompany(company));

        assertEquals(1, result.size());
        assertEquals("a@tap.com", result.get(0).email);
        verify(collaboratorRepositoryATCC).findActiveByCompany(company);
        verify(collaboratorRepositoryATCC, never()).findAll();
    }

    @Test
    void disableCollaboratorFCO_whenUnauthorized_throws() {
        doThrow(new SecurityException("forbidden")).when(authz)
                .ensureAuthenticatedUserHasAnyOf(eq(Roles.BACKOFFICE_OPERATOR), eq(Roles.ADMIN));

        final CollaboratorDTO dto = new CollaboratorDTO("N", "x@y.com", "+351900000000", "ROLE");

        assertThrows(SecurityException.class, () -> controller.disableCollaboratorFCO(dto));
        verify(userSvc, never()).deactivateUser(any());
    }

    private static List<CollaboratorDTO> toList(final Iterable<CollaboratorDTO> iterable) {
        final List<CollaboratorDTO> list = new java.util.ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }
}
