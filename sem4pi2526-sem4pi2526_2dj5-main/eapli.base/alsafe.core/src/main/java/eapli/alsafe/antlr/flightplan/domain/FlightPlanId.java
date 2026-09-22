package eapli.alsafe.antlr.flightplan.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public final class FlightPlanId implements ValueObject, Comparable<FlightPlanId> {

    @Column(name = "FLIGHT_ID", length = 16)
    private String id;

    public FlightPlanId(final String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Flight plan id must not be blank.");
        }
        this.id = id.trim().toUpperCase();
    }

    protected FlightPlanId() {}

    public String id() {
        return id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FlightPlanId that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(final FlightPlanId other) {
        return id.compareTo(other.id);
    }

    @Override
    public String toString() {
        return id;
    }
}
