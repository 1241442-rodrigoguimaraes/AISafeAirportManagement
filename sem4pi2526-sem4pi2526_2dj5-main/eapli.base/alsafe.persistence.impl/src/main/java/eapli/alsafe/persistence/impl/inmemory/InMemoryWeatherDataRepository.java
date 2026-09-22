package eapli.alsafe.persistence.impl.inmemory;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.alsafe.weather.repositories.WeatherDataRepository;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Calendar;
import java.util.Optional;

public class InMemoryWeatherDataRepository extends InMemoryDomainRepository<WeatherData, Long>
        implements WeatherDataRepository {

    static {
        InMemoryInitializer.init();
    }

    @Override
    public Iterable<WeatherData> findByAirControlAreaAndDateBetween(final AirControlArea area, final Calendar startDate, final Calendar endDate) {
        return match(a -> a.getAirControlArea().equals(area)
                && !a.getWeatherDate().getDate().before(startDate)
                && !a.getWeatherDate().getDate().after(endDate));
    }

    @Override
    public boolean duplicates(final WeatherData weatherData) {
        return match(a -> a.getAirControlArea().equals(weatherData.getAirControlArea())
                && a.getWeatherDate().getDate().equals(weatherData.getWeatherDate().getDate())).iterator().hasNext();
    }

    @Override
    public  Optional<WeatherData> getAllWeatherDataFromFCOCollaborator(CollaboratorFCO collaboratorFCO) {
        return matchOne(a ->a.getAirControlArea().getId().equals(collaboratorFCO.airControlArea().getId()));
    }
}
