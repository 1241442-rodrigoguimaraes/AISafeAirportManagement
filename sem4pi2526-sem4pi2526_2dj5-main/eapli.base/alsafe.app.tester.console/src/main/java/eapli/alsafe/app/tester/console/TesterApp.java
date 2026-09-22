package eapli.alsafe.app.tester.console;

import eapli.alsafe.alsafeusermanagement.application.eventhandlers.NewUserRegisteredFromSignupWatchDog;
import eapli.alsafe.alsafeusermanagement.domain.events.NewUserRegisteredFromSignupEvent;
import eapli.alsafe.alsafeusermanagement.domain.events.SignupAcceptedEvent;
import eapli.alsafe.app.common.console.BaseApp;
import eapli.alsafe.app.common.console.presentation.authz.LoginUI;
import eapli.alsafe.app.tester.console.presentation.menus.TesterMainMenu;
import eapli.alsafe.infrastructure.authz.AuthenticationCredentialHandler;
import eapli.alsafe.infrastructure.bootstrappers.register.TesterUserBootstrapper;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.application.eventhandlers.SignupAcceptedWatchDog;
import eapli.alsafe.usermanagement.domain.AlSafePasswordPolicy;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.alsafe.usermanagement.domain.UserBuilderHelper;
import eapli.framework.infrastructure.authz.application.AuthenticationService;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.pubsub.EventDispatcher;
import eapli.framework.validations.Invariants;

@SuppressWarnings("squid:S106")
public final class TesterApp extends BaseApp {

    private static final String ADMIN_USER = "DefaultAdmin";
    private static final String ADMIN_PWD = "DefaultAdmin1";
    private static final String TESTER_USER = "tester";

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final AuthenticationService authenticationService = AuthzRegistry.authenticationService();
    private final UserRepository userRepository = PersistenceContext.repositories().users();

    private TesterApp() {
    }

    public static void main(final String[] args) {
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new AlSafePasswordPolicy(),
                new PlainTextEncoder());

        new TesterApp().run(args);
    }

    @Override
    protected void doMain(final String[] args) {
        registerAdmin();
        authenticateForBootstrapping(ADMIN_USER, ADMIN_PWD);
        if (userRepository.ofIdentity(Username.valueOf(TESTER_USER)).isEmpty()) {
            new TesterUserBootstrapper().execute();
        }

        if (new LoginUI(new AuthenticationCredentialHandler()).show()) {
            final var menu = new TesterMainMenu();
            menu.mainLoop();
        }
    }

    private void registerAdmin() {
        if (userRepository.ofIdentity(Username.valueOf(ADMIN_USER)).isPresent()) {
            return;
        }

        final var userBuilder = UserBuilderHelper.builder();
        userBuilder.withUsername(ADMIN_USER).withPassword(ADMIN_PWD).withName("Default", "Administrator")
                .withEmail("def.adm@alsafe.com").withRoles(Roles.ADMIN);
        userRepository.save(userBuilder.build());
    }

    private void authenticateForBootstrapping(final String username, final String password) {
        authenticationService.authenticate(username, password);
        Invariants.ensure(authz.hasSession());
    }

    @Override
    protected String appTitle() {
        return "Tester App (H2 Local)";
    }

    @Override
    protected String appGoodbye() {
        return "Tester App - Goodbye";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doSetupEventHandlers(final EventDispatcher dispatcher) {
        dispatcher.subscribe(new NewUserRegisteredFromSignupWatchDog(), NewUserRegisteredFromSignupEvent.class);
        dispatcher.subscribe(new SignupAcceptedWatchDog(), SignupAcceptedEvent.class);
    }
}
