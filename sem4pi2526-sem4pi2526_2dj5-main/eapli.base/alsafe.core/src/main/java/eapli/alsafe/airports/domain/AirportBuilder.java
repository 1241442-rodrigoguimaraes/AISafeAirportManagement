package eapli.alsafe.airports.domain;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.utils.nodes.domain.Node;
import eapli.framework.domain.model.DomainFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AirportBuilder implements DomainFactory<Airport> {

    private static final Logger LOGGER = LogManager.getLogger(AirportBuilder.class);

    private ICAOAirportCode icaoCode;
    private IATAAirportCode iataCode;
    private String airportName;
    private Node location;
    private AirControlArea airControlArea;

    public AirportBuilder() {}

    public AirportBuilder with(final String icaoCode, final String iataCode, final String airportName,
                               final Node location, final AirControlArea airControlArea) {
        withICAO(icaoCode);
        withIATA(iataCode);
        withName(airportName);
        withLocation(location);
        withAirControlArea(airControlArea);
        return this;
    }

    public AirportBuilder withICAO(final String icaoCode) {
        this.icaoCode = new ICAOAirportCode(icaoCode);
        return this;
    }

    public AirportBuilder withICAO(final ICAOAirportCode icaoCode) {
        this.icaoCode = icaoCode;
        return this;
    }

    public AirportBuilder withIATA(final String iataCode) {
        this.iataCode = new IATAAirportCode(iataCode);
        return this;
    }

    public AirportBuilder withIATA(final IATAAirportCode iataCode) {
        this.iataCode = iataCode;
        return this;
    }

    public AirportBuilder withName(final String airportName) {
        this.airportName = airportName;
        return this;
    }

    public AirportBuilder withLocation(final Node location) {
        this.location = location;
        return this;
    }

    public AirportBuilder withAirControlArea(final AirControlArea airControlArea) {
        this.airControlArea = airControlArea;
        return this;
    }

    @Override
    public Airport build() {
        final var airport = new Airport(icaoCode, iataCode, airportName, location, airControlArea);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Building Airport : [{}]", airport);
        }

        return airport;
    }
}
