package eapli.alsafe.airinfrastructure.repositories;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface AirControlAreaRepository extends DomainRepository<AirControlAreaID, AirControlArea> {

    Optional<AirControlArea> findById(AirControlAreaID id);

    Optional<AirControlArea> findByCode(String code);

    Iterable<AirControlArea> findAll();
}
