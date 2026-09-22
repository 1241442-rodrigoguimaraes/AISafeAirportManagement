package domain.alsafeusermanagement;

import eapli.alsafe.alsafeusermanagement.domain.SkillsAssessment;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

public class SkillsAssessmentTest {

    @Test
    public void ensureSkillsAssessmentIsCreatedWithFutureDate() {
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.YEAR, 1);
        SkillsAssessment subject = SkillsAssessment.valueOf(futureDate);
        assertNotNull(subject);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureSkillsAssessmentCannotBeNull() {
        SkillsAssessment.valueOf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureSkillsAssessmentCannotBeInThePast() {
        Calendar pastDate = Calendar.getInstance();
        pastDate.add(Calendar.YEAR, -1);
        SkillsAssessment.valueOf(pastDate);
    }

    @Test
    public void ensureEqualsPassesForTheSameIdentity() {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.YEAR, 1);
        SkillsAssessment a = SkillsAssessment.valueOf(date);
        SkillsAssessment b = SkillsAssessment.valueOf(date);

        assertEquals(a, b);
    }

    @Test
    public void ensureEqualsFailsForDifferentIdentity() {
        Calendar date1 = Calendar.getInstance();
        date1.add(Calendar.YEAR, 1);
        Calendar date2 = Calendar.getInstance();
        date2.add(Calendar.YEAR, 2);
        SkillsAssessment a = SkillsAssessment.valueOf(date1);
        SkillsAssessment b = SkillsAssessment.valueOf(date2);

        assertNotEquals(a, b);
    }

    @Test
    public void ensureHashCodeIsTheSameForTheSameIdentity() {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.YEAR, 1);
        SkillsAssessment a = SkillsAssessment.valueOf(date);
        SkillsAssessment b = SkillsAssessment.valueOf(date);

        assertEquals(a.hashCode(), b.hashCode());
    }
}
