package eapli.alsafe.infrastructure.bootstrappers;

import eapli.alsafe.aircraftModelMagnement.domain.*;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
import eapli.alsafe.engineModelMagnement.Domain.engineModel;
import eapli.alsafe.engineModelMagnement.Domain.engineModelId;
import eapli.alsafe.engineModelMagnement.Domain.engineModelName;
import eapli.alsafe.engineModelMagnement.Domain.engineType;
import eapli.alsafe.engineModelMagnement.Repositories.engineModelRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.domain.MakerName;
import eapli.alsafe.aircraftModelMagnement.repositories.MakersRepository;

import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class AircraftModelBootstrapper implements Action {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AircraftModelBootstrapper.class);

    private final aircraftModelRepository aircraftModelRepository =
            PersistenceContext.repositories().aircraftModels();

    private final MakersRepository makerRepository =
            PersistenceContext.repositories().makers();

    private final engineModelRepository engineModelRepository =
            PersistenceContext.repositories().engineModels();

    private final AircraftModelBuilder aircraftModelBuilder = new AircraftModelBuilder();

    @Override
    public boolean execute() {

        registerAircraftModel(
                "BOEING 777-ER",
                "Rodrigos",
                aircraftModelType.PASSENGER,
                engineType.TURBOFAN,
                6150,
                "CFM56",
                "Pedrie"
        );

        return true;
    }

    private void registerAircraftModel(final String modelName,
                                       final String makerName,
                                       final aircraftModelType type,
                                       final engineType motorization,
                                       final double maximumRangeKm,
                                       final String engineName,
                                       final String engineMakerName) {

        final MakerName makerId = new MakerName(makerName);
        final Optional<Maker> maker = makerRepository.ofIdentity(makerId);

        if (maker.isEmpty()) {
            LOGGER.warn("Maker {} does not exist. Aircraft model {} was not registered.",
                    makerName, modelName);
            return;
        }

        final engineModelId engineId = new engineModelId(
                new engineModelName(engineName),
                new MakerName(engineMakerName)
        );

        final Optional<engineModel> engine = engineModelRepository.ofIdentity(engineId);

        if (engine.isEmpty()) {
            LOGGER.warn("Engine model {} by {} does not exist. Aircraft model {} was not registered.",
                    engineName, engineMakerName, modelName);
            return;
        }

        final aircraftModelName aircraftName = new aircraftModelName(modelName);
        final aircraftModelId aircraftId = new aircraftModelId(
                makerId, aircraftName
        );

        if (aircraftModelRepository.ofIdentity(aircraftId).isPresent()) {
            LOGGER.info("Aircraft model {} by {} already exists.", modelName, makerName);
            return;
        }

        final Set<engineModel> certifiedEngines = new HashSet<>();
        certifiedEngines.add(engine.get());

        final aircraftModelPhysicsData physicsData =
                new aircraftModelPhysicsData(
                        42600,
                        78000,
                        62500,
                        24210,
                        12100,
                        840,
                        122.6,
                        0.024,
                        0.50
                );


        final aircraftModel aircraftModel = aircraftModelBuilder
                .with(aircraftName, maker.get(), type, motorization, new maximumRange(maximumRangeKm), physicsData, certifiedEngines)
                .build();

        aircraftModelRepository.save(aircraftModel);

        LOGGER.info("Registered aircraft model {}", aircraftModel);
    }
}
