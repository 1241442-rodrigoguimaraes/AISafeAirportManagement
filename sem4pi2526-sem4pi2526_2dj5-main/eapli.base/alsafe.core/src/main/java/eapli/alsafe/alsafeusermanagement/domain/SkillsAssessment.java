package eapli.alsafe.alsafeusermanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serial;
import java.util.Calendar;
import java.util.Objects;

@Getter
@Embeddable
public final class SkillsAssessment implements ValueObject {

    @Serial
    private static final long serialVersionUID = 1L;

    private Calendar skillsAssessment;

    public SkillsAssessment(final Calendar skillsAssessment) {
        if (skillsAssessment == null) throw new IllegalArgumentException();
        if (skillsAssessment.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) throw new IllegalArgumentException();

        this.skillsAssessment = skillsAssessment;
    }

    protected SkillsAssessment() {
        // for ORM
    }

    public static SkillsAssessment valueOf(final Calendar skillsAssessment) {
        return new SkillsAssessment(skillsAssessment);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SkillsAssessment that = (SkillsAssessment) o;
        return Objects.equals(skillsAssessment, that.skillsAssessment);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(skillsAssessment);
    }

    @Override
    public String toString() {
        return this.skillsAssessment.toString();
    }
}
