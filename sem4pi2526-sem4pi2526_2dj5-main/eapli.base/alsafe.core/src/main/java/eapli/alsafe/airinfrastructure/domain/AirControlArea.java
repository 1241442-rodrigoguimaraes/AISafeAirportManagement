package eapli.alsafe.airinfrastructure.domain;

import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity
@EqualsAndHashCode
public class AirControlArea implements AggregateRoot<AirControlAreaID>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @Getter
    @EmbeddedId
    private AirControlAreaID id;

    @Getter
    @Column(unique = true)
    private String name;

    @Getter
    @Embedded
    private AirControlAreaBoundaries boundaries;

    @Getter
    @Embedded
    private AirControlAreaMinimumFuel minimumFuel;

    public AirControlArea(final AirControlAreaID id, final String name, final AirControlAreaBoundaries boundaries, final AirControlAreaMinimumFuel minimumFuel) {
        if (id == null || name == null || name.isBlank() ||boundaries == null || minimumFuel == null) throw new IllegalArgumentException();
        this.id = id;
        this.name = name;
        this.boundaries = boundaries;
        this.minimumFuel = minimumFuel;
    }

    protected AirControlArea() {
        // for ORM
    }

    public boolean overlaps(AirControlArea other) {
        List<Coordinate> thisBounds = this.boundaries.getBoundaries();
        double thisMinLat = thisBounds.stream().mapToDouble(Coordinate::latitude).min().orElseThrow();
        double thisMaxLat = thisBounds.stream().mapToDouble(Coordinate::latitude).max().orElseThrow();
        double thisMinLon = thisBounds.stream().mapToDouble(Coordinate::longitude).min().orElseThrow();
        double thisMaxLon = thisBounds.stream().mapToDouble(Coordinate::longitude).max().orElseThrow();

        List<Coordinate> otherBounds = other.boundaries.getBoundaries();
        double otherMinLat = otherBounds.stream().mapToDouble(Coordinate::latitude).min().orElseThrow();
        double otherMaxLat = otherBounds.stream().mapToDouble(Coordinate::latitude).max().orElseThrow();
        double otherMinLon = otherBounds.stream().mapToDouble(Coordinate::longitude).min().orElseThrow();
        double otherMaxLon = otherBounds.stream().mapToDouble(Coordinate::longitude).max().orElseThrow();

        boolean noOverlap = thisMaxLat <= otherMinLat || thisMinLat >= otherMaxLat
                || thisMaxLon <= otherMinLon || thisMinLon >= otherMaxLon;

        return !noOverlap;
    }

    @Override
    public boolean sameAs(Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public AirControlAreaID identity() {
        return this.id;
    }

    @Override
    public String toString() {
        return "AirControlArea : ID - " + id + ";\n" +
                "Name - " + name + ";\n" +
                "Boundaries - " + boundaries + ";\n" +
                "Minimum fuel quantity for landing (in litters) - " + minimumFuel + ".";
    }
}
