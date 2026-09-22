package eapli.alsafe.companies.repositories;

import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface AirCompanyRepository extends DomainRepository<ICAOCompanyCode, AirTransportCompany> {

    Optional<AirTransportCompany> findByName(String name);

    Optional<AirTransportCompany> findByIATACode(String iata);

    Optional<AirTransportCompany> findByICAOCode(String icao);

}
