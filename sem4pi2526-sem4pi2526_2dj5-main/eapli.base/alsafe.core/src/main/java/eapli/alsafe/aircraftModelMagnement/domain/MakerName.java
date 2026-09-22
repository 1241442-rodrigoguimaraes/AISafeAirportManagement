package eapli.alsafe.aircraftModelMagnement.domain;


import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class MakerName implements ValueObject, Comparable<MakerName> {

    private String name;

    protected MakerName() {

    }

    public MakerName(final String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Maker name cannot be null or empty");
        }
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MakerName that = (MakerName) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(MakerName o) {
        return name.compareTo(o.name);
    }
}