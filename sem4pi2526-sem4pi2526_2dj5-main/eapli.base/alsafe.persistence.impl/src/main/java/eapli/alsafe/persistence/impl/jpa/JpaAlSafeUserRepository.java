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
package eapli.alsafe.persistence.impl.jpa;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import eapli.alsafe.Application;
import eapli.alsafe.alsafeusermanagement.domain.AlSafeUser;
import eapli.alsafe.alsafeusermanagement.domain.AlSafeUserEmail;
import eapli.alsafe.alsafeusermanagement.repositories.AlSafeUserRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

/**
 *
 * @author Jorge Santos ajs@isep.ipp.pt 02/04/2016
 */
class JpaAlSafeUserRepository
        extends JpaAutoTxRepository<AlSafeUser, AlSafeUserEmail, AlSafeUserEmail>
        implements AlSafeUserRepository {

    public JpaAlSafeUserRepository(final TransactionalContext autoTx) {
        super(autoTx, "alSafeEmailAddress");
    }

    public JpaAlSafeUserRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(),
                "alSafeEmailAddress");
    }

    @Override
    public Optional<AlSafeUser> findByUsername(final Username name) {
        final Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        return matchOne("e.systemUser.username=:name", params);
    }

    @Override
    public Optional<AlSafeUser> findByEmail(final AlSafeUserEmail email) {
        final Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        return matchOne("e.alsafeuseremail=:email", params);
    }

    @Override
    public Optional<AlSafeUser> findBySystemUser(final SystemUser systemUser) {
        final Map<String, Object> params = new HashMap<>();
        params.put("systemUser", systemUser);
        return matchOne("e.systemUser=:systemUser", params);
    }

    @Override
    public Iterable<AlSafeUser> findAllActive() {
        return match("e.systemUser.active = true");
    }
}
