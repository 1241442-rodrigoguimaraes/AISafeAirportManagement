package eapli.alsafe.aircraftModelMagnement.application;

import eapli.alsafe.aircraftModelMagnement.domain.*;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
import eapli.alsafe.engineModelMagnement.Domain.engineModel;
import eapli.alsafe.engineModelMagnement.Domain.engineType;
import eapli.alsafe.engineModelMagnement.Repositories.engineModelRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.repositories.MakersRepository;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@UseCaseController
public class CreateAircraftModelController {

    private final AuthorizationService authz;

    private final aircraftModelRepository aircraftModelRepository;

    private final MakersRepository makerRepository;

    private final engineModelRepository engineRepository;

    private final AircraftModelBuilder aircraftModelBuilder;

    public CreateAircraftModelController() {
        this(
                AuthzRegistry.authorizationService(),
                new AircraftModelBuilder(),
                PersistenceContext.repositories().aircraftModels(),
                PersistenceContext.repositories().makers(),
                PersistenceContext.repositories().engineModels()
        );
    }

    public CreateAircraftModelController(
            final AuthorizationService authz,
            final aircraftModelRepository aircraftModelRepository,
            final MakersRepository makerRepository,
            final engineModelRepository engineRepository) {
        this(authz, new AircraftModelBuilder(), aircraftModelRepository, makerRepository, engineRepository);
    }

    public CreateAircraftModelController(
            final AuthorizationService authz,
            final AircraftModelBuilder aircraftModelBuilder,
            final aircraftModelRepository aircraftModelRepository,
            final MakersRepository makerRepository,
            final engineModelRepository engineRepository) {

        if (authz == null || aircraftModelBuilder == null || aircraftModelRepository == null || makerRepository == null || engineRepository == null) {
            throw new IllegalArgumentException();
        }

        this.authz = authz;
        this.aircraftModelBuilder = aircraftModelBuilder;
        this.aircraftModelRepository = aircraftModelRepository;
        this.makerRepository = makerRepository;
        this.engineRepository = engineRepository;
    }

    public Iterable<Maker> makers() {

        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR);

        return makerRepository.findAll();
    }

    public List<engineType> getEngineTypes() {
        return List.of(engineType.values());
    }

    public Iterable<engineModel> engineModels(final engineType motorization) {

        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR);

        return engineRepository.findByMotorization(motorization);
    }

    public aircraftModel registerAircraftModel(
            final String arModelName,
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

        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR);

        final aircraftModelName name = new aircraftModelName(arModelName);

        final aircraftModelId id = new aircraftModelId(maker.identity(), name);

        if (aircraftModelRepository.ofIdentity(id).isPresent()) {
            throw new IllegalArgumentException("Aircraft model already exists");
        }

        if (certifiedEngines == null || certifiedEngines.isEmpty()) {

            throw new IllegalArgumentException("Aircraft model must have at least one engine");
        }

        final aircraftModel aircraftModel = aircraftModelBuilder
                .with(name, maker, type, motorization, new maximumRange(maximumRangeKm),
                        new aircraftModelPhysicsData(emptyWeight, mtow, mzfw, fuelCapacity, serviceCeiling, cruiseSpeed, wingArea, dragCoefficient, liftCoefficient),
                        new HashSet<>(certifiedEngines))
                .build();

        return aircraftModelRepository.save(aircraftModel);
    }
}
