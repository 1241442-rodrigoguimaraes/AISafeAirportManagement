package domain.airinfrastructure;

import eapli.alsafe.airinfrastructure.domain.*;
import eapli.alsafe.airports.domain.Airport;
import eapli.alsafe.airports.domain.IATAAirportCode;
import eapli.alsafe.airports.domain.ICAOAirportCode;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.IATACompanyCode;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.alsafe.utils.nodes.domain.Altitude;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.alsafe.utils.nodes.domain.Node;
import eapli.alsafe.utils.nodes.domain.NodeId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FlightRouteTest {

    private Airport airport1;
    private Airport airport2;
    private AirTransportCompany company;
    private FlightRouteName flightRouteName;

    @BeforeEach
    void setUp() {
        ICAOAirportCode icao1 = new ICAOAirportCode("ORLY");
        ICAOAirportCode icao2 = new ICAOAirportCode("ZURI");

        IATAAirportCode iata1 = new IATAAirportCode("ORL");
        IATAAirportCode iata2 = new IATAAirportCode("ZUR");

        String airportName1 = "Paris-Orly";
        String airportName2 = "Zurich";

        Node location1 = new Node(new NodeId(1), new Coordinate(48.7275, 2.3636), new Altitude(20.0));
        Node location2 = new Node(new NodeId(2), new Coordinate(47.3769, 8.5392), new Altitude(20.0));

        AirControlArea area1 = new AirControlArea(
                new AirControlAreaID(1L), "France",
                new AirControlAreaBoundaries(List.of(
                        new Coordinate(51.09, -4.79),
                        new Coordinate(51.09,  8.23),
                        new Coordinate(42.33,  8.23),
                        new Coordinate(42.33, -4.79)
                )),
                new AirControlAreaMinimumFuel(250.0)
        );
        AirControlArea area2 = new AirControlArea(
                new AirControlAreaID(2L), "Switzerland",
                new AirControlAreaBoundaries(List.of(
                        new Coordinate(47.81,  8.23),
                        new Coordinate(47.81, 10.49),
                        new Coordinate(45.82, 10.49),
                        new Coordinate(45.82,  8.23)
                )),
                new AirControlAreaMinimumFuel(300.0)
        );

        ICAOCompanyCode companyCodeICAO = new ICAOCompanyCode("AFR");

        IATACompanyCode companyCodeIATA = new IATACompanyCode("FR");

        String airlineName = "Air France";

        String routeName = "AF123";

        airport1 = new Airport(icao1, iata1, airportName1, location1, area1);
        airport2 = new Airport(icao2, iata2, airportName2, location2, area2);
        company = new AirTransportCompany(companyCodeIATA, companyCodeICAO, airlineName);
        flightRouteName = new FlightRouteName(routeName);
    }

    @Test
    void ensureFlightRouteCanBeCreated() {
        FlightRoute flightRoute = new FlightRoute(flightRouteName, airport1, airport2, company);

        assertEquals(flightRouteName, flightRoute.getFlightRouteName());
        assertEquals(airport1, flightRoute.getStartingAirport());
        assertEquals(airport2, flightRoute.getDestinationAirport());
        assertEquals(company, flightRoute.getCompany());
    }

    @Test
    void ensureFlightRouteCannotBeCreatedWithNullCompany() {
        assertThrows(IllegalArgumentException.class, () -> new FlightRoute(flightRouteName, airport1, airport2, null));
    }

    @Test
    void ensureFlightRouteCannotBeCreatedWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> new FlightRoute(null, airport1, airport2, company));
    }

    @Test
    void ensureFlightRouteCannotBeCreatedWithNullStartingAirport() {
        assertThrows(IllegalArgumentException.class, () -> new FlightRoute(flightRouteName, null, airport2, company));
    }

    @Test
    void ensureFlightRouteCannotBeCreatedWithNullDestinationAirport() {
        assertThrows(IllegalArgumentException.class, () -> new FlightRoute(flightRouteName, airport1, null, company));
    }

    @Test
    void ensureFlightRouteCannotHaveSameStartingAndDestinationAirport() {
        assertThrows(IllegalArgumentException.class, () -> new FlightRoute(flightRouteName, airport1, airport1, company));
    }

    @Test
    void ensureFlightRouteIdentityReturnsFlightRouteName() {
        FlightRoute flightRoute = new FlightRoute(flightRouteName, airport1, airport2, company);

        assertEquals(flightRouteName, flightRoute.identity());
    }

    @Test
    void ensureFlightRouteSameAsWorks() {
        FlightRoute flightRoute1 = new FlightRoute(flightRouteName, airport1, airport2, company);
        FlightRoute flightRoute2 = new FlightRoute(flightRouteName, airport1, airport2, company);

        assertTrue(flightRoute1.sameAs(flightRoute2));
    }

    @Test
    void ensureFlightRouteSameAsReturnsFalseForDifferentFlightRoutes() {
        FlightRoute flightRoute1 = new FlightRoute(flightRouteName, airport1, airport2, company);

        String name2 = "AF124";
        FlightRouteName flightRouteName2 = new FlightRouteName(name2);

        FlightRoute flightRoute2 = new FlightRoute(flightRouteName2, airport1, airport2, company);

        assertFalse(flightRoute1.sameAs(flightRoute2));
    }

    @Test
    void ensureFlightRouteToStringWorks() {
        FlightRoute flightRoute = new FlightRoute(flightRouteName, airport1, airport2, company);
        String result = flightRoute.toString();

        assertTrue(result.contains(flightRouteName.toString()));
        assertTrue(result.contains(airport1.getIataCode().toString()));
        assertTrue(result.contains(airport2.getAirportName()));
        assertTrue(result.contains(company.getName()));
    }
}
