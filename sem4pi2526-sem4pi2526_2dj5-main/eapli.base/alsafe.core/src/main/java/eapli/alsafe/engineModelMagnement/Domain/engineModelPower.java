package eapli.alsafe.engineModelMagnement.Domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class engineModelPower implements ValueObject, Serializable {

    private double power;

    protected engineModelPower() {
    }

    public engineModelPower(final double power) {
        if (power <= 0) {
            throw new IllegalArgumentException("Engine power must be greater than zero");
        }
        this.power = power;
    }

    public double power () {
        return power;
    }

    @Override
    public String toString() {
        return String.valueOf(this.power);
    }

}
