package eapli.alsafe.weather.application;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.alsafe.weather.domain.WeatherDataBuilder;
import eapli.alsafe.weather.domain.WeatherParsing.WeatherImportError;
import eapli.alsafe.weather.repositories.WeatherDataRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.Calendar;

public class RegisterWeatherDataController {

    private final AuthorizationService authz;
    private final WeatherDataRepository weatherDataRepository;
    private final AirControlAreaRepository airControlAreaRepository;

    public RegisterWeatherDataController() {
        this.authz = AuthzRegistry.authorizationService();
        this.weatherDataRepository = PersistenceContext.repositories().weatherData();
        this.airControlAreaRepository = PersistenceContext.repositories().areas();
    }

    public RegisterWeatherDataController(final AuthorizationService authz,
                                          final WeatherDataRepository weatherDataRepository,
                                          final AirControlAreaRepository airControlAreaRepository) {
        this.authz = authz;
        this.weatherDataRepository = weatherDataRepository;
        this.airControlAreaRepository = airControlAreaRepository;
    }

    public WeatherData registerWeatherData(final AirControlArea area, final Calendar date,
                                          final int windDirection, final double windSpeed) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.WEATHER_PERSON);

        final WeatherData newWeatherData = new WeatherDataBuilder()
                .with(area, date, windDirection, windSpeed)
                .build();

        if (weatherDataRepository.duplicates(newWeatherData)) {
            throw new WeatherImportException(
                    WeatherImportError.Category.DUPLICATE_ENTRY,
                    "Duplicate weather data entry for area " + area.getId().getId() + " on date " + date.getTime());
        }else return weatherDataRepository.save(newWeatherData);
    }

    public Iterable<AirControlArea> getAirControlAreas() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.WEATHER_PERSON, Roles.ADMIN);
        return airControlAreaRepository.findAll();
    }

    public AirControlArea getAirControlArea(final String id) {
        AirControlAreaID areaID = new AirControlAreaID(Long.parseLong(id.split("-")[1]));
        return airControlAreaRepository.findById(areaID).orElseThrow(() -> new IllegalArgumentException("Air Control Area with ID " + id + " not found."));
    }
}
