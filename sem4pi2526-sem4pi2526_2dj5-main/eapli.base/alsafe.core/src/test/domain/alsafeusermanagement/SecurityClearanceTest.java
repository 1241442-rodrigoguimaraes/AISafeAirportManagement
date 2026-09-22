package domain.alsafeusermanagement;

import eapli.alsafe.alsafeusermanagement.domain.SecurityClearance;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

public class SecurityClearanceTest {

    @Test
    public void ensureSecurityClearanceIsCreatedWithFutureDate() {
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.YEAR, 1);
        SecurityClearance subject = SecurityClearance.valueOf(futureDate);
        assertNotNull(subject);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureSecurityClearanceCannotBeNull() {
        SecurityClearance.valueOf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureSecurityClearanceCannotBeInThePast() {
        Calendar pastDate = Calendar.getInstance();
        pastDate.add(Calendar.YEAR, -1);
        SecurityClearance.valueOf(pastDate);
    }

    @Test
    public void ensureEqualsPassesForTheSameIdentity() {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.YEAR, 1);
        SecurityClearance a = SecurityClearance.valueOf(date);
        SecurityClearance b = SecurityClearance.valueOf(date);

        assertEquals(a, b);
    }

    @Test
    public void ensureEqualsFailsForDifferentIdentity() {
        Calendar date1 = Calendar.getInstance();
        date1.add(Calendar.YEAR, 1);
        Calendar date2 = Calendar.getInstance();
        date2.add(Calendar.YEAR, 2);
        SecurityClearance a = SecurityClearance.valueOf(date1);
        SecurityClearance b = SecurityClearance.valueOf(date2);

        assertNotEquals(a, b);
    }

    @Test
    public void ensureHashCodeIsTheSameForTheSameIdentity() {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.YEAR, 1);
        SecurityClearance a = SecurityClearance.valueOf(date);
        SecurityClearance b = SecurityClearance.valueOf(date);

        assertEquals(a.hashCode(), b.hashCode());
    }
}
