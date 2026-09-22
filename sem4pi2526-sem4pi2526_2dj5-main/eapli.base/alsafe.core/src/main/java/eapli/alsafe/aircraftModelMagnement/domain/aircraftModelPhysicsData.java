package eapli.alsafe.aircraftModelMagnement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class aircraftModelPhysicsData implements ValueObject, Serializable {

    private double emptyWeight;

    private double maximumTakeOffWeight;

    private double maximumZeroFuelWeight;

    private double maximumFuelCapacity;

    private double serviceCeiling;

    private double cruiseSpeed;

    private double wingArea;

    private double dragCoefficient;

    private double liftCoefficient;


    protected aircraftModelPhysicsData() {
    }

    public aircraftModelPhysicsData(final double emptyWeight,
                                    final double maximumTakeOffWeight,
                                    final double maximumZeroFuelWeight,
                                    final double maximumFuelCapacity,
                                    final double serviceCeiling,
                                    final double cruiseSpeed,
                                    final double wingArea,
                                    final double dragCoefficient,
                                    final double liftCoefficient) {

        validate(emptyWeight,
                maximumTakeOffWeight,
                maximumZeroFuelWeight,
                maximumFuelCapacity,
                serviceCeiling,
                cruiseSpeed,
                wingArea,
                dragCoefficient,
                liftCoefficient);

        this.emptyWeight = emptyWeight;
        this.maximumTakeOffWeight = maximumTakeOffWeight;
        this.maximumZeroFuelWeight = maximumZeroFuelWeight;
        this.maximumFuelCapacity = maximumFuelCapacity;
        this.serviceCeiling = serviceCeiling;
        this.cruiseSpeed = cruiseSpeed;
        this.wingArea = wingArea;
        this.dragCoefficient = dragCoefficient;
        this.liftCoefficient = liftCoefficient;
    }

    private void validate(final double emptyWeight,
                          final double maximumTakeOffWeight,
                          final double maximumZeroFuelWeight,
                          final double maximumFuelCapacity,
                          final double serviceCeiling,
                          final double cruiseSpeed,
                          final double wingArea,
                          final double dragCoefficient,
                          final double liftCoefficient) {

        if (emptyWeight <= 0) {
            throw new IllegalArgumentException("Empty weight must be positive");
        }

        if (maximumTakeOffWeight <= 0) {
            throw new IllegalArgumentException("Maximum takeoff weight must be positive");
        }

        if (maximumZeroFuelWeight <= 0) {
            throw new IllegalArgumentException("Maximum zero fuel weight must be positive");
        }

        if (maximumFuelCapacity <= 0) {
            throw new IllegalArgumentException("Maximum fuel capacity must be positive");
        }

        if (serviceCeiling <= 0) {
            throw new IllegalArgumentException("Service ceiling must be positive");
        }

        if (cruiseSpeed <= 0) {
            throw new IllegalArgumentException("Cruise speed must be positive");
        }

        if (wingArea <= 0) {
            throw new IllegalArgumentException("Wing area must be positive");
        }

        if (dragCoefficient <= 0) {
            throw new IllegalArgumentException("Drag coefficient must be positive");
        }

        if (liftCoefficient <= 0) {
            throw new IllegalArgumentException("Lift coefficient must be positive");
        }

        if (maximumTakeOffWeight < emptyWeight) {
            throw new IllegalArgumentException(
                    "MTOW cannot be lower than empty weight");
        }

        if (maximumZeroFuelWeight < emptyWeight) {
            throw new IllegalArgumentException(
                    "MZFW cannot be lower than empty weight");
        }

        if (maximumTakeOffWeight < maximumZeroFuelWeight) {
            throw new IllegalArgumentException(
                    "MTOW cannot be lower than MZFW");
        }
    }

    public double emptyWeight() {
        return emptyWeight;
    }

    public double maximumTakeOffWeight() {
        return maximumTakeOffWeight;
    }

    public double maximumZeroFuelWeight() {
        return maximumZeroFuelWeight;
    }

    public double maximumFuelCapacity() {
        return maximumFuelCapacity;
    }

    public double serviceCeiling() {
        return serviceCeiling;
    }

    public double cruiseSpeed() {
        return cruiseSpeed;
    }

    public double wingArea() {
        return wingArea;
    }

    public double dragCoefficient() {
        return dragCoefficient;
    }

    public double liftCoefficient() {
        return liftCoefficient;
    }

    @Override
    public String toString() {
        return "PhysicsData{" +
                "emptyWeight=" + emptyWeight +
                ", MTOW=" + maximumTakeOffWeight +
                ", MZFW=" + maximumZeroFuelWeight +
                ", fuelCapacity=" + maximumFuelCapacity +
                ", serviceCeiling=" + serviceCeiling +
                ", cruiseSpeed=" + cruiseSpeed +
                ", wingArea=" + wingArea +
                ", dragCoefficient=" + dragCoefficient +
                ", liftCoefficient=" + liftCoefficient +
                '}';
    }
}
