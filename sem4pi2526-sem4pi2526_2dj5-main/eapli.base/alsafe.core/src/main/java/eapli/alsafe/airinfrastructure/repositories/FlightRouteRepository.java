package eapli.alsafe.airinfrastructure.repositories;

import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.airinfrastructure.domain.FlightRouteName;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface FlightRouteRepository extends DomainRepository<FlightRouteName, FlightRoute> {

    Optional<FlightRoute> findByName(String name);
    Iterable<FlightRoute> findByCompany(AirTransportCompany company);
}
