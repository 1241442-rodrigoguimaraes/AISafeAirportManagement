package eapli.alsafe.persistence.impl.jpa;

import eapli.alsafe.Application;
import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.alsafe.weather.repositories.WeatherDataRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JpaWeatherDataRepository extends JpaAutoTxRepository<WeatherData, Long, Long>
        implements WeatherDataRepository {

    public JpaWeatherDataRepository(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    public JpaWeatherDataRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "id");
    }

    @Override
    public Iterable<WeatherData> findByAirControlAreaAndDateBetween(final AirControlArea area, final Calendar startDate, final Calendar endDate) {
        final Map<String, Object> params = new HashMap<>();
        params.put("area", area);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return match("e.airControlArea = :area AND e.weatherDate.date BETWEEN :startDate AND :endDate", params);
    }

    @Override
    public boolean duplicates(final WeatherData weatherData) {
        final Map<String, Object> params = new HashMap<>();
        params.put("area", weatherData.getAirControlArea());
        params.put("date", weatherData.getWeatherDate().getDate());
        return match("e.airControlArea = :area AND e.weatherDate.date = :date", params).iterator().hasNext();
    }

    @Override
    public Optional<WeatherData> getAllWeatherDataFromFCOCollaborator(CollaboratorFCO collaboratorFCO) {
        final Map<String, Object> params = new HashMap<>();
        params.put("areaID", collaboratorFCO.airControlArea().getId());
        return matchOne("e.airControlArea.airControlArea_id = :areaID", params);
    }
}
