package eapli.alsafe.persistence.impl.inmemory;

import eapli.alsafe.airports.domain.Airport;
import eapli.alsafe.airports.domain.ICAOAirportCode;
import eapli.alsafe.airports.repository.AirportRepository;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryAirportRepository extends InMemoryDomainRepository<Airport, ICAOAirportCode>
        implements AirportRepository {

    public InMemoryAirportRepository() {
        super();
    }

    @Override
    public boolean existsByIATACode(String iataCode) {
        if (iataCode == null) return false;
        final String searchCode = iataCode.trim().toUpperCase();
        for (final Airport a : findAll()) {
            if (a.getIataCode() != null && a.getIataCode().getCode().equals(searchCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean existsByICAOCode(String icaoCode) {
        if (icaoCode == null) return false;
        final String searchCode = icaoCode.trim().toUpperCase();
        for (final Airport a : findAll()) {
            if (a.getIcaoCode() != null && a.getIcaoCode().getCode().equals(searchCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean sameCoordinates(Coordinate coordinates) {
        if (coordinates == null) {
            return false;
        }
        for (final Airport a : findAll()) {
            if (a.getLocation() != null && a.getLocation().getCoordinates() != null && a.getLocation().getCoordinates().equals(coordinates)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Airport> findByName(String name) {
        return matchOne(a -> a.getAirportName().equals(name));
    }
}
