package eapli.alsafe.aircraftModelMagnement.domain;

import eapli.alsafe.engineModelMagnement.Domain.engineModel;
import eapli.alsafe.engineModelMagnement.Domain.engineType;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.framework.domain.model.DomainFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class AircraftModelBuilder implements DomainFactory<aircraftModel> {

    private static final Logger LOGGER = LogManager.getLogger(AircraftModelBuilder.class);

    private aircraftModelName name;
    private Maker maker;
    private aircraftModelType type;
    private engineType motorization;
    private maximumRange maxRange;
    private aircraftModelPhysicsData physicsData;
    private Set<engineModel> certifiedEngines;

    public AircraftModelBuilder() {
        this.certifiedEngines = new HashSet<>();
    }

    public AircraftModelBuilder with(final String modelName,
                                     final Maker maker,
                                     final aircraftModelType type,
                                     final engineType motorization,
                                     final double maximumRangeKm,
                                     final double emptyWeight,
                                     final double mtow,
                                     final double mzfw,
                                     final double fuelCapacity,
                                     final double serviceCeiling,
                                     final double cruiseSpeed,
                                     final double wingArea,
                                     final double dragCoefficient,
                                     final double liftCoefficient,
                                     final Set<engineModel> certifiedEngines) {
        withName(modelName);
        withMaker(maker);
        withType(type);
        withMotorization(motorization);
        withMaximumRange(maximumRangeKm);
        withPhysicsData(emptyWeight, mtow, mzfw, fuelCapacity, serviceCeiling, cruiseSpeed, wingArea, dragCoefficient, liftCoefficient);
        withCertifiedEngines(certifiedEngines);

        return this;
    }

    public AircraftModelBuilder with(final aircraftModelName name,
                                     final Maker maker,
                                     final aircraftModelType type,
                                     final engineType motorization,
                                     final maximumRange maxRange,
                                     final aircraftModelPhysicsData physicsData,
                                     final Set<engineModel> certifiedEngines) {
        withName(name);
        withMaker(maker);
        withType(type);
        withMotorization(motorization);
        withMaximumRange(maxRange);
        withPhysicsData(physicsData);
        withCertifiedEngines(certifiedEngines);

        return this;
    }

    public AircraftModelBuilder withName(final String modelName) {
        this.name = new aircraftModelName(modelName);
        return this;
    }

    public AircraftModelBuilder withName(final aircraftModelName name) {
        this.name = name;
        return this;
    }

    public AircraftModelBuilder withMaker(final Maker maker) {
        this.maker = maker;
        return this;
    }

    public AircraftModelBuilder withType(final aircraftModelType type) {
        this.type = type;
        return this;
    }

    public AircraftModelBuilder withMotorization(final engineType motorization) {
        this.motorization = motorization;
        return this;
    }

    public AircraftModelBuilder withMaximumRange(final double maximumRangeKm) {
        this.maxRange = new maximumRange(maximumRangeKm);
        return this;
    }

    public AircraftModelBuilder withMaximumRange(final maximumRange maxRange) {
        this.maxRange = maxRange;
        return this;
    }

    public AircraftModelBuilder withPhysicsData(final double emptyWeight,
                                                final double mtow,
                                                final double mzfw,
                                                final double fuelCapacity,
                                                final double serviceCeiling,
                                                final double cruiseSpeed,
                                                final double wingArea,
                                                final double dragCoefficient,
                                                final double liftCoefficient) {
        this.physicsData = new aircraftModelPhysicsData(emptyWeight, mtow, mzfw, fuelCapacity, serviceCeiling, cruiseSpeed, wingArea, dragCoefficient, liftCoefficient);
        return this;
    }

    public AircraftModelBuilder withPhysicsData(final aircraftModelPhysicsData physicsData) {
        this.physicsData = physicsData;
        return this;
    }

    public AircraftModelBuilder withCertifiedEngines(final Set<engineModel> certifiedEngines) {
        this.certifiedEngines = certifiedEngines == null ? null : new HashSet<>(certifiedEngines);
        return this;
    }

    public AircraftModelBuilder addCertifiedEngine(final engineModel engineModel) {
        if (this.certifiedEngines == null) {
            this.certifiedEngines = new HashSet<>();
        }

        this.certifiedEngines.add(engineModel);
        return this;
    }

    @Override
    public aircraftModel build() {
        final var aircraftModel = new aircraftModel(name, maker, type, motorization, maxRange, physicsData, certifiedEngines);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Building Aircraft Model : [{}]", aircraftModel);
        }

        return aircraftModel;
    }
}
