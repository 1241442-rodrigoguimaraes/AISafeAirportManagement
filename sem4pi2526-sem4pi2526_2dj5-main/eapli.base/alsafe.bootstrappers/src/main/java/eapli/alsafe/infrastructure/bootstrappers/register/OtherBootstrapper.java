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
package eapli.alsafe.infrastructure.bootstrappers.register;

import eapli.alsafe.infrastructure.bootstrappers.MakerBootstrapper;
import eapli.alsafe.infrastructure.bootstrappers.NodeBootstrapper;
import eapli.alsafe.infrastructure.bootstrappers.TestDataConstants;
import eapli.framework.actions.Action;
import eapli.framework.infrastructure.authz.application.AuthenticationService;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.strings.util.Strings;
import eapli.framework.validations.Invariants;

/**
 * Bootstrapping data app
 *
 * @author Paulo Gandra de Sousa
 */
@SuppressWarnings("squid:S106")
public class OtherBootstrapper implements Action {

	private static final String ADMIN_PWD = TestDataConstants.PASSWORD1;
	private static final String ADMIN = "Admin";

	private final AuthorizationService authz = AuthzRegistry.authorizationService();
	private final AuthenticationService authenticationService = AuthzRegistry.authenticationService();

	@Override
	public boolean execute() {
		// declare bootstrap actions
		final Action[] actions = { };

		authenticateForBootstrapping();

		// execute all bootstrapping
		var ret = true;
		for (final Action boot : actions) {
			System.out.println("Bootstrapping " + nameOfEntity(boot) + "...");
			ret &= boot.execute();
		}
		return ret;
	}

	/**
	 * authenticate an admin to be able to register new users
	 *
	 */
	protected void authenticateForBootstrapping() {
		authenticationService.authenticate(ADMIN, ADMIN_PWD);
		Invariants.ensure(authz.hasSession());
	}

	private String nameOfEntity(final Action boot) {
		final var name = boot.getClass().getSimpleName();
		return Strings.left(name, name.length() - "Bootstrapper".length());
	}
}
