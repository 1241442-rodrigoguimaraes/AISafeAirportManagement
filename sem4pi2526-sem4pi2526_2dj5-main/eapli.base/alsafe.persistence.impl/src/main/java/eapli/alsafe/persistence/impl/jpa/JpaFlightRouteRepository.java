package eapli.alsafe.persistence.impl.jpa;

import eapli.alsafe.Application;
import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.airinfrastructure.domain.FlightRouteName;
import eapli.alsafe.airinfrastructure.repositories.FlightRouteRepository;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JpaFlightRouteRepository extends JpaAutoTxRepository<FlightRoute, FlightRouteName, FlightRouteName>
        implements FlightRouteRepository {

    public JpaFlightRouteRepository(final TransactionalContext autoTx) {
        super(autoTx, "flightRouteName");
    }

    public JpaFlightRouteRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(),
                "flightRouteName");
    }

    @Override
    public Optional<FlightRoute> findByName(String name) {
        final Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        return matchOne("e.flightRouteName.name=:name", params);
    }

    @Override
    public Iterable<FlightRoute> findByCompany(AirTransportCompany company) {
        final Map<String, Object> params = new HashMap<>();
        params.put("company", company);
        return match("e.company=:company", params);
    }
}
