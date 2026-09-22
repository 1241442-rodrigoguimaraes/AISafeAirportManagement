package eapli.alsafe.persistence.impl.inmemory;

import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.alsafe.companies.repositories.AirCompanyRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryAirCompanyRepository extends InMemoryDomainRepository<AirTransportCompany, ICAOCompanyCode>
        implements AirCompanyRepository {

    static {
        InMemoryInitializer.init();
    }

    @Override
    public Optional<AirTransportCompany> findByName(String name) {
        return matchOne(c -> c.getName().equals(name));
    }

    @Override
    public Optional<AirTransportCompany> findByIATACode(String iata) {
        return matchOne(c -> c.getIata().equals(iata));
    }

    @Override
    public Optional<AirTransportCompany> findByICAOCode(String icao) {
        return matchOne(c -> c.getIcao().equals(icao));
    }

}
