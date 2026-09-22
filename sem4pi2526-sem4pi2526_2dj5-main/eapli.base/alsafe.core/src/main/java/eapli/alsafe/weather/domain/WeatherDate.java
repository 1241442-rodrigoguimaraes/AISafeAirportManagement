package eapli.alsafe.weather.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Calendar;

@Embeddable
@Getter
@EqualsAndHashCode
public class WeatherDate implements ValueObject, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Calendar date;

    public WeatherDate(final Calendar date) {
        Preconditions.nonNull(date, "Observation date cannot be null.");
        Preconditions.ensure(!date.after(Calendar.getInstance()), "Observation date cannot be in the future and must be possible.");

        this.date = date;
    }

    protected WeatherDate() {
        // for ORM
        this.date = null;
    }

    @Override
    public String toString() {
        return date.getTime().toString();
    }
}
