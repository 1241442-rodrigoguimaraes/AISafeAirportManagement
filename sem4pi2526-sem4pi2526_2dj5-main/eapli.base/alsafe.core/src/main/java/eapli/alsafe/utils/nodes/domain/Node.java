package eapli.alsafe.utils.nodes.domain;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;
import lombok.Getter;

import java.io.Serial;

@Entity
public class Node implements AggregateRoot<NodeId> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @Getter
    @EmbeddedId
    private NodeId id;

    @Getter
    @Embedded
    private Coordinate coordinates;

    @Getter
    @Embedded
    private Altitude altitude;

    public Node(final NodeId id, final Coordinate coordinates, final Altitude altitude) {
        if (id == null || coordinates == null || altitude == null)
            throw new IllegalArgumentException();

        this.id = id;
        this.coordinates = coordinates;
        this.altitude = altitude;
    }

    protected Node() {}

    public void moveTo(final Coordinate newCoordinates) {
        if (newCoordinates == null)
            throw new IllegalArgumentException();
        this.coordinates = newCoordinates;
    }

    public void changeAltitude(final Altitude newAltitude) {
        if (newAltitude == null)
            throw new IllegalArgumentException();
        this.altitude = newAltitude;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Node other = (Node) o;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean sameAs(final Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public NodeId identity() {
        return this.id;
    }

    @Override
    public String toString() {
        return String.format("%s, %s", coordinates, altitude);
    }
}
