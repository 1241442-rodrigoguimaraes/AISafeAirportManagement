package eapli.alsafe.airinfrastructure.domain;

import eapli.alsafe.airports.domain.Airport;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Entity
@EqualsAndHashCode(of = "flightRouteName")
public class FlightRoute implements AggregateRoot<FlightRouteName>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @EmbeddedId
    private FlightRouteName flightRouteName;

    @JoinColumn
    @ManyToOne(optional = false)
    private Airport startingAirport;

    @JoinColumn
    @ManyToOne(optional = false)
    private Airport destinationAirport;

    @JoinColumn
    @ManyToOne(optional = false)
    private AirTransportCompany company;

    public FlightRoute(FlightRouteName flightRouteName, Airport startingAirport, Airport destinationAirport, AirTransportCompany company) {
        if (flightRouteName == null || startingAirport == null || destinationAirport == null || company == null) throw new IllegalArgumentException("One or more parameters for the flight route construction are null");
        Preconditions.ensure(!startingAirport.equals(destinationAirport), "Starting and destination airports cannot be the same");

        this.flightRouteName = flightRouteName;
        this.startingAirport = startingAirport;
        this.destinationAirport = destinationAirport;
        this.company = company;
    }

    protected FlightRoute() {
        // for ORM
    }

    @Override
    public FlightRouteName identity() {
        return this.flightRouteName;
    }

    @Override
    public boolean sameAs(Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public String toString() {
        return this.flightRouteName.toString() + ": " + " (" + this.startingAirport.getIataCode() + ") " +
                this.startingAirport.getAirportName() + " -> " + " (" + this.destinationAirport.getIataCode() + ") " +
                this.destinationAirport.getAirportName() + " via " + this.company.getName();
    }
}
