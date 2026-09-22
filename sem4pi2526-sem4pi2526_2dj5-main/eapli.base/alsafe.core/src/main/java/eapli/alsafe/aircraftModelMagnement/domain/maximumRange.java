package eapli.alsafe.aircraftModelMagnement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.io.Serializable;

@Embeddable
public class maximumRange implements ValueObject, Serializable  {

    private double range;

    protected maximumRange() {
    }

    public maximumRange(final double rangge) {
        if (rangge <= 0) {
            throw new IllegalArgumentException("Maximum range must be greater than zero");
        }
        this.range = rangge;
    }

    public double range() {
        return range;
    }

    @Override
    public String toString() {
        return String.valueOf(this.range);
    }


}
