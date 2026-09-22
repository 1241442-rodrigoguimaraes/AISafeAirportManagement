package eapli.alsafe.companies.domain;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Entity
@EqualsAndHashCode(of = "icao")
public class AirTransportCompany implements AggregateRoot<ICAOCompanyCode>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @Getter
    @Setter
    @Column(unique = true)
    private IATACompanyCode iata;

    @Getter
    @Setter
    @EmbeddedId
    private ICAOCompanyCode icao;

    @Getter
    @Column(unique = true)
    private String name;

    public AirTransportCompany(final IATACompanyCode iata, final ICAOCompanyCode icao, final String name) {
        if (iata == null || icao == null || name == null || name.isBlank()) throw new IllegalArgumentException();

        this.iata = iata;
        this.icao = icao;
        this.name = name;
    }

    protected AirTransportCompany() {
        // for ORM
    }

    @Override
    public boolean sameAs(Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public ICAOCompanyCode identity() {
        return this.icao;
    }

    @Override
    public String toString() {
        return "Air Transport Company : Name - " + name + ";\n" +
                "ICAO - " + icao + ";\n" +
                "IATA - " + iata + ".";
    }
}
