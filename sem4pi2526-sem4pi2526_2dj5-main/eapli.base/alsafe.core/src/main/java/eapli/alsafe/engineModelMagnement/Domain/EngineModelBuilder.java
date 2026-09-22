package eapli.alsafe.engineModelMagnement.Domain;

import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.framework.domain.model.DomainFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EngineModelBuilder implements DomainFactory<engineModel> {

    private static final Logger LOGGER = LogManager.getLogger(EngineModelBuilder.class);

    private engineModelName name;
    private Maker maker;
    private engineType type;
    private engineModelPower power;
    private engineModelFuel fuel;
    private engineModelEfficiency efficiency;

    public EngineModelBuilder() {}

    public EngineModelBuilder with(final String modelName,
                                   final Maker maker,
                                   final engineType type,
                                   final double power,
                                   final engineModelFuel fuel,
                                   final double efficiency) {
        withName(modelName);
        withMaker(maker);
        withType(type);
        withPower(power);
        withFuel(fuel);
        withEfficiency(efficiency);

        return this;
    }

    public EngineModelBuilder with(final engineModelName name,
                                   final Maker maker,
                                   final engineType type,
                                   final engineModelPower power,
                                   final engineModelFuel fuel,
                                   final engineModelEfficiency efficiency) {
        withName(name);
        withMaker(maker);
        withType(type);
        withPower(power);
        withFuel(fuel);
        withEfficiency(efficiency);

        return this;
    }

    public EngineModelBuilder withName(final String modelName) {
        this.name = new engineModelName(modelName);
        return this;
    }

    public EngineModelBuilder withName(final engineModelName name) {
        this.name = name;
        return this;
    }

    public EngineModelBuilder withMaker(final Maker maker) {
        this.maker = maker;
        return this;
    }

    public EngineModelBuilder withType(final engineType type) {
        this.type = type;
        return this;
    }

    public EngineModelBuilder withPower(final double power) {
        this.power = new engineModelPower(power);
        return this;
    }

    public EngineModelBuilder withPower(final engineModelPower power) {
        this.power = power;
        return this;
    }

    public EngineModelBuilder withFuel(final engineModelFuel fuel) {
        this.fuel = fuel;
        return this;
    }

    public EngineModelBuilder withEfficiency(final double efficiency) {
        this.efficiency = new engineModelEfficiency(efficiency);
        return this;
    }

    public EngineModelBuilder withEfficiency(final engineModelEfficiency efficiency) {
        this.efficiency = efficiency;
        return this;
    }

    @Override
    public engineModel build() {
        final var engineModel = new engineModel(name, maker, type, power, fuel, efficiency);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Building Engine Model : [{}]", engineModel);
        }

        return engineModel;
    }
}
