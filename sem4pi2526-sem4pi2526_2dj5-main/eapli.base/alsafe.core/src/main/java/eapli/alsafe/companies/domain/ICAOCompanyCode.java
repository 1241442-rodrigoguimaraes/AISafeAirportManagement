package eapli.alsafe.companies.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;

@Embeddable
@EqualsAndHashCode
public class ICAOCompanyCode implements ValueObject, Comparable<ICAOCompanyCode> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    @Column(name = "COMPANY_ICAO_CODE")
    private String icao;

    public ICAOCompanyCode(String icao) {
        if (icao == null) throw new IllegalArgumentException("Null ICAO code");
        if (!icao.matches("(?i)[A-Z]{2}") && !icao.matches("(?i)[A-Z]{3}")) throw new IllegalArgumentException("Invalid ICAO code");

        this.icao = icao;
    }

    protected ICAOCompanyCode() {
        // for ORM
    }

    @Override
    public int compareTo(ICAOCompanyCode o) {
        return icao.compareTo(o.icao);
    }

    @Override
    public String toString() {
        return icao;
    }
}
