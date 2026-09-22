package eapli.alsafe.companies.domain;

import eapli.framework.domain.model.DomainFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AirCompanyBuilder implements DomainFactory<AirTransportCompany> {

    private static final Logger LOGGER = LogManager.getLogger(AirCompanyBuilder.class);

    private String name;
    private IATACompanyCode iata;
    private ICAOCompanyCode icao;

    public AirCompanyBuilder() {}

    public AirCompanyBuilder with(final IATACompanyCode iata, final ICAOCompanyCode icao, final String name) {
        withName(name);
        withIATA(iata);
        withICAO(icao);

        return this;
    }

    public AirCompanyBuilder with(final String iata, final String icao, final String name) {
        withName(name);
        withIATA(iata);
        withICAO(icao);

        return this;
    }

    public AirCompanyBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public AirCompanyBuilder withIATA(final IATACompanyCode iata) {
        this.iata = iata;
        return this;
    }

    public AirCompanyBuilder withIATA(final String iata) {
        this.iata = new IATACompanyCode(iata);
        return this;
    }

    public AirCompanyBuilder withICAO(final ICAOCompanyCode icao) {
        this.icao = icao;
        return this;
    }

    public AirCompanyBuilder withICAO(final String icao) {
        this.icao = new ICAOCompanyCode(icao);
        return this;
    }

    @Override
    public AirTransportCompany build() {
        final var company = new AirTransportCompany(iata, icao, name);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Building Air Transport Company : [{}]", company);
        }

        return company;
    }
}
