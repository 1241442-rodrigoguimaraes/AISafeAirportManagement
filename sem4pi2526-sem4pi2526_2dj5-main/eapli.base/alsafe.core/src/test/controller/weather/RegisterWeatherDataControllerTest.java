package controller.weather;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaBoundaries;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaMinimumFuel;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.weather.application.RegisterWeatherDataController;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.alsafe.weather.repositories.WeatherDataRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RegisterWeatherDataControllerTest {

    private static class FakeAuthorizationService extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... actions) {}
    }

    private static class FakeWeatherDataRepository implements WeatherDataRepository {
        private final List<WeatherData> store = new ArrayList<>();

        @Override
        public WeatherData save(WeatherData entity) {
            store.add(entity);
            return entity;
        }

        @Override
        public Iterable<WeatherData> findAll() { return store; }

        @Override
        public Optional<WeatherData> ofIdentity(Long id) { return Optional.empty(); }

        @Override
        public void delete(WeatherData entity) {}

        @Override
        public void deleteOfIdentity(Long id) {}

        @Override
        public long count() { return store.size(); }

        @Override
        public Iterable<WeatherData> findByAirControlAreaAndDateBetween(AirControlArea area, Calendar startDate, Calendar endDate) {
            return null;
        }

        @Override
        public boolean duplicates(WeatherData weatherData) {
            return false;
        }

        @Override
        public Optional<WeatherData> getAllWeatherDataFromFCOCollaborator(CollaboratorFCO collaboratorFCO) {
            return Optional.empty();
        }
    }

    private static class FakeAirControlAreaRepository implements AirControlAreaRepository {
        private final List<AirControlArea> store = new ArrayList<>();

        @Override
        public Iterable<AirControlArea> findAll() { return store; }

        @Override
        public Optional<AirControlArea> findById(AirControlAreaID id) { return Optional.empty(); }

        @Override
        public AirControlArea save(AirControlArea entity) {
            store.add(entity);
            return entity;
        }

        @Override
        public Optional<AirControlArea> ofIdentity(AirControlAreaID id) { return Optional.empty(); }

        @Override
        public void delete(AirControlArea entity) {}

        @Override
        public void deleteOfIdentity(AirControlAreaID id) {}

        @Override
        public long count() { return store.size(); }

        @Override
        public Optional<AirControlArea> findByCode(String code) { return Optional.empty(); }
    }

    private RegisterWeatherDataController controller;
    private FakeWeatherDataRepository weatherRepo;
    private FakeAirControlAreaRepository areaRepo;

    @BeforeEach
    void setUp() {
        weatherRepo = new FakeWeatherDataRepository();
        areaRepo = new FakeAirControlAreaRepository();
        controller = new RegisterWeatherDataController(new FakeAuthorizationService(), weatherRepo, areaRepo);
    }

    @Test
    void ensureRegisterWeatherDataWorks() {
        AirControlArea area = new AirControlArea(new AirControlAreaID(1L), "Area 1",
                new AirControlAreaBoundaries(List.of(new Coordinate(0.0, 0.0), new Coordinate(1.0, 0.0), new Coordinate(0.0, 1.0), new Coordinate(1.0, 1.0))),
                new AirControlAreaMinimumFuel(100.0));

        Calendar now = Calendar.getInstance();
        WeatherData result = controller.registerWeatherData(area, now, 180, 10.5);

        assertNotNull(result);
        assertEquals(1, weatherRepo.count());
        assertEquals(area, result.getAirControlArea());
        assertEquals(180, result.getWindCondition().getDirection());
        assertEquals(10.5, result.getWindCondition().getSpeed());
    }
}
