/*
 * Copyright (c) 2013-2024 the original author or authors.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package eapli.alsafe.app.bootstrap;

import eapli.alsafe.app.common.console.BaseApp;
import eapli.alsafe.infrastructure.bootstrappers.Bootstrapper;
import eapli.alsafe.infrastructure.bootstrappers.register.OtherBootstrapper;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.infrastructure.smoketest.SmokeTester;
import eapli.alsafe.usermanagement.application.eventhandlers.SignupAcceptedWatchDog;
import eapli.alsafe.usermanagement.domain.AlSafePasswordPolicy;
import eapli.alsafe.alsafeusermanagement.application.eventhandlers.NewUserRegisteredFromSignupWatchDog;
import eapli.alsafe.alsafeusermanagement.domain.events.NewUserRegisteredFromSignupEvent;
import eapli.alsafe.alsafeusermanagement.domain.events.SignupAcceptedEvent;
import eapli.framework.collections.util.ArrayPredicates;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.pubsub.EventDispatcher;

/**
 * Bootstrapping data app
 */
@SuppressWarnings("squid:S106")
public final class Bootstrap extends BaseApp {
	/**
	 * avoid instantiation of this class.
	 */
	private Bootstrap() {
	}

	private boolean isToBootstrapData;
    private boolean isToRunSampleE2E;

	public static void main(final String[] args) {
		AuthzRegistry.configure(PersistenceContext.repositories().users(), new AlSafePasswordPolicy(), new PlainTextEncoder());

		new Bootstrap().run(args);
	}

	@Override
	protected void doMain(final String[] args) {
		handleArgs(args);

		System.out.println("\n\n------- MASTER DATA -------");
		new Bootstrapper().execute();

		if (isToBootstrapData) {
			System.out.println("\n\n------- DEMO DATA -------");
			new OtherBootstrapper().execute();


		}

		if (isToRunSampleE2E) {
			System.out.println("\n\n------- BASIC SCENARIO -------");
			new SmokeTester().execute();
		}
	}

	private void handleArgs(final String[] args) {
		isToRunSampleE2E = ArrayPredicates.contains(args, "-smoke:basic");
        if (isToRunSampleE2E) {
			isToBootstrapData = true;
		} else {
			isToBootstrapData = ArrayPredicates.contains(args, "-bootstrap:demo");
		}
	}

	@Override
	protected String appTitle() {
		return "Bootstrapping data ";
	}

	@Override
	protected String appGoodbye() {
		return "Bootstrap data done.";
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doSetupEventHandlers(final EventDispatcher dispatcher) {
		dispatcher.subscribe(new NewUserRegisteredFromSignupWatchDog(), NewUserRegisteredFromSignupEvent.class);
		dispatcher.subscribe(new SignupAcceptedWatchDog(), SignupAcceptedEvent.class);
	}
}
