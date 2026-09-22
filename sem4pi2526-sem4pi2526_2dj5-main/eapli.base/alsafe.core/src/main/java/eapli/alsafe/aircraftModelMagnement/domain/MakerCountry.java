package eapli.alsafe.aircraftModelMagnement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.Set;

@Embeddable
public class MakerCountry implements ValueObject{

    private static final Set<String> ISO_COUNTRY_CODES = Set.of(java.util.Locale.getISOCountries());



    private String country ;

    protected MakerCountry(){

    }

    public MakerCountry(final String country) {
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("Maker country cannot be null or empty");
        }

        final String normalizedCountry = country.trim().toUpperCase();

        if (!ISO_COUNTRY_CODES.contains(normalizedCountry)) {
            throw new IllegalArgumentException("Invalid country code: " + country);
        }


        this.country = country;
    }

    public String country() {
        return country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MakerCountry that = (MakerCountry) o;
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