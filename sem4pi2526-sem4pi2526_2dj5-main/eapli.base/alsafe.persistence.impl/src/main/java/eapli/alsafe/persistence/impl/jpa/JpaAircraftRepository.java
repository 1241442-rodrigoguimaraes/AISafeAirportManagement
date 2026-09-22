package eapli.alsafe.persistence.impl.jpa;

import eapli.alsafe.Application;
import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.aircraft.domain.AircraftCountry;
import eapli.alsafe.aircraft.domain.CabinConfiguration;
import eapli.alsafe.aircraft.domain.CrewElement;
import eapli.alsafe.aircraft.domain.RegistrationID;
import eapli.alsafe.aircraft.repositories.AircraftRepository;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JpaAircraftRepository extends JpaAutoTxRepository<Aircraft, RegistrationID, RegistrationID>
        implements AircraftRepository {

    public JpaAircraftRepository(final TransactionalContext autoTx) {
        super(autoTx, "registrationId");
    }

    public JpaAircraftRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "registrationId");
    }

    @Override
    public boolean existsByRegistrationID(final RegistrationID registrationID) {
        final Map<String, Object> params = new HashMap<>();
        params.put("registrationId", registrationID);
        return matchOne("e.registrationId = :registrationId", params).isPresent();
    }

    @Override
    public boolean addAircraft(final aircraftModel model, final RegistrationID registrationID,
                               final AircraftCountry country, final CabinConfiguration cabinConfig,
                               final List<CrewElement> crew, final AirTransportCompany company) {
        final Aircraft aircraft = Aircraft.create(model, registrationID, country, cabinConfig, crew, company);
        save(aircraft);
        return true;
    }

    @Override
    public Iterable<Aircraft> findByCompany(final AirTransportCompany company) {
        final Map<String, Object> params = new HashMap<>();
        params.put("icao", company.identity());
        return match("e.company.icao = :icao", params);
    }

    @Override
    public boolean existsByAircraftModel(final aircraftModel model) {

        final Map<String, Object> params = new HashMap<>();
        params.put("model", model);

        return matchOne("e.aircraftModel = :model", params).isPresent();
    }

    @Override
    public Iterable<Aircraft> findByCompanyAndModel(final AirTransportCompany company,
                                                    final aircraftModel model) {

        final Map<String, Object> params = new HashMap<>();
        params.put("icao", company.identity());
        params.put("model", model);

        return match(
                "e.company.icao = :icao AND e.aircraftModel = :model",
                params
        );
    }
}
