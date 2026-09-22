package domain.alsafeusermanagement;

import eapli.alsafe.alsafeusermanagement.domain.AlSafeUserEmail;
import eapli.framework.general.domain.model.EmailAddress;
import org.junit.Test;

import static org.junit.Assert.*;

public class AlSafeUserEmailTest {

    @Test
    public void ensureAlSafeUserEmailCanBeCreated() {
        String email = "test@alsafe.com";
        AlSafeUserEmail alSafeUserEmail = new AlSafeUserEmail(email);
        assertEquals(email, alSafeUserEmail.toString());
    }

    @Test
    public void ensureAlSafeUserEmailValueOfWorks() {
        String email = "test@alsafe.com";
        AlSafeUserEmail alSafeUserEmail = AlSafeUserEmail.valueOf(email);
        assertEquals(email, alSafeUserEmail.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAlSafeUserEmailValueOfFailsWithEmpty() {
        AlSafeUserEmail.valueOf("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAlSafeUserEmailValueOfFailsWithNull() {
        AlSafeUserEmail.valueOf(null);
    }

    @Test
    public void ensureAlSafeUserEmailFromSystemUserEmailWorks() {
        String email = "test@alsafe.com";
        EmailAddress emailAddress = EmailAddress.valueOf(email);
        AlSafeUserEmail alSafeUserEmail = AlSafeUserEmail.fromSystemUserEmail(emailAddress);
        assertEquals(email, alSafeUserEmail.toString());
    }

    @Test
    public void ensureEqualsAndHashCodeWork() {
        AlSafeUserEmail email1 = new AlSafeUserEmail("test@alsafe.com");
        AlSafeUserEmail email2 = new AlSafeUserEmail("test@alsafe.com");
        AlSafeUserEmail email3 = new AlSafeUserEmail("other@alsafe.com");

        assertEquals(email1, email2);
        assertNotEquals(email1, email3);
        assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    public void ensureCompareToWorks() {
        AlSafeUserEmail email1 = new AlSafeUserEmail("a@alsafe.com");
        AlSafeUserEmail email2 = new AlSafeUserEmail("b@alsafe.com");

        assertTrue(email1.compareTo(email2) < 0);
        assertTrue(email2.compareTo(email1) > 0);
        assertEquals(0, email1.compareTo(new AlSafeUserEmail("a@alsafe.com")));
    }
}
