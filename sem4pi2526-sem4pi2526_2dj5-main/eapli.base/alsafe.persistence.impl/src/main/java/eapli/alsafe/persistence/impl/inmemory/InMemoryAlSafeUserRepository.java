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
package eapli.alsafe.persistence.impl.inmemory;

import java.util.Optional;

import eapli.alsafe.alsafeusermanagement.domain.AlSafeUser;
import eapli.alsafe.alsafeusermanagement.domain.AlSafeUserEmail;
import eapli.alsafe.alsafeusermanagement.repositories.AlSafeUserRepository;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

/**
 *
 * @author Jorge Santos ajs@isep.ipp.pt 02/04/2016
 */
public class InMemoryAlSafeUserRepository extends InMemoryDomainRepository<AlSafeUser, AlSafeUserEmail>
		implements AlSafeUserRepository {

	static {
		InMemoryInitializer.init();
	}

	@Override
	public Optional<AlSafeUser> findByUsername(final Username name) {
		return matchOne(e -> e.user().username().equals(name));
	}

	@Override
	public Optional<AlSafeUser> findByEmail(final AlSafeUserEmail email) {
		return Optional.ofNullable(data().get(email));
	}

	@Override
	public Optional<AlSafeUser> findBySystemUser(final SystemUser systemUser) {
		return matchOne(e -> e.user().equals(systemUser));
	}

	@Override
	public Iterable<AlSafeUser> findAllActive() {
		return match(e -> e.user().isActive());
	}
}
