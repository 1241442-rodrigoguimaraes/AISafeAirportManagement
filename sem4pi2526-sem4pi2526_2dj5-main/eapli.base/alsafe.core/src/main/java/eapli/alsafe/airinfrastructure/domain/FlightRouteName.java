package eapli.alsafe.airinfrastructure.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;
import eapli.framework.domain.model.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;

@Embeddable
@EqualsAndHashCode
public class FlightRouteName implements ValueObject, Comparable<FlightRouteName> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private String name;

    protected FlightRouteName() {
        // for ORM
    }

    public FlightRouteName(String name) {
        Preconditions.ensure(name != null && !name.isBlank(), "Flight route name cannot be null or blank");
        Preconditions.ensure(name.matches("^[A-Z]{2}[0-9]{1,4}$"), "Flight route name must be composed by 2 letters and 1 to 4 digits");

        this.name = name;
    }

    @Override
    public int compareTo(FlightRouteName other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
