package eapli.alsafe.companies.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;

@Embeddable
@EqualsAndHashCode
public class IATACompanyCode implements ValueObject {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    @Column(name = "COMPANY_IATA_CODE")
    private String iata;

    public IATACompanyCode(String iata) {
        if (iata == null) throw new IllegalArgumentException("Null IATA code");
        if (!iata.matches("(?i)[A-Z]{2}")) throw new IllegalArgumentException("Invalid IATA code");

        this.iata = iata;
    }

    protected IATACompanyCode() {
        // for ORM
    }

    @Override
    public String toString() {
        return iata;
    }
}
