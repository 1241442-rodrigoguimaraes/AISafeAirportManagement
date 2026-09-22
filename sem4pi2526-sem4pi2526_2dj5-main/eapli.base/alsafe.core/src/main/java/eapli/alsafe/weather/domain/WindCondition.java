package eapli.alsafe.weather.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
public class WindCondition implements ValueObject, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int direction;
    private final double speed;

    public WindCondition(final int direction, final double speed) {
        Preconditions.ensure(direction >= 0 && direction <= 360, "Wind direction must be between 0 and 360 degrees.");
        Preconditions.ensure(speed >= 0, "Wind speed must be a non-negative value.");

        this.direction = direction;
        this.speed = speed;
    }

    protected WindCondition() {
        // for ORM
        this.direction = 0;
        this.speed = 0;
    }

    @Override
    public String toString() {
        return String.format("Direction: %d°, Speed: %.2f m/s", direction, speed);
    }
}
