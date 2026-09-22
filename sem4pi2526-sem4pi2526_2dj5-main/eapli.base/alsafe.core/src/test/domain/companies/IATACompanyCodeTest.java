package domain.companies;

import eapli.alsafe.companies.domain.IATACompanyCode;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IATACompanyCodeTest {

    @Test
    public void ensureCanCreateIATACode() {
        IATACompanyCode iata = new IATACompanyCode("AB");

        assertNotNull(iata);
        assertEquals("AB", iata.getIata());
    }

    @Test
    public void ensureIATACodeIsCaseInsensitive() {
        IATACompanyCode iata = new IATACompanyCode("ab");

        assertNotNull(iata);
    }

    @Test
    public void ensureTwoIATACodesWithSameValueAreEqual() {
        IATACompanyCode iata1 = new IATACompanyCode("AB");
        IATACompanyCode iata2 = new IATACompanyCode("AB");

        assertEquals(iata1, iata2);
    }

    @Test
    public void ensureTwoIATACodesWithDifferentValuesAreNotEqual() {
        IATACompanyCode iata1 = new IATACompanyCode("AB");
        IATACompanyCode iata2 = new IATACompanyCode("BC");

        assertNotEquals(iata1, iata2);
    }

    @Test
    public void ensureToStringReturnsIATACode() {
        IATACompanyCode iata = new IATACompanyCode("AB");

        assertEquals("AB", iata.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCannotCreateWithNullIATA() {
        new IATACompanyCode(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCannotCreateWithOneChar() {
        new IATACompanyCode("A");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCannotCreateWithThreeChars() {
        new IATACompanyCode("ABC");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCannotCreateWithNumbers() {
        new IATACompanyCode("A1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCannotCreateWithSpecialChars() {
        new IATACompanyCode("A!");
    }
}