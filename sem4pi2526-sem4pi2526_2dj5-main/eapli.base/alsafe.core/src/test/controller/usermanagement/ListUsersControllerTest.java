package controller.usermanagement;

import eapli.alsafe.alsafeusermanagement.domain.AlSafeUser;
import eapli.alsafe.alsafeusermanagement.domain.AlSafeUserBuilder;
import eapli.alsafe.alsafeusermanagement.repositories.AlSafeUserRepository;
import eapli.alsafe.usermanagement.application.ListUsersController;
import eapli.alsafe.usermanagement.application.dto.UserDTO;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.alsafe.usermanagement.domain.UserBuilderHelper;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * US033 — list users controller (AAA + Mockito).
 */
class ListUsersControllerTest {

    private AuthorizationService authz;
    private AlSafeUserRepository alSafeUserRepository;
    private UserManagementService userManagementService;
    private ListUsersController controller;

    @BeforeEach
    void setUp() {
        authz = mock(AuthorizationService.class);
        alSafeUserRepository = mock(AlSafeUserRepository.class);
        userManagementService = mock(UserManagementService.class);
        controller = new ListUsersController(authz, alSafeUserRepository, userManagementService);
    }

    private static AlSafeUser alsafeUser(final SystemUser su, final String phone) {
        final Calendar createdOn = Calendar.getInstance();
        return new AlSafeUserBuilder().with(su, phone, createdOn).build();
    }

    @Test
    void listUsers_withMultipleUsers_returnsCompleteList() {
        final SystemUser u1 = UserBuilderHelper.builder()
                .withUsername("alice")
                .withPassword("Password1")
                .withName("Alice", "One")
                .withEmail("alice@alsafe.com")
                .withRoles(Set.of(Roles.BACKOFFICE_OPERATOR).toArray(new Role[0]))
                .build();
        final SystemUser u2 = UserBuilderHelper.builder()
                .withUsername("bob")
                .withPassword("Password1")
                .withName("Bob", "Two")
                .withEmail("bob@alsafe.com")
                .withRoles(Set.of(Roles.ADMIN).toArray(new Role[0]))
                .build();
        final AlSafeUser a1 = alsafeUser(u1, "911111111");
        final AlSafeUser a2 = alsafeUser(u2, "922222222");
        when(alSafeUserRepository.findAll()).thenReturn(List.of(a1, a2));

        final List<UserDTO> result = controller.getUsers();

        verify(authz).ensureAuthenticatedUserHasAnyOf(eq(Roles.BACKOFFICE_OPERATOR), eq(Roles.ADMIN));
        assertEquals(2, result.size());
        final UserDTO d1 = result.stream().filter(d -> d.email().contains("alice")).findFirst().orElseThrow();
        assertEquals("911111111", d1.phoneNumber());
        assertEquals("Active", d1.status());
        assertEquals(Roles.BACKOFFICE_OPERATOR.toString(), d1.role());
        assertEquals(u1.name().toString(), d1.name());
        assertNotNull(d1.securityClearanceExpiration());
        assertNotNull(d1.lastSkillsAssessmentDate());

        final UserDTO d2 = result.stream().filter(d -> d.email().contains("bob")).findFirst().orElseThrow();
        assertEquals("922222222", d2.phoneNumber());
        assertEquals(Roles.ADMIN.toString(), d2.role());
    }

    @Test
    void listUsers_whenRepositoryIsEmpty_returnsEmptyList() {
        when(alSafeUserRepository.findAll()).thenReturn(List.of());

        final List<UserDTO> result = controller.getUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(alSafeUserRepository).findAll();
    }

    @Test
    void listUsers_statusAccuracy_disabledUserAppearsAsDisabled() {
        final SystemUser su = UserBuilderHelper.builder()
                .withUsername("carl")
                .withPassword("Password1")
                .withName("Carl", "Off")
                .withEmail("carl@alsafe.com")
                .withRoles(Set.of(Roles.BACKOFFICE_OPERATOR).toArray(new Role[0]))
                .build();
        su.deactivate(Calendar.getInstance());
        final AlSafeUser au = alsafeUser(su, "933333333");
        when(alSafeUserRepository.findAll()).thenReturn(List.of(au));

        final List<UserDTO> result = controller.getUsers();

        assertEquals(1, result.size());
        assertEquals("Disabled", result.get(0).status());
    }

    @Test
    void listUsers_unauthorizedUser_throwsException() {
        doThrow(new SecurityException("denied")).when(authz).ensureAuthenticatedUserHasAnyOf(
                eq(Roles.BACKOFFICE_OPERATOR), eq(Roles.ADMIN));

        assertThrows(SecurityException.class, () -> controller.getUsers());
        verify(alSafeUserRepository, never()).findAll();
    }
}
