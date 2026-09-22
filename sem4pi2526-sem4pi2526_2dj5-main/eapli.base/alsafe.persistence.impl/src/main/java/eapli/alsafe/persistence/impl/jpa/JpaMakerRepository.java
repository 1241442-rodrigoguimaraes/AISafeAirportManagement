package eapli.alsafe.persistence.impl.jpa;

import eapli.alsafe.Application;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.repositories.MakersRepository;
import eapli.alsafe.aircraftModelMagnement.domain.MakerName;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

public class JpaMakerRepository extends JpaAutoTxRepository<Maker, MakerName, MakerName> implements MakersRepository {

    public JpaMakerRepository(final TransactionalContext autoTx) {
        super(autoTx, "name");
    }

    public JpaMakerRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "name");
    }

}