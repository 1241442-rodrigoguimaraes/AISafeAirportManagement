package eapli.alsafe.persistence.impl.inmemory;

import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.airinfrastructure.domain.FlightRouteName;
import eapli.alsafe.airinfrastructure.repositories.FlightRouteRepository;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryFlightRouteRepository extends InMemoryDomainRepository<FlightRoute, FlightRouteName>
        implements FlightRouteRepository {

    static {
        InMemoryInitializer.init();
    }

    @Override
    public Optional<FlightRoute> findByName(String name) {
        return matchOne(a -> a.getFlightRouteName().getName().equals(name));
    }

    @Override
    public Iterable<FlightRoute> findByCompany(AirTransportCompany company) {
        return match(a -> a.getCompany().equals(company));
    }
}
