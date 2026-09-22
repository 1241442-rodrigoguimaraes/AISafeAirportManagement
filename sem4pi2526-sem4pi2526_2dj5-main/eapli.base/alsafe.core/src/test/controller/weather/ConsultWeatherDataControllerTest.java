package controller.weather;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaBoundaries;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaMinimumFuel;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryFCO;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.alsafe.weather.application.ConsultWeatherDataController;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.alsafe.weather.domain.WeatherDate;
import eapli.alsafe.weather.domain.WindCondition;
import eapli.alsafe.weather.repositories.WeatherDataRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConsultWeatherDataControllerTest {

    private static class FakeAuthorizationService extends AuthorizationService {
        private final boolean shouldThrow;

        FakeAuthorizationService(final boolean shouldThrow) {
            this.shouldThrow = shouldThrow;
        }

        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... actions) {
            if (shouldThrow) {
                throw new SecurityException("User is not authorized.");
            }
        }
    }

    private static class FakeWeatherDataRepository implements WeatherDataRepository {
        private final List<WeatherData> store = new ArrayList<>();

        @Override
        public WeatherData save(final WeatherData entity) {
            store.add(entity);
            return entity;
        }

        @Override
        public Iterable<WeatherData> findAll() {
            return store;
        }

        @Override
        public Optional<WeatherData> ofIdentity(final Long id) {
            return Optional.empty();
        }

        @Override
        public void delete(final WeatherData entity) {
        }

        @Override
        public void deleteOfIdentity(final Long id) {
        }

        @Override
        public long count() {
            return store.size();
        }

        @Override
        public Iterable<WeatherData> findByAirControlAreaAndDateBetween(final AirControlArea area, final Calendar startDate, final Calendar endDate) {
            return store.stream()
                    .filter(wd -> wd.getAirControlArea().equals(area))
                    .filter(wd -> !wd.getWeatherDate().getDate().before(startDate))
                    .filter(wd -> !wd.getWeatherDate().getDate().after(endDate))
                    .toList();
        }

        @Override
        public boolean duplicates(final WeatherData weatherData) {
            return store.stream().anyMatch(wd ->
                    wd.getAirControlArea().equals(weatherData.getAirControlArea())
                            && wd.getWeatherDate().getDate().equals(weatherData.getWeatherDate().getDate()));
        }

        @Override
        public Optional<WeatherData> getAllWeatherDataFromFCOCollaborator(final CollaboratorFCO collaboratorFCO) {
            return Optional.empty();
        }
    }

    private static class FakeAirControlAreaRepository implements AirControlAreaRepository {
        private final List<AirControlArea> store = new ArrayList<>();

        @Override
        public Iterable<AirControlArea> findAll() {
            return store;
        }

        @Override
        public Optional<AirControlArea> findById(final AirControlAreaID id) {
            return store.stream().filter(a -> a.identity().equals(id)).findFirst();
        }

        @Override
        public AirControlArea save(final AirControlArea entity) {
            store.add(entity);
            return entity;
        }

        @Override
        public Optional<AirControlArea> ofIdentity(final AirControlAreaID id) {
            return Optional.empty();
        }

        @Override
        public void delete(final AirControlArea entity) {
        }

        @Override
        public void deleteOfIdentity(final AirControlAreaID id) {
        }

        @Override
        public long count() {
            return store.size();
        }

        @Override
        public Optional<AirControlArea> findByCode(final String code) {
            return store.stream().filter(a -> a.identity().toString().equals(code)).findFirst();
        }
    }

    private ConsultWeatherDataController controller;
    private FakeWeatherDataRepository weatherRepo;
    private FakeAirControlAreaRepository areaRepo;
    private FakeCollaboratorRepositoryFCO collaboratorFcoRepo;
    private AirControlArea area;

    @BeforeEach
    void setUp() {
        weatherRepo = new FakeWeatherDataRepository();
        areaRepo = new FakeAirControlAreaRepository();
        collaboratorFcoRepo = new FakeCollaboratorRepositoryFCO();
        controller = new ConsultWeatherDataController(new FakeAuthorizationService(false), weatherRepo, areaRepo, collaboratorFcoRepo);

        area = new AirControlArea(new AirControlAreaID(1L), "Area 1",
                new AirControlAreaBoundaries(List.of(
                        new Coordinate(0.0, 0.0), new Coordinate(1.0, 0.0),
                        new Coordinate(0.0, 1.0), new Coordinate(1.0, 1.0))),
                new AirControlAreaMinimumFuel(100.0));
        areaRepo.save(area);
    }

    @Test
    void consultWithValidData_returnsResults() {
        Calendar day1 = Calendar.getInstance();
        day1.set(2025, Calendar.JANUARY, 1, 0, 0, 0);
        Calendar day2 = Calendar.getInstance();
        day2.set(2025, Calendar.JANUARY, 2, 0, 0, 0);

        weatherRepo.save(new WeatherData(area, new WeatherDate(day1), new WindCondition(180, 12.5)));
        weatherRepo.save(new WeatherData(area, new WeatherDate(day2), new WindCondition(90, 8.0)));

        Calendar start = Calendar.getInstance();
        start.set(2024, Calendar.DECEMBER, 31, 0, 0, 0);
        Calendar end = Calendar.getInstance();
        end.set(2025, Calendar.JANUARY, 3, 0, 0, 0);

        Iterable<WeatherData> results = controller.consultWeatherData(area, start, end);

        List<WeatherData> list = new ArrayList<>();
        results.forEach(list::add);
        assertEquals(2, list.size());
    }

    @Test
    void consultWithNoMatchingData_returnsEmptyList() {
        Calendar today = Calendar.getInstance();
        weatherRepo.save(new WeatherData(area, new WeatherDate(today), new WindCondition(180, 12.5)));

        Calendar future = Calendar.getInstance();
        future.add(Calendar.YEAR, 1);
        Calendar futureEnd = (Calendar) future.clone();
        futureEnd.add(Calendar.MONTH, 1);

        Iterable<WeatherData> results = controller.consultWeatherData(area, future, futureEnd);

        List<WeatherData> list = new ArrayList<>();
        results.forEach(list::add);
        assertTrue(list.isEmpty());
    }

    @Test
    void consultWithNullArea_throwsException() {
        Calendar today = Calendar.getInstance();
        assertThrows(IllegalArgumentException.class,
                () -> controller.consultWeatherData(null, today, today));
    }

    @Test
    void consultWithNullStartDate_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.consultWeatherData(area, null, Calendar.getInstance()));
    }

    @Test
    void consultWithNullEndDate_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.consultWeatherData(area, Calendar.getInstance(), null));
    }

    @Test
    void consultAsUnauthorizedUser_throwsSecurityException() {
        controller = new ConsultWeatherDataController(new FakeAuthorizationService(true), weatherRepo, areaRepo, collaboratorFcoRepo);
        Calendar today = Calendar.getInstance();
        assertThrows(SecurityException.class,
                () -> controller.consultWeatherData(area, today, today));
    }

    @Test
    void getAirControlAreas_returnsAreas() {
        Iterable<AirControlArea> areas = controller.getAirControlAreas();
        List<AirControlArea> list = new ArrayList<>();
        areas.forEach(list::add);
        assertEquals(1, list.size());
        assertEquals(area, list.get(0));
    }

    @Test
    void findByAreaAndDateBetween_boundaryInclusive_startDate() {
        Calendar today = Calendar.getInstance();
        weatherRepo.save(new WeatherData(area, new WeatherDate(today), new WindCondition(180, 12.5)));

        Iterable<WeatherData> results = controller.consultWeatherData(area, today, Calendar.getInstance());

        List<WeatherData> list = new ArrayList<>();
        results.forEach(list::add);
        assertEquals(1, list.size());
    }

    @Test
    void findByAreaAndDateBetween_differentArea_notIncluded() {
        AirControlArea otherArea = new AirControlArea(new AirControlAreaID(2L), "Area 2",
                new AirControlAreaBoundaries(List.of(
                        new Coordinate(0.0, 0.0), new Coordinate(1.0, 0.0),
                        new Coordinate(0.0, 1.0), new Coordinate(1.0, 1.0))),
                new AirControlAreaMinimumFuel(200.0));

        Calendar today = Calendar.getInstance();
        weatherRepo.save(new WeatherData(area, new WeatherDate(today), new WindCondition(180, 12.5)));

        Calendar start = (Calendar) today.clone();
        start.add(Calendar.DAY_OF_MONTH, -1);
        Calendar end = (Calendar) today.clone();
        end.add(Calendar.DAY_OF_MONTH, 1);

        Iterable<WeatherData> results = controller.consultWeatherData(otherArea, start, end);

        List<WeatherData> list = new ArrayList<>();
        results.forEach(list::add);
        assertTrue(list.isEmpty());
    }

    @Test
    void duplicates_withExistingEntry_returnsTrue() {
        Calendar today = Calendar.getInstance();
        WeatherData wd = new WeatherData(area, new WeatherDate(today), new WindCondition(180, 12.5));
        weatherRepo.save(wd);

        assertTrue(weatherRepo.duplicates(wd));
    }

    @Test
    void duplicates_withDifferentArea_returnsFalse() {
        AirControlArea otherArea = new AirControlArea(new AirControlAreaID(2L), "Area 2",
                new AirControlAreaBoundaries(List.of(
                        new Coordinate(0.0, 0.0), new Coordinate(1.0, 0.0),
                        new Coordinate(0.0, 1.0), new Coordinate(1.0, 1.0))),
                new AirControlAreaMinimumFuel(200.0));

        Calendar today = Calendar.getInstance();
        weatherRepo.save(new WeatherData(area, new WeatherDate(today), new WindCondition(180, 12.5)));

        assertFalse(weatherRepo.duplicates(new WeatherData(otherArea, new WeatherDate(today), new WindCondition(90, 8.0))));
    }

    @Test
    void duplicates_withDifferentDate_returnsFalse() {
        Calendar day1 = Calendar.getInstance();
        day1.set(2025, Calendar.FEBRUARY, 1, 0, 0, 0);
        Calendar day2 = Calendar.getInstance();
        day2.set(2025, Calendar.FEBRUARY, 2, 0, 0, 0);

        weatherRepo.save(new WeatherData(area, new WeatherDate(day1), new WindCondition(180, 12.5)));

        assertFalse(weatherRepo.duplicates(new WeatherData(area, new WeatherDate(day2), new WindCondition(180, 12.5))));
    }

    @Test
    void duplicates_withNoEntries_returnsFalse() {
        Calendar today = Calendar.getInstance();
        assertFalse(weatherRepo.duplicates(new WeatherData(area, new WeatherDate(today), new WindCondition(180, 12.5))));
    }
}

class FakeCollaboratorRepositoryFCO implements CollaboratorRepositoryFCO {
    @Override
    public Optional<CollaboratorFCO> findByEmail(final CollaboratorEmail email) { return Optional.empty(); }
    @Override
    public Optional<CollaboratorFCO> findBySystemUser(final SystemUser systemUser) { return Optional.empty(); }
    @Override
    public Iterable<CollaboratorFCO> findActiveByCustomerArea(final AirControlArea area) { return List.of(); }
    @Override
    public <S extends CollaboratorFCO> S save(final S entity) { return entity; }
    @Override
    public Iterable<CollaboratorFCO> findAll() { return List.of(); }
    @Override
    public Optional<CollaboratorFCO> ofIdentity(final Long id) { return Optional.empty(); }
    @Override
    public void delete(final CollaboratorFCO entity) {}
    @Override
    public void deleteOfIdentity(final Long id) {}
    @Override
    public long count() { return 0; }
}
