package eapli.alsafe.aircraft.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RegistrationID implements Serializable, Comparable<RegistrationID> {

    @Column(name = "registration_code", length = 32, nullable = false)
    private String value;

    protected RegistrationID() {
        // ORM
    }

    public RegistrationID(final String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Registration ID cannot be null or blank");
        }
        final String trimmed = raw.trim().toUpperCase();
        if (trimmed.length() < 3 || trimmed.length() > 32) {
            throw new IllegalArgumentException("Registration ID length must be between 3 and 32 characters");
        }
        if (!trimmed.matches("[A-Z]{2}-[A-Z][A-Z0-9]+")) {
            throw new IllegalArgumentException("Registration ID format is invalid. Expected format: XX-X... (e.g., CS-TNV)");
        }
        this.value = trimmed;
    }

    public static RegistrationID valueOf(final String raw) {
        return new RegistrationID(raw);
    }

    public String value() {
        return value;
    }

    @Override
    public int compareTo(final RegistrationID o) {
        return this.value.compareTo(o.value);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RegistrationID that = (RegistrationID) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
