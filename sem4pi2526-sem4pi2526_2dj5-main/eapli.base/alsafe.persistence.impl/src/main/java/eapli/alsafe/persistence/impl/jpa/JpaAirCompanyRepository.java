package eapli.alsafe.persistence.impl.jpa;

import eapli.alsafe.Application;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.alsafe.companies.repositories.AirCompanyRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JpaAirCompanyRepository extends JpaAutoTxRepository<AirTransportCompany, ICAOCompanyCode, ICAOCompanyCode>
        implements AirCompanyRepository {

    public JpaAirCompanyRepository(final TransactionalContext autoTx) {
        super(autoTx, "icao");
    }

    public JpaAirCompanyRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(),
                "icao");
    }

    @Override
    public Optional<AirTransportCompany> findByName(String name) {
        final Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        return matchOne("e.name=:name", params);
    }

    @Override
    public Optional<AirTransportCompany> findByIATACode(String iata) {
        final Map<String, Object> params = new HashMap<>();
        params.put("iata", iata);
        return matchOne("e.iata.iata=:iata", params);
    }

    @Override
    public Optional<AirTransportCompany> findByICAOCode(String icao) {
        final Map<String, Object> params = new HashMap<>();
        params.put("icao", icao);
        return matchOne("e.icao.icao=:icao", params);
    }

}
