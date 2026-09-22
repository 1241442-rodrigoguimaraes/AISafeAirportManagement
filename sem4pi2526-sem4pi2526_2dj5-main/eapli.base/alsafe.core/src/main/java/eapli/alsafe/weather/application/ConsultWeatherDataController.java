package eapli.alsafe.weather.application;



import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryFCO;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.alsafe.weather.repositories.WeatherDataRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.validations.Preconditions;

import java.util.Calendar;
import java.util.Optional;

public class ConsultWeatherDataController {

    private final AuthorizationService authz;
    private final WeatherDataRepository weatherDataRepository;
    private final AirControlAreaRepository airControlAreaRepository;
    private final CollaboratorRepositoryFCO collaboratorRepositoryFCO;

    public ConsultWeatherDataController() {
        authz = AuthzRegistry.authorizationService();
        weatherDataRepository = PersistenceContext.repositories().weatherData();
        airControlAreaRepository = PersistenceContext.repositories().areas();
        collaboratorRepositoryFCO = PersistenceContext.repositories().collaboratorsFCO();
    }

    public ConsultWeatherDataController(AuthorizationService authz, WeatherDataRepository weatherDataRepository, AirControlAreaRepository airControlAreaRepository, CollaboratorRepositoryFCO collaboratorRepositoryFCO) {
        this.authz = authz;
        this.weatherDataRepository = weatherDataRepository;
        this.airControlAreaRepository = airControlAreaRepository;
        this.collaboratorRepositoryFCO = collaboratorRepositoryFCO;
    }

    public Iterable<AirControlArea> getAirControlAreas() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.WEATHER_PERSON, Roles.PILOT, Roles.FLIGHT_CONTROL_OPERATOR);
        return airControlAreaRepository.findAll();
    }

    public Iterable<WeatherData> consultWeatherData(final AirControlArea area, final Calendar startDate, final Calendar endDate) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.WEATHER_PERSON, Roles.PILOT, Roles.FLIGHT_CONTROL_OPERATOR);

        Preconditions.nonNull(area, "Air control area cannot be null.");
        Preconditions.nonNull(startDate, "Start date cannot be null.");
        Preconditions.nonNull(endDate, "End date cannot be null.");

        return weatherDataRepository.findByAirControlAreaAndDateBetween(area, startDate, endDate);
    }

    public boolean isFCORole(){
        return authz.isAuthenticatedUserAuthorizedTo(Roles.FLIGHT_CONTROL_OPERATOR);
    }

    public Optional<WeatherData> getAllWeatherDataFromFCOCollaborator() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.FLIGHT_CONTROL_OPERATOR);
        final var session = authz.session().orElseThrow(
                () -> new IllegalStateException("No authenticated session"));
        final Optional<CollaboratorFCO> collaboratorFCO = collaboratorRepositoryFCO.findBySystemUser(session.authenticatedUser());
        final CollaboratorFCO fco = collaboratorFCO.orElseThrow(
                () -> new IllegalStateException("No CollaboratorFCO found for current user"));
        return weatherDataRepository.getAllWeatherDataFromFCOCollaborator(fco);
    }
}
