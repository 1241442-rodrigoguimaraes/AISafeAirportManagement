package eapli.alsafe.airports.repository;

import eapli.alsafe.airports.domain.Airport;
import eapli.alsafe.airports.domain.ICAOAirportCode;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface AirportRepository extends DomainRepository<ICAOAirportCode, Airport> {
        boolean existsByIATACode(String iataCode);
        boolean existsByICAOCode(String icaoCode);
        boolean sameCoordinates(Coordinate coordinates);

        Optional<Airport> findByName(String name);
}
