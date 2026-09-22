package eapli.alsafe.aircraftModelMagnement.domain;

import eapli.framework.domain.model.AggregateRoot;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "MAKERS")
public class Maker implements AggregateRoot<MakerName> {

    @EmbeddedId
    @AttributeOverride(name = "name", column = @Column(name = "maker_name", nullable = false, unique = true))
    private MakerName name;

    @Embedded
    @AttributeOverride(name = "country", column = @Column(name = "maker_country", nullable = false))
    private MakerCountry country;

    protected Maker() {
    }

    public Maker(final MakerName name, final MakerCountry country) {
        if (name == null) throw new IllegalArgumentException("Maker name cannot be null");
        if (country == null) throw new IllegalArgumentException("Maker country cannot be null");

        this.name = name;
        this.country = country;
    }

    @Override
    public boolean sameAs(final Object other) {
        if (this == other) return true;
        if (!(other instanceof Maker)) return false;
        final Maker that = (Maker) other;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public MakerName identity() {
        return this.name;
    }

    @Override
    public boolean hasIdentity(final MakerName name) {
        return this.name.equals(name);
    }

    @Override
    public String toString() {
        return "Maker: " + name;
    }
}
