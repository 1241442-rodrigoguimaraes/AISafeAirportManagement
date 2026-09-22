package eapli.alsafe.engineModelMagnement.Repositories;

import eapli.alsafe.engineModelMagnement.Domain.engineModel;
import eapli.alsafe.engineModelMagnement.Domain.engineModelId;
import eapli.alsafe.engineModelMagnement.Domain.engineType;
import eapli.framework.domain.repositories.DomainRepository;

public interface engineModelRepository extends DomainRepository<engineModelId, engineModel> {

    Iterable<engineModel> findByMotorization(final engineType motorization);
}
