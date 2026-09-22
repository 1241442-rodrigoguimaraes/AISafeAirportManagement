package eapli.alsafe.persistence.impl.inmemory;

import eapli.alsafe.engineModelMagnement.Domain.engineModel;
import eapli.alsafe.engineModelMagnement.Domain.engineModelId;
import eapli.alsafe.engineModelMagnement.Domain.engineType;
import eapli.alsafe.engineModelMagnement.Repositories.engineModelRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

public class InMemoryEngineModelRepository extends InMemoryDomainRepository<engineModel, engineModelId>
        implements engineModelRepository {

    public InMemoryEngineModelRepository() {
        super();
    }

    @Override
    public Iterable<engineModel> findByMotorization(final engineType motorization) {
        return match(e -> e.getType() == motorization);
    }
}
