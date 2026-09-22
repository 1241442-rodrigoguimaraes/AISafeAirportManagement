package domain.AirportTest;

import eapli.alsafe.airports.domain.IATAAirportCode;
import org.junit.Test;

public class IATAAirportCodeTest {
    @Test
    public void testICAO() {
        String code = "HSB";
        IATAAirportCode IATA = new IATAAirportCode(code);
        assert(IATA.getCode().equals(code));
    }

    @Test
    public void testEquals() {
        String code1 = "HSB";
        String code2 = "HSB";
        IATAAirportCode IATA1 = new IATAAirportCode(code1);
        IATAAirportCode IATA2 = new IATAAirportCode(code2);
        assert(IATA1.equals(IATA2));
    }

    @Test
    public void testNotEquals() {
        String code1 = "HSB";
        String code2 = "HSD";
        IATAAirportCode IATA1 = new IATAAirportCode(code1);
        IATAAirportCode IATA2 = new IATAAirportCode(code2);
        assert(!IATA1.equals(IATA2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatTooLong(){
        new IATAAirportCode("HSB123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatNumbers(){
        new IATAAirportCode("123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatNull(){
        new IATAAirportCode(null);
    }

    @Test
    public void testValidFormat(){
        assert(IATAAirportCode.validIATAFormat("HSB"));
        assert(IATAAirportCode.validIATAFormat("gss"));
    }
}
