package eapli.alsafe.aircraftModelMagnement.application;

import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
import eapli.alsafe.engineModelMagnement.Domain.engineModel;
import eapli.alsafe.engineModelMagnement.Repositories.engineModelRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.ArrayList;
import java.util.List;

@UseCaseController
public class AddCertifiedEngineController {

    private final AuthorizationService authz;
    private final aircraftModelRepository amRepository;
    private final engineModelRepository emRepository;

    public AddCertifiedEngineController() {
        this(AuthzRegistry.authorizationService(), PersistenceContext.repositories().aircraftModels(), PersistenceContext.repositories().engineModels());
    }

    public AddCertifiedEngineController(final AuthorizationService authz, final aircraftModelRepository amRepository, final engineModelRepository emRepository) {
        if (authz == null || amRepository == null || emRepository == null) throw new IllegalArgumentException();

        this.authz = authz;
        this.amRepository = amRepository;
        this.emRepository = emRepository;
    }

    public Iterable<aircraftModel> getAllAircraftModels() {
        return amRepository.findAll();
    }

    public Iterable<engineModel> getCompatibleEngineModels(final aircraftModel aircraft) {
        List<engineModel> allCompatible = new ArrayList<>();
        emRepository.findByMotorization(aircraft.motorization()).forEach(allCompatible::add);

        return allCompatible.stream()
                .filter(em -> !aircraft.certifiedEngines().contains(em))
                .toList();
    }

    public void addCertifiedEngine(aircraftModel aircraftModel, engineModel engineModel) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR);

        aircraftModel.addCertifiedEngine(engineModel);

        amRepository.save(aircraftModel);
    }
}
