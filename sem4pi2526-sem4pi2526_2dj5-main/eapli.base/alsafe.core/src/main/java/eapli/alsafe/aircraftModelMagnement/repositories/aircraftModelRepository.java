package eapli.alsafe.aircraftModelMagnement.repositories;

import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelId;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;


public interface aircraftModelRepository extends DomainRepository<aircraftModelId, aircraftModel> {

    /**
     * All aircraft models available for fleet configuration (same as {@link #findAll()}).
     */
    default Iterable<aircraftModel> getAircraftModels() {
        return findAll();
    }

    Optional<aircraftModel> findByName(final String name);
}
