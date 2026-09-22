package eapli.alsafe.weather.repositories;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.framework.domain.repositories.DomainRepository;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.util.Calendar;
import java.util.Optional;

public interface WeatherDataRepository extends DomainRepository<Long, WeatherData> {

    Iterable<WeatherData> findByAirControlAreaAndDateBetween(AirControlArea area, Calendar startDate, Calendar   endDate);

    boolean duplicates(WeatherData weatherData);

    Optional<WeatherData> getAllWeatherDataFromFCOCollaborator(CollaboratorFCO collaboratorFCO);

    Iterable<WeatherData> findAll();
}
