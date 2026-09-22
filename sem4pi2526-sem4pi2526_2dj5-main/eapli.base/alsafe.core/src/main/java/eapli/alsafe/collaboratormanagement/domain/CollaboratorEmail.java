package eapli.alsafe.collaboratormanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.general.domain.model.EmailAddress;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class CollaboratorEmail implements ValueObject, Comparable<CollaboratorEmail> {
    private EmailAddress email;

    protected CollaboratorEmail() {
        // for ORM
    }

    public CollaboratorEmail(final String email) {
        this.email = EmailAddress.valueOf(email);
    }

    public static CollaboratorEmail valueOf(final String email) {
        return new CollaboratorEmail(email);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollaboratorEmail that = (CollaboratorEmail) o;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return email.toString();
    }

    @Override
    public int compareTo(CollaboratorEmail o) {
        return email.compareTo(o.email);
    }
}
