package eapli.alsafe.infrastructure.bootstrappers;

import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.airports.application.RegisterAirportController;
import eapli.alsafe.airports.domain.Airport;
import eapli.alsafe.airports.repository.AirportRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirportBootstrapper implements Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(AirportBootstrapper.class);

    private final AirportRepository airportRepository = PersistenceContext.repositories().airports();
    private final AirControlAreaRepository airControlAreaRepository = PersistenceContext.repositories().areas();
    private final RegisterAirportController registerAirportController = new RegisterAirportController();

    @Override
    public boolean execute() {
        try {
            registerAirport();
            return true;
        } catch (final Exception e) {
            LOGGER.error("Error bootstrapping airports", e);
            return false;
        }
    }

    private void registerAirport() {
        if (!airportRepository.existsByIATACode("LIS")) {
            if (!airControlAreaRepository.findAll().iterator().hasNext()) {
                LOGGER.error("No Air Control Area available. Cannot register airport {}");
                return;
            }

            final Airport airport = registerAirportController.registerAirport(
                    "LPPT",
                    "LIS",
                    "Lisbon Airport",
                    37.0, -8.0,
                    22.0,
                    airControlAreaRepository.findAll().iterator().next().getId()
            );
            LOGGER.info("Registered Airport: {}", airport);
        } else {
            LOGGER.warn("Airport with IATA code {} already exists. Skipping registration.", "LIS");
        }
    }

}
