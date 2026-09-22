package eapli.alsafe.utils.nodes.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.util.Objects;

@Embeddable
public final class Altitude implements ValueObject {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "METERS_VAL")
    private Double meters;

    public Altitude(final Double meters) {
        if (meters == null)
            throw new IllegalArgumentException("Altitude cannot be null");
        if (meters < -500)
            throw new IllegalArgumentException("Invalid altitude: " + meters);

        this.meters = meters;
    }

    protected Altitude() {}

    public Double meters() { return meters; }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Altitude altitude = (Altitude) o;
        return Objects.equals(meters, altitude.meters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meters);
    }

    @Override
    public String toString() {
        return meters + "m";
    }
}
