package eapli.alsafe.airports.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public final class ICAOAirportCode implements ValueObject, Comparable<ICAOAirportCode> {

    @Column(name = "ICAO_CODE")
    private String code;

    public ICAOAirportCode(String code) {
        if (code == null || !code.matches("(?i)[A-Z]{4}")) {
            throw new IllegalArgumentException("Invalid ICAO Airport Code format. Must be 4 letters.");
        }
        this.code = code.toUpperCase();
    }

    protected ICAOAirportCode() {}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ICAOAirportCode that = (ICAOAirportCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    public static boolean validICAOFormat(String code) {
        if(code.matches("[a-zA-Z]{4}")){
            System.out.println("Valid ICAO Airport Code format");
            return true;
        }
        System.out.println("Invalid ICAO Airport Code format");
        return false;
    }

    @Override
    public int compareTo(ICAOAirportCode o) {
        return code.compareTo(o.code);
    }

    @Override
    public String toString() {
        return code;
    }
}
