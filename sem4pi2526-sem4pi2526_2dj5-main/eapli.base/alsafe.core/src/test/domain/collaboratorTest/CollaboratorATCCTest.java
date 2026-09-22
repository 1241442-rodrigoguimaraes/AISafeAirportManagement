package domain.collaboratorTest;

import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.IATACompanyCode;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.framework.infrastructure.authz.domain.model.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class CollaboratorATCCTest {

    private SystemUser dummyUser() {
        final SystemUserBuilder userBuilder = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder());
        return userBuilder.with("username", "password", "firstName", "lastName", "email@email.com").build();
    }

    private AirTransportCompany dummyATCC() {
        return new AirTransportCompany(new IATACompanyCode("TE"), new ICAOCompanyCode("TET"), "Test Company");
    }

    @Test
    public void ensureCollaboratorATCCIsCreated() {
        CollaboratorEmail email = CollaboratorEmail.valueOf("test@example.com");
        CollaboratorPhone phone = CollaboratorPhone.valueOf("912345678");
        SystemUser user = dummyUser();
        AirTransportCompany company = dummyATCC();

        CollaboratorATCC collaboratorATCC = new CollaboratorATCC("John Doe", email, phone, user, company);
        assertNotNull(collaboratorATCC);
        assertEquals("John Doe", collaboratorATCC.name());
        assertEquals("test@example.com", collaboratorATCC.email().toString());
        assertEquals("912345678", collaboratorATCC.phoneNumber());
        assertEquals(user, collaboratorATCC.user());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCollaboratorATCCCannotBeCreatedWithNullName() {
        new CollaboratorATCC(null, CollaboratorEmail.valueOf("test@example.com"),
                CollaboratorPhone.valueOf("912345678"), dummyUser(), dummyATCC());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCollaboratorATCCCannotBeCreatedWithNullEmail() {
        new CollaboratorATCC("John Doe", null,
                CollaboratorPhone.valueOf("912345678"), dummyUser(), dummyATCC());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCollaboratorATCCCannotBeCreatedWithNullPhone() {
        new CollaboratorATCC("John Doe", CollaboratorEmail.valueOf("test@example.com"),
                null, dummyUser(), dummyATCC());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCollaboratorATCCCannotBeCreatedWithNullUser() {
        new CollaboratorATCC("John Doe", CollaboratorEmail.valueOf("test@example.com"),
                CollaboratorPhone.valueOf("912345678"), null, dummyATCC());
    }

    @Test
    public void ensureChangeEmailAndPhoneUpdatesContactDetails() {
        CollaboratorATCC collaboratorATCC = new CollaboratorATCC("John Doe",
                CollaboratorEmail.valueOf("old@example.com"),
                CollaboratorPhone.valueOf("912345678"),
                dummyUser(), dummyATCC());

        collaboratorATCC.changeEmailAndPhone(
                CollaboratorEmail.valueOf("new@example.com"),
                CollaboratorPhone.valueOf("923456789"));

        assertEquals("new@example.com", collaboratorATCC.email().toString());
        assertEquals("923456789", collaboratorATCC.phoneNumber());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureChangeEmailAndPhoneRejectsNullEmail() {
        CollaboratorATCC collaboratorATCC = new CollaboratorATCC("John Doe",
                CollaboratorEmail.valueOf("test@example.com"),
                CollaboratorPhone.valueOf("912345678"),
                dummyUser(), dummyATCC());
        collaboratorATCC.changeEmailAndPhone(null, CollaboratorPhone.valueOf("923456789"));
    }

    @Test
    public void ensureEqualityBasedOnEmail() {
        CollaboratorEmail email = CollaboratorEmail.valueOf("test@example.com");
        CollaboratorPhone phone = CollaboratorPhone.valueOf("912345678");
        SystemUser user = dummyUser();

        CollaboratorATCC c1 = new CollaboratorATCC("John", email, phone, user, dummyATCC());
        CollaboratorATCC c2 = new CollaboratorATCC("John Doe", email, phone, user, dummyATCC());

        assertTrue(c1.sameAs(c2));
    }
}
