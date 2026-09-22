/*
 * Copyright (c) 2013-2024 the original author or authors.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package eapli.alsafe.infrastructure.bootstrappers;

import eapli.alsafe.infrastructure.bootstrappers.register.AlSafeUserBootstrapper;
import eapli.alsafe.infrastructure.bootstrappers.register.BackofficeUsersBootstrapper;
import eapli.alsafe.infrastructure.bootstrappers.register.EmailDomainBootstrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.alsafe.usermanagement.domain.UserBuilderHelper;
import eapli.framework.actions.Action;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.infrastructure.authz.application.AuthenticationService;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.strings.util.Strings;
import eapli.framework.validations.Invariants;

/**
 * Bootstrapping data app
 *
 * @author Paulo Gandra de Sousa
 */
@SuppressWarnings("squid:S106")
public class Bootstrapper implements Action {
	private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrapper.class);

	private static final String ADMIN = "DefaultAdmin";
	private static final String BACKOFFICEOP = "DefaultBackofficeOP";
	private static final String ADMIN_PWD = "DefaultAdmin1";
	private static final String BACKOFFICEOP_PWD = "DefaultBackofficeOP1";

	private final AuthorizationService authz = AuthzRegistry.authorizationService();
	private final AuthenticationService authenticationService = AuthzRegistry.authenticationService();
	private final UserRepository userRepository = PersistenceContext.repositories().users();

	@Override
	public boolean execute() {
		final Action[] adminActions = { new EmailDomainBootstrapper(), new MasterUsersBootstrapper(),
				new BackofficeUsersBootstrapper(), new AlSafeUserBootstrapper() };

		final Action[] opActions = { new NodeBootstrapper(), new MakerBootstrapper(), new AirControlAreaBootstrapper(),
				new AirCompanyBootstrapper(), new EngineModelBootstrapper(), new AircraftModelBootstrapper(), new AirportBootstrapper(),
                new CollaboratorATCCBootstrapper(), new CollaboratorFCOBootstrapper()};

		registerAdmin();
		registerBackofficeOP();

		authenticateForBootstrapping(ADMIN, ADMIN_PWD);
		var ret = true;
		for (final Action boot : adminActions) {
			System.out.println("Bootstrapping " + nameOfEntity(boot) + "...");
			ret &= boot.execute();
		}

		authenticateForBootstrapping(BACKOFFICEOP, BACKOFFICEOP_PWD);
		for (final Action boot : opActions) {
			System.out.println("Bootstrapping " + nameOfEntity(boot) + "...");
			ret &= boot.execute();
		}

		return ret;
	}

	/**
	 * register an admin directly in the persistence layer as we need to
	 * circumvent authorizations in the Application Layer
	 */
	private boolean registerAdmin() {
		final var userBuilder = UserBuilderHelper.builder();
		userBuilder.withUsername(ADMIN).withPassword(ADMIN_PWD).withName("Default", "Administrator")
				.withEmail("def.adm@alsafe.com").withRoles(Roles.ADMIN);
		final var newUser = userBuilder.build();

		SystemUser admin;
		try {
			admin = userRepository.save(newUser);
			assert admin != null;
			return true;
		} catch (ConcurrencyException | IntegrityViolationException e) {
			LOGGER.warn("Assuming {} already exists (activate trace log for details)", newUser.username());
			LOGGER.trace("Assuming existing record", e);
			return false;
		}
	}

	private boolean registerBackofficeOP() {
		final var userBuilder = UserBuilderHelper.builder();
		userBuilder.withUsername(BACKOFFICEOP).withPassword(BACKOFFICEOP_PWD).withName("Default", "Operator")
				.withEmail("def.boop@alsafe.com").withRoles(Roles.BACKOFFICE_OPERATOR);
		final var newOp = userBuilder.build();

		SystemUser op;
		try {
			op = userRepository.save(newOp);
			assert op != null;
			return true;
		} catch (ConcurrencyException | IntegrityViolationException e) {
			LOGGER.warn("This username: {} is already in use)", newOp.username());
			LOGGER.trace("Assuming existing record", e);
			return false;
		}
	}

	/**
	 * authenticate a system user to be able to perform the actions
	 */
	protected void authenticateForBootstrapping(final String username, final String password) {
		authenticationService.authenticate(username, password);
		Invariants.ensure(authz.hasSession());
	}

	private String nameOfEntity(final Action boot) {
		final var name = boot.getClass().getSimpleName();
		return Strings.left(name, name.length() - "Bootstrapper".length());
	}
}
