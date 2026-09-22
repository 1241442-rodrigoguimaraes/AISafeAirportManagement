package eapli.alsafe.airinfrastructure.domain;

import eapli.alsafe.airports.domain.Airport;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.domain.model.DomainFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlightRouteBuilder implements DomainFactory<FlightRoute> {

    private static final Logger LOGGER = LogManager.getLogger(FlightRouteBuilder.class);

    private FlightRouteName name;
    private Airport startingAirport;
    private Airport destinationAirport;
    private AirTransportCompany company;

    public FlightRouteBuilder() {}

    public FlightRouteBuilder with(final String name, final Airport startingAirport, final Airport destinationAirport, final AirTransportCompany company) {
        withName(name);
        withStartingAirport(startingAirport);
        withDestinationAirport(destinationAirport);
        withCompany(company);

        return this;
    }

    public FlightRouteBuilder withName(final String name) {
        this.name = new FlightRouteName(name);
        return this;
    }

    public FlightRouteBuilder withStartingAirport(final Airport startingAirport) {
        this.startingAirport = startingAirport;
        return this;
    }

    public FlightRouteBuilder withDestinationAirport(final Airport destinationAirport) {
        this.destinationAirport = destinationAirport;
        return this;
    }

    public FlightRouteBuilder withCompany(final AirTransportCompany company) {
        this.company = company;
        return this;
    }

    @Override
    public FlightRoute build() {
        final var flightRoute = new FlightRoute(name, startingAirport, destinationAirport, company);
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Building Flight Route : [{}, {}, {}, {}]", name, startingAirport, destinationAirport, company);

        return flightRoute;
    }
}
