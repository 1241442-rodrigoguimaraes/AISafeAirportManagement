package eapli.alsafe.persistence.impl.jpa;

import eapli.alsafe.Application;
import eapli.alsafe.airports.domain.Airport;
import eapli.alsafe.airports.domain.ICAOAirportCode;
import eapli.alsafe.airports.repository.AirportRepository;

import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public class JpaAirportRepository extends JpaAutoTxRepository<Airport, ICAOAirportCode, ICAOAirportCode>
        implements AirportRepository {

    public JpaAirportRepository(final TransactionalContext autoTx) {
        super(autoTx, "icao");
    }

    public JpaAirportRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "icao");
    }

    @Override
    public boolean existsByIATACode(String iataCode) {
        return createQuery("SELECT COUNT(a) FROM Airport a WHERE a.iataCode.code = :iataCode", Long.class)
                .setParameter("iataCode", iataCode.toUpperCase())
                .getSingleResult() > 0;
    }

    @Override
    public boolean existsByICAOCode(String icaoCode) {
        return createQuery("SELECT COUNT(a) FROM Airport a WHERE a.icaoCode.code = :icaoCode", Long.class)
                .setParameter("icaoCode", icaoCode.toUpperCase())
                .getSingleResult() > 0;
    }

    @Override
    public boolean sameCoordinates(Coordinate coordinates) {
        if (coordinates == null) {
            return false;
        }
        return createQuery("SELECT COUNT(a) FROM Airport a WHERE a.location.coordinates = :coordinates", Long.class)
                .setParameter("coordinates", coordinates)
                .getSingleResult() > 0;
    }

    @Override
    public Optional<Airport> findByName(String name) {
        return createQuery("SELECT a FROM Airport a WHERE a.airportName=:name", Airport.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }
}
