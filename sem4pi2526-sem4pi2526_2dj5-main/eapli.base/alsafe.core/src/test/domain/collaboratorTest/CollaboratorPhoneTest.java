package domain.collaboratorTest;

import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import org.junit.Test;
import static org.junit.Assert.*;

public class CollaboratorPhoneTest {

    @Test
    public void ensurePhoneIsCreatedWithValidNumber() {
        CollaboratorPhone phone = CollaboratorPhone.valueOf("912345678");
        assertNotNull(phone);
        assertEquals("912345678", phone.toString());
    }

    @Test
    public void ensurePhoneIsCreatedWithValidNumberAndPlus() {
        CollaboratorPhone phone = CollaboratorPhone.valueOf("+351912345678");
        assertNotNull(phone);
        assertEquals("+351912345678", phone.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensurePhoneCannotBeCreatedWithInvalidNumber() {
        CollaboratorPhone.valueOf("123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensurePhoneCannotBeCreatedWithLetters() {
        CollaboratorPhone.valueOf("91234567a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensurePhoneCannotBeCreatedWithNull() {
        CollaboratorPhone.valueOf(null);
    }

    @Test
    public void ensurePhonesWithSameNumberAreEqual() {
        CollaboratorPhone phone1 = CollaboratorPhone.valueOf("912345678");
        CollaboratorPhone phone2 = CollaboratorPhone.valueOf("912345678");
        assertEquals(phone1, phone2);
    }

    @Test
    public void ensurePhonesWithDifferentNumberAreNotEqual() {
        CollaboratorPhone phone1 = CollaboratorPhone.valueOf("912345678");
        CollaboratorPhone phone2 = CollaboratorPhone.valueOf("912345679");
        assertNotEquals(phone1, phone2);
    }
}
