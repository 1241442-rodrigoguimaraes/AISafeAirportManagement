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
package eapli.alsafe.alsafeusermanagement.repositories;

import java.util.Optional;

import eapli.alsafe.alsafeusermanagement.domain.AlSafeUser;
import eapli.alsafe.alsafeusermanagement.domain.AlSafeUserEmail;
import eapli.framework.domain.repositories.DomainRepository;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;

/**
 *
 * @author Jorge Santos ajs@isep.ipp.pt 02/04/2016
 */
public interface AlSafeUserRepository
        extends DomainRepository<AlSafeUserEmail, AlSafeUser> {

    /**
     * returns the client user whose username is given
     *
     * @param name the username to search for
     *
     * @return the alsafe user
     */
    Optional<AlSafeUser> findByUsername(Username name);

    /**
     * returns the client user with the given email address
     *
     * @param email the user email address
     *
     * @return the alsafe user
     */
    default Optional<AlSafeUser> findByEmail(final AlSafeUserEmail email) {
        return ofIdentity(email);
    }

    Optional<AlSafeUser> findBySystemUser(SystemUser systemUser);

    Iterable<AlSafeUser> findAllActive();
}
