package eapli.alsafe.alsafeusermanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serial;
import java.util.Calendar;

@Getter
@Embeddable
public final class SecurityClearance implements ValueObject {

    @Serial
    private final static long serialVersionUID = 1L;

    private Calendar securityClearance;

    public SecurityClearance(final Calendar securityClearance) {
        if (securityClearance == null) throw new IllegalArgumentException();
        if (securityClearance.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) throw new IllegalArgumentException();

        this.securityClearance = securityClearance;
    }

    protected SecurityClearance() {
        // for ORM
    }

    public static SecurityClearance valueOf(final Calendar securityClearance) {
        return new SecurityClearance(securityClearance);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SecurityClearance that)) {
            return false;
        }

        return this.securityClearance.equals(that.securityClearance);
    }

    @Override
    public int hashCode() {
        return this.securityClearance.hashCode();
    }

    @Override
    public String toString() {
        return this.securityClearance.toString();
    }
}
