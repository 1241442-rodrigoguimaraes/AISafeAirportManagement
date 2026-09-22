package eapli.alsafe.flightPlan.application;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.flightPlan.repositories.FlightPlanRepository;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.alsafe.weather.repositories.WeatherDataRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.domain.Roles;

@UseCaseController
public class InsertWeatherDataInFlightController {

    private final AuthorizationService authz;
    private final FlightPlanRepository flightPlanRepository;
    private final WeatherDataRepository weatherDataRepository;

    public InsertWeatherDataInFlightController() {
        this(AuthzRegistry.authorizationService(),
                PersistenceContext.repositories().flightPlan(),
                PersistenceContext.repositories().weatherData());
    }

    public InsertWeatherDataInFlightController(final AuthorizationService authz,
                                               final FlightPlanRepository flightPlanRepository,
                                               final WeatherDataRepository weatherDataRepository) {
        if (authz == null || flightPlanRepository == null || weatherDataRepository == null) {
            throw new IllegalArgumentException();
        }
        this.authz = authz;
        this.flightPlanRepository = flightPlanRepository;
        this.weatherDataRepository = weatherDataRepository;
    }

    public Iterable<FlightPlan> myFlightPlans() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.PILOT);
        final SystemUser pilot = authenticatedPilot();
        return flightPlanRepository.findBySystemUser(pilot);
    }

    public Iterable<WeatherData> availableWeatherData() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.PILOT);
        return weatherDataRepository.findAll();
    }

    public FlightPlan insertWeatherData(final FlightPlan flightPlan, final WeatherData weatherData) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.PILOT);
        ensureFlightPlanBelongsToAuthenticatedPilot(flightPlan);

        final boolean wasTested = flightPlan.isTested();
        flightPlan.insertWeatherData(weatherData);
        final FlightPlan saved = flightPlanRepository.save(flightPlan);

        if (wasTested) {
            System.out.println("WARNING: This flight plan had been previously tested. "
                    + "The test result has been voided due to new weather data.");
        }

        return saved;
    }

    private SystemUser authenticatedPilot() {
        return authz.session()
                .orElseThrow(() -> new IllegalStateException("No authenticated session"))
                .authenticatedUser();
    }

    private void ensureFlightPlanBelongsToAuthenticatedPilot(final FlightPlan flightPlan) {
        if (!flightPlan.pilot().user().equals(authenticatedPilot())) {
            throw new IllegalArgumentException("Flight plan does not belong to the authenticated pilot.");
        }
    }
}
