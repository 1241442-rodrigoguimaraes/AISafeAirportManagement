package eapli.alsafe.persistence.impl.inmemory;

import eapli.alsafe.aircraftModelMagnement.repositories.MakersRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.domain.MakerName;

public class InMemoryMakersRepository extends InMemoryDomainRepository<Maker, MakerName>
        implements MakersRepository {

    public InMemoryMakersRepository() {
        super();
    }
}
