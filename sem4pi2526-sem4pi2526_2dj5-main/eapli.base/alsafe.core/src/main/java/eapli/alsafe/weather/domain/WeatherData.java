package eapli.alsafe.weather.domain;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Entity
public class WeatherData implements AggregateRoot<Long>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Long version;

    @Getter
    @ManyToOne(optional = false)
    private AirControlArea airControlArea;

    @Getter
    @Embedded
    private WeatherDate weatherDate;

    @Getter
    @Embedded
    private WindCondition windCondition;

    public WeatherData(final AirControlArea airControlArea, final WeatherDate weatherDate, final WindCondition windCondition) {
        Preconditions.nonNull(airControlArea, "Air control area cannot be null.");
        Preconditions.nonNull(weatherDate, "Weather date cannot be null.");
        Preconditions.nonNull(windCondition, "Wind condition cannot be null.");

        this.airControlArea = airControlArea;
        this.weatherDate = weatherDate;
        this.windCondition = windCondition;
    }

    protected WeatherData() {
        // for ORM
    }

    @Override
    public boolean sameAs(Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public Long identity() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("WeatherData for Area: %s, Date: %s, %s",
                airControlArea.getName(), weatherDate, windCondition);
    }
}
