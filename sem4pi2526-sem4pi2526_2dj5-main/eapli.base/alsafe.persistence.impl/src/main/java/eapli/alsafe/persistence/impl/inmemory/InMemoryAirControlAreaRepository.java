package eapli.alsafe.persistence.impl.inmemory;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryAirControlAreaRepository extends InMemoryDomainRepository<AirControlArea, AirControlAreaID>
        implements AirControlAreaRepository {

    static {
        InMemoryInitializer.init();
    }

    @Override
    public Optional<AirControlArea> findById(AirControlAreaID id) {
        return matchOne(a -> a.getId().equals(id));
    }

    @Override
    public Optional<AirControlArea> findByCode(final String code) {
        return matchOne(a -> a.identity().toString().equals(code));
    }
}
