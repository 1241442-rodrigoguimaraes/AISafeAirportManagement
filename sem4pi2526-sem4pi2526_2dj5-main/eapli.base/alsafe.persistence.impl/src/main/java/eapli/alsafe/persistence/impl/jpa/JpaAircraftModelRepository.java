package eapli.alsafe.persistence.impl.jpa;

import eapli.alsafe.Application;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelId;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class JpaAircraftModelRepository extends JpaAutoTxRepository<aircraftModel, aircraftModelId, aircraftModelId>
        implements aircraftModelRepository {

    public JpaAircraftModelRepository(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    public JpaAircraftModelRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "id");
    }

    @Override
    public Optional<aircraftModel> findByName(final String name) {
        final Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        return matchOne("e.arModelName.name = :name", params);
    }
}
