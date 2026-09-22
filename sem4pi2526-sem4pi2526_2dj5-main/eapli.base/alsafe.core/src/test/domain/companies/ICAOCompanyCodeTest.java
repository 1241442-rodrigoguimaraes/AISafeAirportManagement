package domain.companies;

import eapli.alsafe.companies.domain.ICAOCompanyCode;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ICAOCompanyCodeTest {

    @Test
    public void ensureCanCreateICAOCodeWithTwoChars() {
        ICAOCompanyCode icao = new ICAOCompanyCode("AB");

        assertNotNull(icao);
        assertEquals("AB", icao.getIcao());
    }

    @Test
    public void ensureCanCreateICAOCodeWithThreeChars() {
        ICAOCompanyCode icao = new ICAOCompanyCode("ABC");

        assertNotNull(icao);
        assertEquals("ABC", icao.getIcao());
    }

    @Test
    public void ensureICAOCodeIsCaseInsensitive() {
        ICAOCompanyCode icao1 = new ICAOCompanyCode("ab");
        ICAOCompanyCode icao2 = new ICAOCompanyCode("abc");

        assertNotNull(icao1);
        assertNotNull(icao2);
    }

    @Test
    public void ensureTwoICAOCodesWithSameValueAreEqual() {
        ICAOCompanyCode icao1 = new ICAOCompanyCode("ABC");
        ICAOCompanyCode icao2 = new ICAOCompanyCode("ABC");

        assertEquals(icao1, icao2);
    }

    @Test
    public void ensureTwoICAOCodesWithDifferentValuesAreNotEqual() {
        ICAOCompanyCode icao1 = new ICAOCompanyCode("ABC");
        ICAOCompanyCode icao2 = new ICAOCompanyCode("BCD");

        assertNotEquals(icao1, icao2);
    }

    @Test
    public void ensureToStringReturnsICAOCode() {
        ICAOCompanyCode icao = new ICAOCompanyCode("ABC");

        assertEquals("ABC", icao.toString());
    }

    @Test
    public void ensureCompareToWorksCorrectly() {
        ICAOCompanyCode icao1 = new ICAOCompanyCode("ABC");
        ICAOCompanyCode icao2 = new ICAOCompanyCode("BCD");

        assertTrue(icao1.compareTo(icao2) < 0);
        assertTrue(icao2.compareTo(icao1) > 0);
        assertEquals(0, icao1.compareTo(new ICAOCompanyCode("ABC")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCannotCreateWithNullICAO() {
        new ICAOCompanyCode(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCannotCreateWithOneChar() {
        new ICAOCompanyCode("A");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCannotCreateWithFourChars() {
        new ICAOCompanyCode("ABCD");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCannotCreateWithNumbers() {
        new ICAOCompanyCode("A1B");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCannotCreateWithSpecialChars() {
        new ICAOCompanyCode("AB!");
    }
}