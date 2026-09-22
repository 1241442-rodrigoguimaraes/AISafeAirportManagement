package eapli.alsafe.flightPlan.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
public final class FuelQuantity {

    public static final double LITERS_PER_KG = 0.721;
    public static final double KG_PER_LBS = 0.4536;
    public static final double LBS_PER_L = 1.589;

    @Column(name = "FUEL_QUANTITY")
    @Getter
    private double fuelQuantity;

    @Column(name = "FUEL_UNIT")
    @Getter
    @Setter
    private FuelUnit fuelUnit;


    public FuelQuantity(double fuelQuantity, FuelUnit fuelUnit) {
        if (fuelQuantity < 0) {
            throw new IllegalArgumentException("Fuel quantity cannot be negative");
        }
        Preconditions.nonNull(fuelUnit, "Fuel unit cannot be null");
        this.fuelQuantity = fuelQuantity;
        this.fuelUnit = fuelUnit;
    }

    protected FuelQuantity() {}

    public static FuelQuantity toLiters (FuelQuantity fuelQuantity) {
        if (fuelQuantity.getFuelUnit() == FuelUnit.KG) {
            return new FuelQuantity(fuelQuantity.getFuelQuantity() / LITERS_PER_KG, FuelUnit.L);
        }else if (fuelQuantity.getFuelUnit() == FuelUnit.LBS) {
            return new FuelQuantity(fuelQuantity.getFuelQuantity() * LBS_PER_L, FuelUnit.L);
        }
        return fuelQuantity;
    }

    public static FuelQuantity toKilograms (FuelQuantity fuelQuantity) {
        if (fuelQuantity.getFuelUnit() == FuelUnit.L) {
            return new FuelQuantity(fuelQuantity.getFuelQuantity() * LITERS_PER_KG, FuelUnit.KG);
        }else if (fuelQuantity.getFuelUnit() == FuelUnit.LBS) {
            return new FuelQuantity(fuelQuantity.getFuelQuantity() * KG_PER_LBS, FuelUnit.KG);
        }
        return fuelQuantity;
    }

    public static FuelQuantity toPounds (FuelQuantity fuelQuantity) {
        if (fuelQuantity.getFuelUnit() == FuelUnit.KG) {
            return new FuelQuantity(fuelQuantity.getFuelQuantity() / KG_PER_LBS, FuelUnit.LBS);
        }else if (fuelQuantity.getFuelUnit() == FuelUnit.L) {
            return new FuelQuantity(fuelQuantity.getFuelQuantity() / LBS_PER_L, FuelUnit.LBS);
        }
        return fuelQuantity;
    }
}
