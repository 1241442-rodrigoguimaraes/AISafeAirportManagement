package eapli.alsafe.utils.nodes.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public final class NodeId implements ValueObject, Serializable, Comparable<NodeId> {

    private static final long serialVersionUID = 1L;

    @Column(name = "NODE_ID_VAL")
    private Integer id;

    public NodeId(final Integer id) {
        if (id == null)
            throw new IllegalArgumentException("NodeId cannot be null");
        this.id = id;
    }

    protected NodeId() {}

    public Integer value() { return id; }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final NodeId nodeId = (NodeId) o;
        return id.equals(nodeId.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public int compareTo(NodeId o) {
        return id.compareTo(o.id);
    }
}