package domain.AirportTest;

import eapli.alsafe.airports.domain.ICAOAirportCode;
import org.junit.Test;

public class ICAOAirportCodeTest {
    @Test
    public void testICAO() {
        String code = "HSBP";
        ICAOAirportCode ICAO = new ICAOAirportCode(code);
        assert(ICAO.getCode().equals(code));
    }

    @Test
    public void testEquals() {
        String code1 = "HSBP";
        String code2 = "HSBP";
        ICAOAirportCode ICAO1 = new ICAOAirportCode(code1);
        ICAOAirportCode ICAO2 = new ICAOAirportCode(code2);
        assert(ICAO1.equals(ICAO2));
    }

    @Test
    public void testNotEquals() {
        String code1 = "HSBP";
        String code2 = "HSDP";
        ICAOAirportCode ICAO1 = new ICAOAirportCode(code1);
        ICAOAirportCode ICAO2 = new ICAOAirportCode(code2);
        assert(!ICAO1.equals(ICAO2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatTooLong(){
        new ICAOAirportCode("HSBP123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatNumbers(){
        new ICAOAirportCode("1234");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatNull(){
        new ICAOAirportCode(null);
    }

    @Test
    public void testValidFormat(){
        assert(ICAOAirportCode.validICAOFormat("HSBP"));
        assert(ICAOAirportCode.validICAOFormat("gsgg"));
    }
}
