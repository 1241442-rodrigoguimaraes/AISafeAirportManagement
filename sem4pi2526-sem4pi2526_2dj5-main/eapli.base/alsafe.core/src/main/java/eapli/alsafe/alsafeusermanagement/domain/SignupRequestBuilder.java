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
package eapli.alsafe.alsafeusermanagement.domain;

import java.util.Calendar;

import eapli.alsafe.usermanagement.domain.AlSafePasswordPolicy;
import eapli.framework.general.domain.model.EmailAddress;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import eapli.framework.domain.model.DomainFactory;
import eapli.framework.infrastructure.authz.domain.model.Name;
import eapli.framework.infrastructure.authz.domain.model.Password;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.time.util.CurrentTimeCalendars;

/**
 * A factory for Signup Requests.
 *
 * <p>
 * This class demonstrates the use of the factory (DDD) pattern using a fluent
 * interface. It acts as a Builder (GoF).
 *
 * @author Jorge Santos ajs@isep.ipp.pt 02/04/2016
 */
@Component
public class SignupRequestBuilder implements DomainFactory<SignupRequest> {

	private final AlSafePasswordPolicy policy;
	private final PasswordEncoder encoder;

	private String username;
	private Password password;
	private String firstName;
	private String lastName;
	private String email;
	private Calendar createdOn;

	public SignupRequestBuilder(final AlSafePasswordPolicy policy, final PasswordEncoder encoder) {
		this.policy = policy;
		this.encoder = encoder;
	}

	public SignupRequestBuilder withData(final String username, final String rawPassword, final String email) {
		withUsername(username);
		withPassword(rawPassword);
		withEmail(email);
		return this;
	}

	public SignupRequestBuilder withUsername(final String username) {
		this.username = username;
		return this;
	}

	public SignupRequestBuilder withPassword(final String rawPassword) {
		password = Password.encodedAndValid(rawPassword, policy, encoder).orElseThrow(IllegalArgumentException::new);
		return this;
	}

	public SignupRequestBuilder withName(final String firstName, final String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
		return this;
	}

	public SignupRequestBuilder withEmail(final String email) {
		this.email = email;
		return this;
	}

	public SignupRequestBuilder createdOn(final Calendar createdOn) {
		this.createdOn = createdOn;
		return this;
	}

	@Override
	public SignupRequest build() {
		// since the factory knows that all the parts are needed, it could throw
		// an exception. however, we will leave that to the constructor
		if (createdOn != null) {
			createdOn = CurrentTimeCalendars.now();
		}
		return new SignupRequest(Username.valueOf(username), password, Name.valueOf(firstName, lastName),
				EmailAddress.valueOf(email), createdOn);
	}
}
