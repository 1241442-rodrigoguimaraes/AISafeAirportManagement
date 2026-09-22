package eapli.alsafe.aircraftModelMagnement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.io.Serializable;

@Embeddable
public class aircraftModelName implements ValueObject, Serializable {

    private String name;

    protected aircraftModelName() {
    }

    public aircraftModelName(final String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Aircraft model name cannot be null or empty");
        }
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof aircraftModelName)) return false;
        final aircraftModelName that = (aircraftModelName) o;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
