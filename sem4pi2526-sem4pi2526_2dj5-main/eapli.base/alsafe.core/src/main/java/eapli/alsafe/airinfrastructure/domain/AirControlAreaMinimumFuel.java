package eapli.alsafe.airinfrastructure.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;

@Embeddable
@EqualsAndHashCode
public class AirControlAreaMinimumFuel implements ValueObject {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private final double minimumFuel;

    public AirControlAreaMinimumFuel(final double minimumFuel) {
        Preconditions.nonNegative((long) minimumFuel, "Minimum fuel cannot be negative.");

        this.minimumFuel = minimumFuel;
    }

    protected AirControlAreaMinimumFuel() {
        // for ORM;
        minimumFuel = 0;
    }

    @Override
    public String toString() {
        return Double.toString(minimumFuel);
    }
}
