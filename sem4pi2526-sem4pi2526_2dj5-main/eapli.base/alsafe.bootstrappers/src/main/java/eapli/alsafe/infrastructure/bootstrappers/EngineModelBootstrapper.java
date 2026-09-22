package eapli.alsafe.infrastructure.bootstrappers;

import eapli.alsafe.engineModelMagnement.Domain.*;
import eapli.alsafe.engineModelMagnement.Repositories.engineModelRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.domain.MakerName;
import eapli.alsafe.aircraftModelMagnement.repositories.MakersRepository;

import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;


public class EngineModelBootstrapper implements Action {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(EngineModelBootstrapper.class);

    private final engineModelRepository engineModelRepository =
            PersistenceContext.repositories().engineModels();

    private final MakersRepository makerRepository =
            PersistenceContext.repositories().makers();

    private final EngineModelBuilder engineModelBuilder = new EngineModelBuilder();

    @Override
    public boolean execute() {
        registerEngineModel("CFM56", "Pedrie",
                engineType.TURBOFAN, 120000, engineModelFuel.JET_A1, 0.60);

        registerEngineModel("LEAP-1A", "Pedrie",
                engineType.TURBOFAN, 140000, engineModelFuel.JET_A1, 0.65);

        return true;
    }

    private void registerEngineModel(final String modelName,
                                     final String makerName,
                                     final engineType type,
                                     final double power,
                                     final engineModelFuel fuel,
                                     final double efficiency) {

        final MakerName makerId = new MakerName(makerName);
        final Optional<Maker> maker = makerRepository.ofIdentity(makerId);

        if (maker.isEmpty()) {
            LOGGER.warn("Maker {} does not exist. Engine model {} was not registered.",
                    makerName, modelName);
            return;
        }

        final engineModelName name = new engineModelName(modelName);
        final engineModelId id = new engineModelId(name, makerId);

        if (engineModelRepository.ofIdentity(id).isPresent()) {
            LOGGER.info("Engine model {} by {} already exists.", modelName, makerName);
            return;
        }

        final engineModel engineModel = engineModelBuilder
                .with(name, maker.get(), type, new engineModelPower(power), fuel, new engineModelEfficiency(efficiency))
                .build();

        engineModelRepository.save(engineModel);

        LOGGER.info("Registered engine model {}", engineModel);
    }
}
