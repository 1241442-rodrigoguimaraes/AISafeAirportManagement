package controller.collaborator;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaBoundaries;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaMinimumFuel;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.collaboratormanagement.application.EditCollaboratorController;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryFCO;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
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
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for US063 — edit collaborator email & phone.
 */
class EditCollaboratorControllerTest {

    private static class DenyingAuthorizationService extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {
            throw new SecurityException("forbidden");
        }
    }

    private AuthorizationService authz;
    private CollaboratorRepositoryFCO collaboratorRepositoryFCO;
    private CollaboratorRepositoryATCC collaboratorRepositoryATCC;
    private AirControlAreaRepository airControlAreaRepository;
    private AirCompanyRepository airCompanyRepository;
    private EditCollaboratorController controller;

    private static AirControlArea dummyArea() {
        final List<Coordinate> coords = List.of(
                new Coordinate(0.0, 0.0),
                new Coordinate(1.0, 0.0),
                new Coordinate(0.0, 1.0),
                new Coordinate(1.0, 1.0));
        return new AirControlArea(new AirControlAreaID(1L), "Area 1",
                new AirControlAreaBoundaries(coords), new AirControlAreaMinimumFuel(100.0));
    }

    private static AirTransportCompany dummyCompany() {
        return new AirTransportCompany(new IATACompanyCode("AA"), new ICAOCompanyCode("AAA"), "Test Co");
    }

    private static SystemUser dummyUser(final String username, final String email) {
        return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with(username, "Password1", "First", "Last", email)
                .withRoles(Set.of(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR).toArray(new Role[0]))
                .build();
    }

    private static CollaboratorFCO collaboratorFCO(final String name, final String email, final String phone,
                                                  final SystemUser user, final AirControlArea area) {
        return new CollaboratorFCO(name, CollaboratorEmail.valueOf(email), CollaboratorPhone.valueOf(phone),
                user, area);
    }

    private static CollaboratorATCC collaboratorATCC(final String name, final String email, final String phone,
                                                    final SystemUser user) {
        return new CollaboratorATCC(name, CollaboratorEmail.valueOf(email), CollaboratorPhone.valueOf(phone),
                user, dummyCompany());
    }

    @BeforeEach
    void setUp() {
        authz = mock(AuthorizationService.class);
        collaboratorRepositoryFCO = mock(CollaboratorRepositoryFCO.class);
        collaboratorRepositoryATCC = mock(CollaboratorRepositoryATCC.class);
        airControlAreaRepository = mock(AirControlAreaRepository.class);
        airCompanyRepository = mock(AirCompanyRepository.class);
        when(collaboratorRepositoryFCO.save(any(CollaboratorFCO.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(collaboratorRepositoryATCC.save(any(CollaboratorATCC.class))).thenAnswer(invocation -> invocation.getArgument(0));
        controller = new EditCollaboratorController(authz, collaboratorRepositoryFCO, collaboratorRepositoryATCC,
                airControlAreaRepository, airCompanyRepository);
    }

    @Test
    void ensureControllerRejectsNullAuthorizationService() {
        assertThrows(IllegalArgumentException.class,
                () -> new EditCollaboratorController(null, collaboratorRepositoryFCO, collaboratorRepositoryATCC, airControlAreaRepository,
                        airCompanyRepository));
    }

    @Test
    void ensureControllerRejectsNullCollaboratorRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> new EditCollaboratorController(authz, null, collaboratorRepositoryATCC, airControlAreaRepository, airCompanyRepository));
    }

    @Test
    void updateEmailAndPhone_withNewEmailAndPhone_persistsAndReturnsUpdatedCollaborator() {
        final AirControlArea area = dummyArea();
        final AirTransportCompany company = dummyCompany();
        final CollaboratorFCO subject = collaboratorFCO("Jane", "jane@old.com", "912345678",
                dummyUser("jane", "jane@old.com"), area);
        when(collaboratorRepositoryFCO.findByEmail(CollaboratorEmail.valueOf("jane@new.com"))).thenReturn(Optional.empty());

        final CollaboratorFCO result = controller.updateEmailAndPhoneFCO(subject, "jane@new.com", "923456789");

        assertEquals("jane@new.com", result.email().toString());
        assertEquals("923456789", result.phoneNumber());
        verify(collaboratorRepositoryFCO).findByEmail(CollaboratorEmail.valueOf("jane@new.com"));
        verify(collaboratorRepositoryFCO).save(subject);
        verify(authz).ensureAuthenticatedUserHasAnyOf(eq(Roles.BACKOFFICE_OPERATOR), eq(Roles.ADMIN));
    }

    @Test
    void updateEmailAndPhone_whenEmailUnchanged_doesNotQueryDuplicateEmail() {
        final CollaboratorFCO subject = collaboratorFCO("Bob", "bob@co.com", "911111111",
                dummyUser("bob", "bob@co.com"), dummyArea());

        final CollaboratorFCO result = controller.updateEmailAndPhoneFCO(subject, "bob@co.com", "922222222");

        assertEquals("922222222", result.phoneNumber());
        verify(collaboratorRepositoryFCO, never()).findByEmail(any(CollaboratorEmail.class));
        verify(collaboratorRepositoryFCO).save(subject);
    }

    @Test
    void updateEmailAndPhone_whenNewEmailBelongsToAnotherCollaborator_throws() {
        final AirControlArea area = dummyArea();
        final AirTransportCompany company = dummyCompany();
        final CollaboratorFCO subject = collaboratorFCO("One", "one@co.com", "911111111",
                dummyUser("u1", "one@co.com"), area);
        final CollaboratorFCO other = mock(CollaboratorFCO.class);
        when(other.sameAs(subject)).thenReturn(false);
        when(collaboratorRepositoryFCO.findByEmail(CollaboratorEmail.valueOf("taken@co.com")))
                .thenReturn(Optional.of(other));

        assertThrows(IllegalArgumentException.class,
                () -> controller.updateEmailAndPhoneFCO(subject, "taken@co.com", "933333333"));
        verify(collaboratorRepositoryFCO, never()).save(subject);
    }

    @Test
    void updateEmailAndPhone_whenUnauthorized_throwsAndDoesNotSave() {
        final var sut = new EditCollaboratorController(new DenyingAuthorizationService(), collaboratorRepositoryFCO, collaboratorRepositoryATCC,
                airControlAreaRepository, airCompanyRepository);
        final CollaboratorFCO subject = collaboratorFCO("X", "x@co.com", "911111111",
                dummyUser("ux", "x@co.com"), dummyArea());

        assertThrows(SecurityException.class, () -> sut.updateEmailAndPhoneFCO(subject, "y@co.com", "922222222"));
        verify(collaboratorRepositoryFCO, never()).save(any());
        verify(collaboratorRepositoryFCO, never()).findByEmail(any(CollaboratorEmail.class));
    }

    @Test
    void getAirTransportCompanies_checksPermissionAndReturnsRepositoryData() {
        final AirTransportCompany c = dummyCompany();
        when(airCompanyRepository.findAll()).thenReturn(List.of(c));

        final Iterable<AirTransportCompany> all = controller.getAirTransportCompanies();

        assertTrue(all.iterator().hasNext());
        assertEquals(c, all.iterator().next());
        verify(authz).ensureAuthenticatedUserHasAnyOf(eq(Roles.BACKOFFICE_OPERATOR), eq(Roles.ADMIN));
    }

    @Test
    void activeCollaboratorsOfCompany_checksPermissionAndDelegates() {
        final AirTransportCompany company = dummyCompany();
        final CollaboratorATCC col = collaboratorATCC("C", "c@co.com", "911111111",
                dummyUser("uc", "c@co.com"));
        when(collaboratorRepositoryATCC.findAll()).thenReturn(List.of(col));

        final Iterable<CollaboratorATCC> list = controller.activeCollaboratorsOfCompany(company);

        assertEquals(col, list.iterator().next());
        verify(collaboratorRepositoryATCC).findAll();
    }
}
