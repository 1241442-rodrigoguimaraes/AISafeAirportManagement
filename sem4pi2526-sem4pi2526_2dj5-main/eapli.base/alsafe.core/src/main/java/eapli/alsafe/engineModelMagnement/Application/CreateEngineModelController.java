package eapli.alsafe.engineModelMagnement.Application;

import eapli.alsafe.engineModelMagnement.Domain.*;
import eapli.alsafe.engineModelMagnement.Repositories.engineModelRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.repositories.MakersRepository;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class CreateEngineModelController {

    private final AuthorizationService authz;

    private final engineModelRepository engineModelRepository;

    private final MakersRepository makerRepository;

    private final EngineModelBuilder engineModelBuilder;

    public CreateEngineModelController() {
        this(
                AuthzRegistry.authorizationService(),
                new EngineModelBuilder(),
                PersistenceContext.repositories().engineModels(),
                PersistenceContext.repositories().makers()
        );
    }

    public CreateEngineModelController(
            final AuthorizationService authz,
            final engineModelRepository engineModelRepository,
            final MakersRepository makerRepository) {
        this(authz, new EngineModelBuilder(), engineModelRepository, makerRepository);
    }

    public CreateEngineModelController(
            final AuthorizationService authz,
            final EngineModelBuilder engineModelBuilder,
            final engineModelRepository engineModelRepository,
            final MakersRepository makerRepository) {

        if (authz == null || engineModelBuilder == null || engineModelRepository == null || makerRepository == null) {
            throw new IllegalArgumentException();
        }

        this.authz = authz;
        this.engineModelBuilder = engineModelBuilder;
        this.engineModelRepository = engineModelRepository;
        this.makerRepository = makerRepository;
    }

    public Iterable<Maker> makers() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR);

        return makerRepository.findAll();
    }

    public engineModel registerEngineModel(final String modelName,
                                           final Maker maker,
                                           final engineType type,
                                           final double power,
                                           final engineModelFuel fuel,
                                           final double efficiency) {

        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR);

        final engineModelName name = new engineModelName(modelName);

        final engineModelId id =
                new engineModelId(name, maker.identity());

        if (engineModelRepository.ofIdentity(id).isPresent()) {
            throw new IllegalArgumentException(
                    "An engine model with the same name and maker already exists."
            );
        }

        final engineModel engineModel = engineModelBuilder
                .with(name, maker, type, new engineModelPower(power), fuel, new engineModelEfficiency(efficiency))
                .build();

        return engineModelRepository.save(engineModel);
    }
}
