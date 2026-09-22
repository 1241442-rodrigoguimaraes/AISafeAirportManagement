package controller.alsafeusermanagement;

import eapli.alsafe.alsafeusermanagement.domain.AlSafeUser;
import eapli.alsafe.alsafeusermanagement.domain.AlSafeUserEmail;
import eapli.alsafe.alsafeusermanagement.domain.EmailDomain;
import eapli.alsafe.alsafeusermanagement.repositories.AlSafeUserRepository;
import eapli.alsafe.alsafeusermanagement.repositories.EmailDomainRepository;
import eapli.alsafe.usermanagement.application.AddUserController;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.alsafe.usermanagement.domain.UserBuilderHelper;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AddUserControllerTest {

    private static class FakeAuthorizationService extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {}
    }

    private static class FakeUserManagementService extends UserManagementService {
        public FakeUserManagementService() {
            super(null, null, null);
        }

        @Override
        public SystemUser registerNewUser(final String username, final String password,
                                          final String firstName, final String lastName,
                                          final String email, final Set<Role> roles,
                                          final Calendar createdOn) {

            return UserBuilderHelper.builder().withUsername(username).withPassword(password)
                    .withName(firstName, lastName).withEmail(email).withRoles(roles.toArray(new Role[0]))
                    .build();
        }
    }

    private static class FakeEmailDomainRepository implements EmailDomainRepository {
        private final List<String> allowedDomains = new ArrayList<>();
        private final List<EmailDomain> emailDomains = new ArrayList<>();

        public void addDomain(final String domain) {
            allowedDomains.add(domain);
        }

        @Override
        public boolean contains(final String domain) {
            return allowedDomains.contains(domain);
        }

        @Override
        public void save(final String domain) {
            allowedDomains.add(domain);
        }

        @Override
        public EmailDomain save(final EmailDomain domain) {
            emailDomains.add(domain);
            return domain;
        }

        @Override
        public List<EmailDomain> findAll() {
            return emailDomains;
        }

        @Override
        public Optional<EmailDomain> ofIdentity(final String domain) {
            return emailDomains.stream().filter(d -> d.getDomain().equals(domain)).findFirst();
        }

        @Override
        public void delete(final EmailDomain o) {}

        @Override
        public void deleteOfIdentity(final String domain) {}

        @Override
        public long count() {
            return emailDomains.size();
        }
    }

    private static class FakeAlSafeUserRepository implements AlSafeUserRepository {
        public final List<AlSafeUser> store = new ArrayList<>();

        @Override
        public AlSafeUser save(final AlSafeUser user) {
            store.add(user);
            return user;
        }

        @Override
        public Optional<AlSafeUser> findBySystemUser(final SystemUser systemUser) {
            return store.stream()
                    .filter(u -> u.user().equals(systemUser))
                    .findFirst();
        }

        @Override
        public Optional<AlSafeUser> findByUsername(final Username username) {
            return store.stream()
                    .filter(u -> u.user().username().equals(username))
                    .findFirst();
        }

        @Override
        public long count() {
            return store.size();
        }

        @Override
        public Iterable<AlSafeUser> findAll() {
            return store;
        }

        @Override
        public Iterable<AlSafeUser> findAllActive() {
            return store.stream().filter(u -> u.user().isActive()).toList();
        }

        @Override
        public Optional<AlSafeUser> ofIdentity(final AlSafeUserEmail id) {
            return Optional.empty();
        }

        @Override
        public void delete(final AlSafeUser user) {
            store.remove(user);
        }

        @Override
        public void deleteOfIdentity(final AlSafeUserEmail id) {}

        @Override
        public boolean containsOfIdentity(final AlSafeUserEmail id) {
            return false;
        }
    }

    private FakeAlSafeUserRepository alSafeUserRepository;
    private FakeEmailDomainRepository emailDomainRepository;
    private FakeUserManagementService userManagementService;
    private AddUserController controller;

    private static final String USERNAME = "jdoe";
    private static final String PASSWORD = "Password1";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String EMAIL = "jdoe@alsafe.com";
    private static final String PHONE = "912345678";
    private static final Set<Role> ROLES = Set.of(Roles.BACKOFFICE_OPERATOR);

    @BeforeEach
    void setUp() {
        alSafeUserRepository = new FakeAlSafeUserRepository();
        emailDomainRepository = new FakeEmailDomainRepository();
        emailDomainRepository.addDomain("alsafe.com");
        userManagementService = new FakeUserManagementService();
        controller = new AddUserController(
                        new FakeAuthorizationService(),
                        userManagementService,
                        alSafeUserRepository,
                        emailDomainRepository);
    }

    @Test
    void ensureControllerCannotBeCreatedWithNullAuthz() {
        assertThrows(IllegalArgumentException.class, () ->
                new AddUserController(null, userManagementService, alSafeUserRepository, emailDomainRepository)
        );
    }

    @Test
    void ensureControllerCannotBeCreatedWithNullUserService() {
        assertThrows(IllegalArgumentException.class, () ->
                new AddUserController(new FakeAuthorizationService(), null, alSafeUserRepository, emailDomainRepository)
        );
    }

    @Test
    void ensureControllerCannotBeCreatedWithNullAlSafeUserRepository() {
        assertThrows(IllegalArgumentException.class, () ->
                new AddUserController(new FakeAuthorizationService(), userManagementService, null, emailDomainRepository)
        );
    }

    @Test
    void ensureControllerCannotBeCreatedWithNullEmailDomainRepository() {
        assertThrows(IllegalArgumentException.class, () ->
                new AddUserController(new FakeAuthorizationService(), userManagementService, alSafeUserRepository, null)
        );
    }

    @Test
    void ensureValidEmailDomainIsAccepted() {
        assertDoesNotThrow(() -> controller.validateEmail(EMAIL));
    }

    @Test
    void ensureInvalidEmailDomainIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.validateEmail("jdoe@unknown.com")
        );
    }

    @Test
    void ensureUserIsRegisteredAndSaved() {
        AlSafeUser result = controller.addUser(USERNAME, PASSWORD, FIRST_NAME, LAST_NAME, EMAIL, PHONE, ROLES);

        assertNotNull(result);
        assertEquals(1, alSafeUserRepository.store.size());
    }

    @Test
    void ensureUserCannotBeRegisteredWithInvalidEmailDomain() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.addUser(USERNAME, PASSWORD, FIRST_NAME, LAST_NAME, "jdoe@unknown.com", PHONE, ROLES)
        );
    }

    @Test
    void ensureDuplicateSystemUserIsRejected() {
        controller.addUser(USERNAME, PASSWORD, FIRST_NAME, LAST_NAME, EMAIL, PHONE, ROLES);

        assertThrows(IllegalArgumentException.class, () ->
                controller.addUser(USERNAME, PASSWORD, FIRST_NAME, LAST_NAME, EMAIL, PHONE, ROLES)
        );
    }
}