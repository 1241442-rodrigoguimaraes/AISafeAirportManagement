package eapli.alsafe.engineModelMagnement.Domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.io.Serializable;

@Embeddable
public class engineModelName implements ValueObject, Serializable{
    private String name;

    protected engineModelName() {

    }

    public engineModelName(final String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Engine model name cannot be null or empty");
        }
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof engineModelName)) return false;
        final engineModelName that = (engineModelName) o;
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
