package eapli.alsafe.aircraft.repositories;

import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.aircraft.domain.AircraftCountry;
import eapli.alsafe.aircraft.domain.CabinConfiguration;
import eapli.alsafe.aircraft.domain.CrewElement;
import eapli.alsafe.aircraft.domain.RegistrationID;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.List;

public interface AircraftRepository extends DomainRepository<RegistrationID, Aircraft> {

    boolean existsByRegistrationID(RegistrationID registrationID);

    boolean existsByAircraftModel(aircraftModel model);

    boolean addAircraft(aircraftModel model, RegistrationID registrationID,
                        AircraftCountry country, CabinConfiguration cabinConfig,
                        List<CrewElement> crew, AirTransportCompany company);

    Iterable<Aircraft> findByCompany(AirTransportCompany company);

    Iterable<Aircraft> findByCompanyAndModel(AirTransportCompany company,
                                             aircraftModel model);
}
