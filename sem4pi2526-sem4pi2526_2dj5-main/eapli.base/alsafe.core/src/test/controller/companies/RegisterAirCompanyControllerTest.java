package controller.companies;

import eapli.alsafe.companies.application.RegisterAirCompanyController;
import eapli.alsafe.companies.domain.AirCompanyBuilder;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.alsafe.companies.repositories.AirCompanyRepository;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RegisterAirCompanyControllerTest {

    private static class FakeAuthorizationService extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {}
    }

    private static class FakeAirCompanyRepository implements AirCompanyRepository {
        public final List<AirTransportCompany> store = new ArrayList<>();

        @SuppressWarnings("unchecked")
        @Override
        public AirTransportCompany save(final AirTransportCompany company) {
            store.add(company);
            return company;
        }

        @Override
        public Optional<AirTransportCompany> findByIATACode(final String iata) {
            return store.stream()
                    .filter(c -> c.getIata().getIata().equalsIgnoreCase(iata))
                    .findFirst();
        }

        @Override
        public Optional<AirTransportCompany> findByICAOCode(final String icao) {
            return store.stream()
                    .filter(c -> c.getIcao().getIcao().equalsIgnoreCase(icao))
                    .findFirst();
        }

        @Override
        public Optional<AirTransportCompany> findByName(final String name) {
            return store.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(name))
                    .findFirst();
        }

        @Override
        public Iterable<AirTransportCompany> findAll() {
            return store;
        }

        @Override
        public Optional<AirTransportCompany> ofIdentity(final ICAOCompanyCode id) {
            return Optional.empty();
        }

        @Override
        public void delete(final AirTransportCompany company) {
            store.remove(company);
        }

        @Override
        public void deleteOfIdentity(final ICAOCompanyCode id) {}

        @Override
        public long count() {
            return store.size();
        }

        @Override
        public boolean containsOfIdentity(final ICAOCompanyCode id) {
            return false;
        }
    }

    private FakeAirCompanyRepository repository;
    private AirCompanyBuilder builder;
    private RegisterAirCompanyController controller;

    private static final String IATA = "AB";
    private static final String ICAO = "ABC";
    private static final String NAME = "AirCompany";

    @BeforeEach
    public void setUp() {
        repository = new FakeAirCompanyRepository();
        builder = new AirCompanyBuilder();
        controller = new RegisterAirCompanyController(new FakeAuthorizationService(), builder, repository);
    }

    @Test
    public void ensureControllerCannotBeCreatedWithNullAuthz() {
        assertThrows(IllegalArgumentException.class, () -> new RegisterAirCompanyController(null, builder, repository));
    }

    @Test
    public void ensureControllerCannotBeCreatedWithNullBuilder() {
        assertThrows(IllegalArgumentException.class, () -> new RegisterAirCompanyController(new FakeAuthorizationService(), null, repository));
    }

    @Test
    public void ensureControllerCannotBeCreatedWithNullRepository() {
        assertThrows(IllegalArgumentException.class, () -> new RegisterAirCompanyController(new FakeAuthorizationService(), builder, null));
    }

    @Test
    public void ensureCreateAirTransportCompanySavesAndReturnsCompany() {
        AirTransportCompany result = controller.createAirTransportCompany(IATA, ICAO, NAME);

        assertNotNull(result);
        assertEquals(NAME, result.getName());
        assertEquals(1, repository.store.size());
    }

    @Test
    public void ensureCheckIATADoesNotThrowWhenIATAIsNew() {
        assertDoesNotThrow(() -> controller.checkIATA(IATA));
    }

    @Test
    public void ensureCheckIATAThrowsWhenIATAAlreadyExists() {
        controller.createAirTransportCompany(IATA, ICAO, NAME);

        assertThrows(IntegrityViolationException.class, () -> controller.checkIATA(IATA));
    }

    @Test
    public void ensureCheckICAODoesNotThrowWhenICAOIsNew() {
        assertDoesNotThrow(() -> controller.checkICAO(ICAO));
    }

    @Test
    public void ensureCheckICAOThrowsWhenICAOAlreadyExists() {
        controller.createAirTransportCompany(IATA, ICAO, NAME);

        assertThrows(IntegrityViolationException.class, () -> controller.checkICAO(ICAO));
    }
    @Test
    public void ensureCheckNameDoesNotThrowWhenNameIsNew() {
        assertDoesNotThrow(() -> controller.checkName(NAME));
    }

    @Test
    public void ensureCheckNameThrowsWhenNameAlreadyExists() {
        controller.createAirTransportCompany(IATA, ICAO, NAME);

        assertThrows(IntegrityViolationException.class, () -> controller.checkName(NAME));
    }

    @Test
    public void ensureCannotRegisterTwoCompaniesWithSameIATA() {
        controller.createAirTransportCompany(IATA, ICAO, NAME);

        assertThrows(IntegrityViolationException.class, () -> {
            controller.checkIATA(IATA);
            controller.createAirTransportCompany(IATA, "BCD", "OtherCompany"); });
    }

    @Test
    public void ensureCannotRegisterTwoCompaniesWithSameICAO() {
        controller.createAirTransportCompany(IATA, ICAO, NAME);

        assertThrows(IntegrityViolationException.class, () -> {
            controller.checkICAO(ICAO);
            controller.createAirTransportCompany("BC", ICAO, "OtherCompany"); });
    }

    @Test
    public void ensureCannotRegisterTwoCompaniesWithSameName() {
        controller.createAirTransportCompany(IATA, ICAO, NAME);

        assertThrows(IntegrityViolationException.class, () -> {
            controller.checkName(NAME);
            controller.createAirTransportCompany("BC", "BCD", NAME); });
    }

    @Test
    public void ensureCanRegisterTwoCompaniesWithDifferentData() {
        controller.createAirTransportCompany(IATA, ICAO, NAME);

        assertDoesNotThrow(() -> controller.createAirTransportCompany("BC", "BCD", "OtherCompany"));

        assertEquals(2, repository.store.size());
    }
}
