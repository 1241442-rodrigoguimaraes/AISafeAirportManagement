package eapli.alsafe.persistence.impl.inmemory;

import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.aircraft.domain.AircraftCountry;
import eapli.alsafe.aircraft.domain.CabinConfiguration;
import eapli.alsafe.aircraft.domain.CrewElement;
import eapli.alsafe.aircraft.domain.RegistrationID;
import eapli.alsafe.aircraft.repositories.AircraftRepository;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.List;

public class InMemoryAircraftRepository extends InMemoryDomainRepository<Aircraft, RegistrationID>
        implements AircraftRepository {

    @Override
    public boolean existsByRegistrationID(final RegistrationID registrationID) {
        return matchOne(a -> a.identity().equals(registrationID)).isPresent();
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
        return match(a -> company.identity().equals(a.company().identity()));
    }

    @Override
    public boolean existsByAircraftModel(final aircraftModel model) {

        return matchOne(a -> a.model().equals(model)).isPresent();
    }

    @Override
    public Iterable<Aircraft> findByCompanyAndModel(final AirTransportCompany company,
                                                    final aircraftModel model) {

        return match(a ->
                a.company().identity().equals(company.identity())
                        && a.model().equals(model)
        );
    }


}
