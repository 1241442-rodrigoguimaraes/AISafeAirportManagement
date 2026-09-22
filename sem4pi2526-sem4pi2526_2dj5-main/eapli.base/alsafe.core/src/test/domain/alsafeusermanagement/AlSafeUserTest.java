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
package domain.alsafeusermanagement;

import static org.junit.Assert.*;

import java.util.Calendar;

import eapli.alsafe.alsafeusermanagement.domain.AlSafeUser;
import eapli.alsafe.alsafeusermanagement.domain.AlSafeUserBuilder;
import eapli.alsafe.alsafeusermanagement.domain.SecurityClearance;
import eapli.alsafe.alsafeusermanagement.domain.SkillsAssessment;
import org.junit.Test;

import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;

/**
 * Created by Nuno Bettencourt [NMB] on 03/04/16.
 * Modified by Junie on 01/05/26.
 */
public class AlSafeUserTest {

    private final String email = "a@alsafe.com";
    private final String anotherEmail = "b@alsafe.com";

    public static SystemUser dummyUser(final String username, final String email, final Role... roles) {
        final SystemUserBuilder userBuilder = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder());
        return userBuilder.with(username, "dummy1", "dummy", "dummy", email).withRoles(roles).build();
    }

    private SystemUser getNewDummyUser() {
        return dummyUser("dummy", email, Roles.ADMIN);
    }

    private SystemUser getAnotherDummyUser() {
        return dummyUser("another", anotherEmail, Roles.ADMIN);
    }

    private AlSafeUser createAlSafeUser(SystemUser systemUser) {
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.YEAR, 10);
        return new AlSafeUserBuilder().with(systemUser, "912345678", futureDate).build();
    }

    private AlSafeUser createAlSafeUserWithDifferentNonIdentityFields(SystemUser systemUser) {
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.YEAR, 11); // different year
        return new AlSafeUserBuilder().with(systemUser, "999999999", futureDate).build();
    }

    @Test
    public void ensureAlSafeUserWithSameIdentityAreEqual() throws Exception {
        final SystemUser user = getNewDummyUser();
        final AlSafeUser aClientUser = createAlSafeUser(user);
        final AlSafeUser anotherClientUser = createAlSafeUserWithDifferentNonIdentityFields(user);

        assertEquals(aClientUser, anotherClientUser);
    }

    @Test
    public void ensureAlSafeUserHashCodeEqualsPassesForTheSameIdentity() throws Exception {
        final SystemUser user = getNewDummyUser();
        final AlSafeUser aClientUser = createAlSafeUser(user);
        final AlSafeUser anotherClientUser = createAlSafeUserWithDifferentNonIdentityFields(user);

        assertEquals(aClientUser.hashCode(), anotherClientUser.hashCode());
    }

    @Test
    public void ensureClientUserIdentityEqualsFailsForDifferentSystemUser() throws Exception {
        final AlSafeUser aClientUser = createAlSafeUser(getNewDummyUser());
        final AlSafeUser anotherClientUser = createAlSafeUser(getAnotherDummyUser());

        assertNotEquals(aClientUser, anotherClientUser);
    }

    @Test
    public void ensureClientUserEqualsAreTheSameForTheSameInstance() throws Exception {
        final AlSafeUser aClientUser = createAlSafeUser(getNewDummyUser());

        assertEquals(aClientUser, aClientUser);
    }

    @Test
    public void ensureClientUserEqualsFailsForDifferentObjectTypes() throws Exception {
        final AlSafeUser aClientUser = createAlSafeUser(getNewDummyUser());

        assertNotEquals(aClientUser, getNewDummyUser());
    }

    @Test
    public void ensureClientUserIsTheSameAsItsInstance() throws Exception {
        final AlSafeUser aClientUser = createAlSafeUser(getNewDummyUser());

        assertTrue(aClientUser.sameAs(aClientUser));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAlSafeUserCannotHaveNullSystemUser() {
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.YEAR, 10);
        SecurityClearance sc = SecurityClearance.valueOf(futureDate);
        SkillsAssessment sa = SkillsAssessment.valueOf(futureDate);
        new AlSafeUser(null, "912345678", sc, sa);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAlSafeUserCannotHaveNullPhoneNumber() {
        SystemUser user = getNewDummyUser();
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.YEAR, 10);
        SecurityClearance sc = SecurityClearance.valueOf(futureDate);
        SkillsAssessment sa = SkillsAssessment.valueOf(futureDate);
        new AlSafeUser(user, null, sc, sa);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAlSafeUserCannotHaveNullSecurityClearance() {
        SystemUser user = getNewDummyUser();
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.YEAR, 10);
        SkillsAssessment sa = SkillsAssessment.valueOf(futureDate);
        new AlSafeUser(user, "912345678", null, sa);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAlSafeUserCannotHaveNullSkillsAssessment() {
        SystemUser user = getNewDummyUser();
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.YEAR, 10);
        SecurityClearance sc = SecurityClearance.valueOf(futureDate);
        new AlSafeUser(user, "912345678", sc, null);
    }
}
