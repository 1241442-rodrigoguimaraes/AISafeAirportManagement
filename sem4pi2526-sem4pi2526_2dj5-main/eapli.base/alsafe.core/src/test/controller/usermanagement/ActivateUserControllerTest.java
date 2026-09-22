//package controller.usermanagement;
//
//import eapli.alsafe.usermanagement.application.ActivateUserController;
//import eapli.alsafe.usermanagement.domain.Roles;
//import eapli.alsafe.usermanagement.domain.UserBuilderHelper;
//import eapli.framework.infrastructure.authz.application.AuthorizationService;
//import eapli.framework.infrastructure.authz.application.AuthzRegistry;
//import eapli.framework.infrastructure.authz.application.UserManagementService;
//import eapli.framework.infrastructure.authz.domain.model.Role;
//import eapli.framework.infrastructure.authz.domain.model.SystemUser;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.MockedStatic;
//
//import java.util.List;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertIterableEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertSame;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.mockStatic;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
///**
// * Unit tests for US032 — activate user flow ({@link ActivateUserController}).
// * Uses Mockito static mock of {@link AuthzRegistry}; production controllers are unchanged.
// */
//class ActivateUserControllerTest {
//
//    private MockedStatic<AuthzRegistry> authzRegistryStatic;
//    private AuthorizationService authz;
//    private UserManagementService userSvc;
//
//    @BeforeEach
//    void setUp() {
//        authz = mock(AuthorizationService.class);
//        userSvc = mock(UserManagementService.class);
//        authzRegistryStatic = mockStatic(AuthzRegistry.class);
//        authzRegistryStatic.when(AuthzRegistry::authorizationService).thenReturn(authz);
//        authzRegistryStatic.when(AuthzRegistry::userService).thenReturn(userSvc);
//    }
//
//    @AfterEach
//    void tearDown() {
//        authzRegistryStatic.close();
//    }
//
//    private static SystemUser sampleUser(final String username) {
//        return UserBuilderHelper.builder()
//                .withUsername(username)
//                .withPassword("Password1")
//                .withName("Test", "User")
//                .withEmail(username + "@alsafe.com")
//                .withRoles(Set.of(Roles.BACKOFFICE_OPERATOR).toArray(new Role[0]))
//                .build();
//    }
//
//    @Test
//    void deactivatedUsers_withMultipleUsers_returnsCompleteListFromService() {
//        // Arrange
//        final SystemUser u1 = sampleUser("eve");
//        final SystemUser u2 = sampleUser("frank");
//        when(userSvc.deactivatedUsers()).thenReturn(List.of(u1, u2));
//
//        // Act
//        final Iterable<SystemUser> result = new ActivateUserController().deactivatedUsers();
//
//        // Assert
//        verify(authz).ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);
//        verify(userSvc).deactivatedUsers();
//        assertIterableEquals(List.of(u1, u2), result);
//    }
//
//    @Test
//    void deactivatedUsers_whenServiceReturnsEmpty_returnsEmptyIterable() {
//        // Arrange
//        when(userSvc.deactivatedUsers()).thenReturn(List.of());
//
//        // Act
//        final Iterable<SystemUser> result = new ActivateUserController().deactivatedUsers();
//
//        // Assert
//        assertNotNull(result);
//        assertFalse(result.iterator().hasNext());
//        verify(userSvc).deactivatedUsers();
//    }
//
//    @Test
//    void activateUser_delegatesToUserServiceAndReturnsResult() {
//        // Arrange
//        final SystemUser input = sampleUser("grace");
//        final SystemUser returned = sampleUser("grace");
//        when(userSvc.activateUser(input)).thenReturn(returned);
//
//        // Act
//        final SystemUser out = new ActivateUserController().activateUser(input);
//
//        // Assert
//        verify(authz).ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);
//        verify(userSvc).activateUser(input);
//        assertSame(returned, out);
//    }
//
//    @Test
//    void deactivatedUsers_whenUnauthorized_throwsAndDoesNotCallUserService() {
//        // Arrange
//        doThrow(new SecurityException("forbidden")).when(authz).ensureAuthenticatedUserHasAnyOf(
//                eq(Roles.BACKOFFICE_OPERATOR), eq(Roles.ADMIN));
//
//        // Act + Assert
//        assertThrows(SecurityException.class, () -> new ActivateUserController().deactivatedUsers());
//        verify(userSvc, never()).deactivatedUsers();
//    }
//
//    @Test
//    void activateUser_whenUnauthorized_throwsAndDoesNotCallUserService() {
//        // Arrange
//        doThrow(new SecurityException("forbidden")).when(authz).ensureAuthenticatedUserHasAnyOf(
//                eq(Roles.BACKOFFICE_OPERATOR), eq(Roles.ADMIN));
//
//        // Act + Assert
//        assertThrows(SecurityException.class,
//                () -> new ActivateUserController().activateUser(sampleUser("henry")));
//        verify(userSvc, never()).activateUser(any());
//    }
//}
