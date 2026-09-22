package eapli.alsafe.persistence.impl.jpa;

import eapli.alsafe.Application;
import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JpaAirControlAreaRepository extends JpaAutoTxRepository<AirControlArea, AirControlAreaID, AirControlAreaID>
        implements AirControlAreaRepository {

    public JpaAirControlAreaRepository(final TransactionalContext autoTx) {
        super(autoTx, "airControlAreaID");
    }

    public JpaAirControlAreaRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(),
                "airControlAreaID");
    }

    @Override
    public Optional<AirControlArea> findById(AirControlAreaID id) {
        final Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        return matchOne("e.id=:id", params);
    }

    @Override
    public Optional<AirControlArea> findByCode(final String code) {
        final Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        return matchOne("e.id.id=:code", params);
    }
}
