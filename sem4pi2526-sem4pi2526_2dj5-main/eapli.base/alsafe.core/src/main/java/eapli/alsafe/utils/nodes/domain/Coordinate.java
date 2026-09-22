package eapli.alsafe.utils.nodes.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.util.Locale;
import java.util.Objects;

@Embeddable
public final class Coordinate implements ValueObject {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "LATITUDE_VAL")
    private Double latitude;
    @Column(name = "LONGITUDE_VAL")
    private Double longitude;

    public Coordinate(final Double latitude, final Double longitude) {
        if (latitude == null || longitude == null)
            throw new IllegalArgumentException("Coordinates cannot be null");
        if (latitude < -90 || latitude > 90)
            throw new IllegalArgumentException("Invalid latitude: " + latitude);
        if (longitude < -180 || longitude > 180)
            throw new IllegalArgumentException("Invalid longitude: " + longitude);

        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected Coordinate() {}

    public Double latitude() { return latitude; }
    public Double longitude() { return longitude; }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Coordinate that = (Coordinate) o;
        return Objects.equals(latitude, that.latitude) && Objects.equals(longitude, that.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "[%.1f, %.1f]", latitude, longitude);
    }
}