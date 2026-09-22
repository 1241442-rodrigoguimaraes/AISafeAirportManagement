package controller.collaborator;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaBoundaries;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaMinimumFuel;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.collaboratormanagement.application.CollaboratorDTO;
import eapli.alsafe.collaboratormanagement.application.ListCollaboratorsController;
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
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for US062 — {@link ListCollaboratorsController}.
 */
class ListCollaboratorsControllerTest {

    private AuthorizationService authz;
    private AirCompanyRepository companyRepository;
    private AirControlAreaRepository areaRepository;
    private CollaboratorRepositoryFCO collaboratorRepositoryFCO;
    private CollaboratorRepositoryATCC collaboratorRepositoryATCC;
    private ListCollaboratorsController controller;

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
        companyRepository = mock(AirCompanyRepository.class);
        areaRepository = mock(AirControlAreaRepository.class);
        collaboratorRepositoryFCO = mock(CollaboratorRepositoryFCO.class);
        collaboratorRepositoryATCC = mock(CollaboratorRepositoryATCC.class);
        controller = new ListCollaboratorsController(authz, companyRepository, areaRepository,
                collaboratorRepositoryFCO, collaboratorRepositoryATCC);
    }

    @Test
    void listActiveCollaboratorsOfArea_returnsOnlyActiveFcos() {
        final AirControlArea area = dummyArea();
        final CollaboratorFCO active = new CollaboratorFCO("Active FCO",
                CollaboratorEmail.valueOf("active@fca.com"),
                CollaboratorPhone.valueOf("+351911111111"),
                activeUser("active@fca.com"), area);
        final CollaboratorDTO expected = CollaboratorDTO.fromCollaboratorFCO(active);

        when(collaboratorRepositoryFCO.findActiveByCustomerArea(area)).thenReturn(List.of(active));

        final List<CollaboratorDTO> result = toList(controller.listActiveCollaboratorsOfArea(area));

        assertEquals(1, result.size());
        assertEquals(expected.email, result.get(0).email);
        verify(authz).ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);
        verify(collaboratorRepositoryFCO).findActiveByCustomerArea(area);
    }

    @Test
    void listActiveCollaboratorsOfCompany_returnsOnlyActiveAtccs() {
        final AirTransportCompany company = dummyCompany();
        final CollaboratorATCC active = new CollaboratorATCC("Active ATCC",
                CollaboratorEmail.valueOf("atcc@tap.com"),
                CollaboratorPhone.valueOf("+351922222222"),
                activeUser("atcc@tap.com"), company);

        when(collaboratorRepositoryATCC.findActiveByCompany(company)).thenReturn(List.of(active));

        final List<CollaboratorATCC> result = toList(controller.listActiveCollaboratorsOfCompany(company));

        assertEquals(1, result.size());
        assertEquals("atcc@tap.com", result.get(0).email().toString());
        verify(collaboratorRepositoryATCC).findActiveByCompany(company);
    }

    @Test
    void listActiveCollaboratorsOfArea_whenUnauthorized_throws() {
        doThrow(new SecurityException("forbidden")).when(authz)
                .ensureAuthenticatedUserHasAnyOf(eq(Roles.BACKOFFICE_OPERATOR), eq(Roles.ADMIN));

        final AirControlArea area = mock(AirControlArea.class);
        assertThrows(SecurityException.class, () -> controller.listActiveCollaboratorsOfArea(area));
    }

    @Test
    void constructor_rejectsNullDependencies() {
        assertThrows(IllegalArgumentException.class,
                () -> new ListCollaboratorsController(null, companyRepository, areaRepository,
                        collaboratorRepositoryFCO, collaboratorRepositoryATCC));
    }

    private static <T> List<T> toList(final Iterable<T> iterable) {
        final List<T> list = new java.util.ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }
}
