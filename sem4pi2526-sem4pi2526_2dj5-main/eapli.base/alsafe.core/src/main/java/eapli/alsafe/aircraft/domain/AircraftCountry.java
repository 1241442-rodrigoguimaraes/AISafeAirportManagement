package eapli.alsafe.aircraft.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AircraftCountry implements Serializable {

    @Column(name = "registration_country", length = 120, nullable = false)
    private String country;

    protected AircraftCountry() {
    }

    public AircraftCountry(final String country) {
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Registration country cannot be null or blank");
        }
        final String c = country.trim();
        if (c.length() < 2) {
            throw new IllegalArgumentException("Registration country must be at least 2 characters");
        }
        this.country = c;
    }

    public String country() {
        return country;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AircraftCountry that = (AircraftCountry) o;
        return Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country);
    }

    @Override
    public String toString() {
        return country;
    }
}
