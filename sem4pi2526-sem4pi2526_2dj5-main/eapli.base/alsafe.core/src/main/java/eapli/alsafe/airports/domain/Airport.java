package eapli.alsafe.airports.domain;


import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.utils.nodes.domain.Node;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import jakarta.persistence.*;
import lombok.Getter;

import java.io.Serial;
import java.util.Objects;

@Entity
public class Airport implements AggregateRoot<ICAOAirportCode> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @Getter
    @EmbeddedId
    private ICAOAirportCode icaoCode;

    @Getter
    @Embedded
    @Column(unique = true)
    private IATAAirportCode iataCode;

    @Getter
    @Column(unique = true, name = "AIRPORT_NAME")
    private String airportName;

    @Getter
    @ManyToOne(optional = false)
    @JoinColumn(name = "LOCATION_ID")
    private Node location;

    @Getter
    @ManyToOne(optional = false)
    @JoinColumn(name = "AIR_CONTROL_AREA_ID")
    private AirControlArea airControlArea;

    public Airport(final ICAOAirportCode icaoCode, final IATAAirportCode iataCode, final String airportName, final Node location, final AirControlArea airControlArea) {
        if (icaoCode == null || iataCode == null || airportName == null || location == null || airControlArea == null)
            throw new IllegalArgumentException();

        this.icaoCode = icaoCode;
        this.iataCode = iataCode;
        this.airportName = airportName;
        this.location = location;
        this.airControlArea = airControlArea;
    }

    protected Airport() {}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Airport airport = (Airport) o;
        return Objects.equals(icaoCode, airport.icaoCode) && Objects.equals(iataCode, airport.iataCode) && Objects.equals(airportName, airport.airportName) && Objects.equals(location, airport.location) && Objects.equals(airControlArea, airport.airControlArea);
    }

    @Override
    public int hashCode() {
        return Objects.hash(icaoCode, iataCode, airportName, location, airControlArea);
    }

    @Override
    public boolean sameAs(Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public ICAOAirportCode identity() {
        return icaoCode;
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s) at %s on %s - %s", icaoCode, airportName, iataCode, location, airControlArea.getId(), airControlArea.getName());
    }
}
