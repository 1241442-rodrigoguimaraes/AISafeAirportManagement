package eapli.alsafe.engineModelMagnement.Domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class engineModelEfficiency implements ValueObject, Serializable{

    private double efficiency;

    protected engineModelEfficiency() {
    }

    public engineModelEfficiency(final double efficiency){
        if (efficiency <= 0){
            throw new IllegalArgumentException("Efficiency must be greater than zero");
        }
        this.efficiency = efficiency;
    }

    public double efficiency() {
        return efficiency;
    }

    @Override
    public String toString() {
        return String.valueOf(this.efficiency);
    }


}

