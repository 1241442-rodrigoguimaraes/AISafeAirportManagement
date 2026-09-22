package eapli.alsafe.engineModelMagnement.Domain;

import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;

@Entity
@Getter
@Table(name = "engine_model")
@EqualsAndHashCode(of = "id")
public class engineModel implements AggregateRoot< engineModelId  > {
    @Serial
    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "modelName.name", column = @Column(name = "model_name_id")),
            @AttributeOverride(name = "makerName.name", column = @Column(name = "maker_name_id"))
    })
    private engineModelId id;

    @Embedded
    private engineModelName name;

    @ManyToOne(optional = false)
    private Maker maker;

    @Enumerated(EnumType.STRING)
    private engineType type;

    @Embedded
    private engineModelPower power;

    @Enumerated(EnumType.STRING)
    private engineModelFuel fuel;

    @Embedded
    private engineModelEfficiency efficiency;

    protected engineModel() {

    }

    public engineModel(final engineModelName name,
                       final Maker maker,
                       final engineType type,
                       final engineModelPower power,
                       final engineModelFuel fuel,
                       final engineModelEfficiency efficiency) {

        if (name == null) throw new IllegalArgumentException("Name cannot be null");
        if (maker == null) throw new IllegalArgumentException("Maker cannot be null");
        if (type == null) throw new IllegalArgumentException("Type cannot be null");
        if (power == null) throw new IllegalArgumentException("Power cannot be null");
        if (fuel == null) throw new IllegalArgumentException("Fuel cannot be null");
        if (efficiency == null) throw new IllegalArgumentException("Efficiency cannot be null");

        this.id = new engineModelId(name, maker.identity());
        this.name = name;
        this.maker = maker;
        this.type = type;
        this.power = power;
        this.fuel = fuel;
        this.efficiency = efficiency;
    }

    public engineModelName name() {
        return this.name;
    }

    public Maker maker() {
        return this.maker;
    }

    @Override
    public engineModelId identity() {
        return this.id;
    }

    @Override
    public boolean sameAs(final Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public String toString() {
        return String.format("%s - %s [%s, %s, %s, %s]",
                this.maker,
                this.name,
                this.type,
                this.power,
                this.fuel,
                this.efficiency);
    }
}
