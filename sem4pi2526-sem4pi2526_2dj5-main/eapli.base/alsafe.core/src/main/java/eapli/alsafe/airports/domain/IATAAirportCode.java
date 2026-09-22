package eapli.alsafe.airports.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.util.Objects;

@Embeddable
public final class IATAAirportCode implements ValueObject {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "IATA_CODE")
    private String code;

    public IATAAirportCode(String code) {
        if (code == null || !code.matches("(?i)[A-Z]{3}")) {
            throw new IllegalArgumentException("Invalid IATA Airport Code format. Must be 3 letters.");
        }
        this.code = code.toUpperCase();
    }

    protected IATAAirportCode() {}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        IATAAirportCode that = (IATAAirportCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    public static boolean validIATAFormat(String code) {
        if(code.matches("[a-zA-Z]{3}")){
            System.out.println("Valid IATA Airport Code format");
            return true;
        }
        System.out.println("Invalid IATA Airport Code format");
        return false;
    }

    @Override
    public String toString() {
        return code;
    }
}
