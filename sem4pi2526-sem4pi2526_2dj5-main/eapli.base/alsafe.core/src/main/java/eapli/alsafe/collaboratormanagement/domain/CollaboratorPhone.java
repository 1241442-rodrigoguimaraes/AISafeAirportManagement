package eapli.alsafe.collaboratormanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;

@Embeddable
public class CollaboratorPhone implements ValueObject, Comparable<CollaboratorPhone> {
    private String phoneNumber;

    private static final Pattern VALID_PHONE = Pattern.compile("^[+]?[0-9]{9,15}$");

    protected CollaboratorPhone() {
        // for ORM
    }

    public CollaboratorPhone(final String phoneNumber) {
        if (phoneNumber == null || !VALID_PHONE.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("Invalid phone number");
        }
        this.phoneNumber = phoneNumber;
    }

    public static CollaboratorPhone valueOf(final String phoneNumber) {
        return new CollaboratorPhone(phoneNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollaboratorPhone that = (CollaboratorPhone) o;
        return Objects.equals(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber);
    }

    @Override
    public String toString() {
        return phoneNumber;
    }

    @Override
    public int compareTo(CollaboratorPhone o) {
        return phoneNumber.compareTo(o.phoneNumber);
    }
}
