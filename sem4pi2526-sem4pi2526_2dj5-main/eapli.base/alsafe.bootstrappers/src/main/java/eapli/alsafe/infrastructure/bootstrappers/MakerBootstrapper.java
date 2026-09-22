package eapli.alsafe.infrastructure.bootstrappers;

import eapli.alsafe.aircraftModelMagnement.repositories.MakersRepository;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.domain.MakerCountry;
import eapli.alsafe.aircraftModelMagnement.domain.MakerName;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MakerBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(MakerBootstrapper.class);

    private final MakersRepository repo = PersistenceContext.repositories().makers();

    public boolean execute() {
        registerMaker("Pedrie", "FR");
        registerMaker("Rodrigos", "US");


        return true;
    }

    private void registerMaker(final String name, final String country) {
        final MakerName  makerName = new MakerName(name);

        if (repo.ofIdentity(makerName).isPresent()) {
            LOGGER.info("Maker {} already exists", name);
            return;
        }

        final Maker maker = new Maker(makerName, new MakerCountry(country));
        repo.save(maker);

        LOGGER.info("Registered maker {}", maker);
    }
}