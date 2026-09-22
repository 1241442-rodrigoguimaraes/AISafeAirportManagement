package eapli.alsafe.aircraftModelMagnement.application;

import eapli.alsafe.aircraft.repositories.AircraftRepository;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
import eapli.alsafe.engineModelMagnement.Domain.engineModel;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class RemoveEngineModelController {

    private final AuthorizationService authz;

    private final aircraftModelRepository aircraftRepository;

    private final AircraftRepository fleetRepository;

    public RemoveEngineModelController() {
        this(
                AuthzRegistry.authorizationService(),
                PersistenceContext.repositories().aircraftModels(),
                PersistenceContext.repositories().aircraft()
        );
    }

    public RemoveEngineModelController(
            final AuthorizationService authz,
            final aircraftModelRepository aircraftRepository,
            final AircraftRepository fleetRepository) {

        this.authz = authz;
        this.aircraftRepository = aircraftRepository;
        this.fleetRepository = fleetRepository;
    }

    public Iterable<aircraftModel> aircraftModels() {

        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR);

        return aircraftRepository.findAll();
    }

    public void removeEngineModel(final aircraftModel aircraftModel,
                                  final engineModel engineModel) {

        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR);

        if (fleetRepository.existsByAircraftModel(aircraftModel)) {

            throw new IllegalStateException(
                    "Cannot remove engine model because aircraft are using this aircraft model."
            );
        }

        aircraftModel.removeCertifiedEngine(engineModel);

        aircraftRepository.save(aircraftModel);
    }
}