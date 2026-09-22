package eapli.alsafe.persistence.impl.inmemory;

import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelId;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;


public class InMemoryAircraftModelRepository extends InMemoryDomainRepository<aircraftModel, aircraftModelId> implements aircraftModelRepository {

    public InMemoryAircraftModelRepository() {
        super();
    }

    @Override
    public Optional<aircraftModel> findByName(final String name) {
        return matchOne(e -> e.name().equals(name));
    }
}
