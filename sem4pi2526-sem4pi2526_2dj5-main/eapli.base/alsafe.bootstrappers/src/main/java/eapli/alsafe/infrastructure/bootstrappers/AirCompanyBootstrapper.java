package eapli.alsafe.infrastructure.bootstrappers;

import eapli.alsafe.companies.application.RegisterAirCompanyController;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirCompanyBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(AirCompanyBootstrapper.class);

    @Override
    public boolean execute() {
        registerCompany("PT", "POR", "Portugal Airlines");
        registerCompany("IB", "IBE", "Iberian Airlines");

        return true;
    }

    public void registerCompany(final String iata, final String icao, final String name) {
        final RegisterAirCompanyController controller = new RegisterAirCompanyController();

        controller.checkIATA(iata);
        controller.checkICAO(icao);
        controller.checkName(name);

        final AirTransportCompany company = controller.createAirTransportCompany(iata, icao, name);

        LOGGER.info("Registered Air Transport Company: {}", company);
    }
}
