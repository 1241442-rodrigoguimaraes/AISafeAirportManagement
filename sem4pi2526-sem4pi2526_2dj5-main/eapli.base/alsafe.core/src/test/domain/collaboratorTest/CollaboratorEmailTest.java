package domain.collaboratorTest;

import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import org.junit.Test;
import static org.junit.Assert.*;

public class CollaboratorEmailTest {

    @Test
    public void ensureEmailIsCreatedWithValidAddress() {
        CollaboratorEmail email = CollaboratorEmail.valueOf("test@example.com");
        assertNotNull(email);
        assertEquals("test@example.com", email.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureEmailCannotBeCreatedWithInvalidAddress() {
        CollaboratorEmail.valueOf("invalid-email");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureEmailCannotBeCreatedWithNull() {
        CollaboratorEmail.valueOf(null);
    }

    @Test
    public void ensureEmailsWithSameAddressAreEqual() {
        CollaboratorEmail email1 = CollaboratorEmail.valueOf("test@example.com");
        CollaboratorEmail email2 = CollaboratorEmail.valueOf("test@example.com");
        assertEquals(email1, email2);
    }

    @Test
    public void ensureEmailsWithDifferentAddressAreNotEqual() {
        CollaboratorEmail email1 = CollaboratorEmail.valueOf("test1@example.com");
        CollaboratorEmail email2 = CollaboratorEmail.valueOf("test2@example.com");
        assertNotEquals(email1, email2);
    }
}
