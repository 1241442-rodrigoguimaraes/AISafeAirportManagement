package domain.companies;

import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.IATACompanyCode;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AirTransportCompanyTest {

    private static final IATACompanyCode IATA1 = new IATACompanyCode("AB");
    private static final IATACompanyCode IATA2 = new IATACompanyCode("BC");

    private static final ICAOCompanyCode ICAO1 = new ICAOCompanyCode("ABC");
    private static final ICAOCompanyCode ICAO2 = new ICAOCompanyCode("BCD");

    private static final String NAME1 = "AirCompany1";
    private static final String NAME2 = "AirCompany2";

    @Test
    public void ensureCanCreateAirTransportCompany() {
        AirTransportCompany company = new AirTransportCompany(IATA1, ICAO1, NAME1);

        assertNotNull(company);
        assertEquals(IATA1, company.getIata());
        assertEquals(ICAO1, company.getIcao());
        assertEquals(NAME1, company.getName());
    }

    @Test
    public void ensureAirTransportCompanyNameIsUnique() {
        AirTransportCompany company1 = new AirTransportCompany(IATA1, ICAO1, NAME1);
        AirTransportCompany company2 = new AirTransportCompany(IATA2, ICAO2, NAME1);

        assertNotEquals(company1, company2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCannotCreateWithNullIATA() {
        new AirTransportCompany(null, ICAO1, NAME1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCannotCreateWithNullICAO() {
        new AirTransportCompany(IATA1, null, NAME1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCannotCreateWithNullName() {
        new AirTransportCompany(IATA1, ICAO1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCannotCreateWithBlankName() {
        new AirTransportCompany(IATA1, ICAO1, " ");
    }

    @Test
    public void ensureAirTransportCompanySameAsWorks() {
        AirTransportCompany company1 = new AirTransportCompany(IATA1, ICAO1, NAME1);
        AirTransportCompany company2 = new AirTransportCompany(IATA1, ICAO1, NAME1);

        assertTrue(company1.sameAs(company2));
    }

    @Test
    public void ensureICAOIsIdentity() {
        AirTransportCompany company = new AirTransportCompany(IATA1, ICAO1, NAME1);

        assertEquals(ICAO1, company.identity());
    }

    @Test
    public void ensureToStringWorks() {
        AirTransportCompany company = new AirTransportCompany(IATA1, ICAO1, NAME1);

        String expected = "Air Transport Company : Name - " + NAME1 + ";\n" +
                "ICAO - " + ICAO1 + ";\n" +
                "IATA - " + IATA1 + ".";

        assertEquals(expected, company.toString());
    }
}
