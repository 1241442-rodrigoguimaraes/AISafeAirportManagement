package eapli.alsafe.persistence.impl.jpa;

import eapli.alsafe.Application;
import eapli.alsafe.engineModelMagnement.Domain.engineModel;
import eapli.alsafe.engineModelMagnement.Domain.engineModelId;
import eapli.alsafe.engineModelMagnement.Domain.engineType;
import eapli.alsafe.engineModelMagnement.Repositories.engineModelRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;


public class JpaEngineModelRepository extends JpaAutoTxRepository<engineModel, engineModelId, engineModelId> implements engineModelRepository {

    public JpaEngineModelRepository(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    public JpaEngineModelRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "id");
    }

    @Override
    public Iterable<engineModel> findByMotorization(final engineType motorization) {
        final Map<String, Object> params = new HashMap<>();
        params.put("type", motorization);
        return match("e.type=:type", params);
    }
}
