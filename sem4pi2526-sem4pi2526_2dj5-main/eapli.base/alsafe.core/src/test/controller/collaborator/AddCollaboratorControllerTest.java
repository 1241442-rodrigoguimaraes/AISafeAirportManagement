package controller.collaborator;

import eapli.alsafe.airinfrastructure.domain.*;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.collaboratormanagement.application.AddCollaboratorController;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryFCO;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.IATACompanyCode;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.alsafe.companies.repositories.AirCompanyRepository;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AddCollaboratorControllerTest {

    private static class FakeAuthorizationService extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... actions) {}
    }

    private static class FakeUserManagementService extends UserManagementService {
        public FakeUserManagementService() {
            super(null, null, null);
        }

        @Override
        public SystemUser registerNewUser(String username, String password, String firstName, String lastName, String email, Set<Role> roles) {
            final SystemUserBuilder userBuilder = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder());
            return userBuilder.with(username, password, firstName, lastName, email).withRoles(roles).build();
        }
    }

    private static class FakeCollaboratorRepositoryFCO implements CollaboratorRepositoryFCO {
        private final List<CollaboratorFCO> store = new ArrayList<>();

        @Override
        public CollaboratorFCO save(CollaboratorFCO entity) {
            store.add(entity);
            return entity;
        }

        @Override
        public Iterable<CollaboratorFCO> findAll() { return store; }

        @Override
        public Optional<CollaboratorFCO> ofIdentity(Long id) { return Optional.empty(); }

        @Override
        public void delete(CollaboratorFCO entity) {}

        @Override
        public void deleteOfIdentity(Long id) {}

        @Override
        public long count() { return store.size(); }

        @Override
        public Optional<CollaboratorFCO> findBySystemUser(final SystemUser user) {
            return Optional.empty();
        }

        @Override
        public Optional<CollaboratorFCO> findByEmail(CollaboratorEmail email) { return Optional.empty(); }

        @Override
        public Iterable<CollaboratorFCO> findActiveByCustomerArea(final AirControlArea area) {
            return List.of();
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

    private static class FakeAirCompanyRepository implements AirCompanyRepository {
        private final List<AirTransportCompany> store = new ArrayList<>();

        @Override
        public Optional<AirTransportCompany> findByName(String name) { return Optional.empty(); }

        @Override
        public Optional<AirTransportCompany> findByIATACode(String iata) { return Optional.empty(); }

        @Override
        public Optional<AirTransportCompany> findByICAOCode(String icao) { return Optional.empty(); }


        @Override
        public AirTransportCompany save(AirTransportCompany entity) {
            store.add(entity);
            return entity;
        }

        @Override
        public Iterable<AirTransportCompany> findAll() { return store; }

        @Override
        public Optional<AirTransportCompany> ofIdentity(ICAOCompanyCode id) { return Optional.empty(); }

        @Override
        public void delete(AirTransportCompany entity) {}

        @Override
        public void deleteOfIdentity(ICAOCompanyCode id) {}

        @Override
        public long count() { return store.size(); }
    }

    private static class FakeCollaboratorRepositoryATCC implements CollaboratorRepositoryATCC {
        private final List<CollaboratorATCC> store = new ArrayList<>();

        @Override
        public CollaboratorATCC save(CollaboratorATCC entity) {
            store.add(entity);
            return entity;
        }

        @Override
        public Iterable<CollaboratorATCC> findAll() { return store; }

        @Override
        public Optional<CollaboratorATCC> ofIdentity(Long id) { return Optional.empty(); }

        @Override
        public void delete(CollaboratorATCC entity) {}

        @Override
        public void deleteOfIdentity(Long id) {}

        @Override
        public long count() { return store.size(); }

        @Override
        public Optional<CollaboratorATCC> findByEmail(CollaboratorEmail email) { return Optional.empty(); }

        @Override
        public Optional<CollaboratorATCC> findBySystemUser(SystemUser user) { return Optional.empty(); }

        @Override
        public Optional<AirTransportCompany> findCompanyByCollaborator(CollaboratorATCC collaborator) {
            return Optional.empty();
        }

        @Override
        public Iterable<CollaboratorATCC> findActiveByCompany(AirTransportCompany company) {
            return List.of();
        }
    }

    private AddCollaboratorController controller;
    private FakeCollaboratorRepositoryFCO collaboratorRepo;
    private FakeCollaboratorRepositoryATCC collaboratorRepoATCC;
    private FakeAirControlAreaRepository areaRepo;
    private FakeAirCompanyRepository companyRepo;

    @BeforeEach
    void setUp() {
        collaboratorRepo = new FakeCollaboratorRepositoryFCO();
        collaboratorRepoATCC = new FakeCollaboratorRepositoryATCC();
        areaRepo = new FakeAirControlAreaRepository();
        companyRepo = new FakeAirCompanyRepository();
        controller = new AddCollaboratorController(new FakeAuthorizationService(), new FakeUserManagementService(), collaboratorRepo, collaboratorRepoATCC, areaRepo, companyRepo);
    }

    @Test
    void ensureAddCollaboratorFCOWorks() {
        AirControlArea area = new AirControlArea(new AirControlAreaID(1L), "Area 1", 
                new AirControlAreaBoundaries(List.of(new Coordinate(0.0, 0.0), new Coordinate(1.0, 0.0), new Coordinate(0.0, 1.0), new Coordinate(1.0, 1.0))),
                new AirControlAreaMinimumFuel(100.0));

        CollaboratorFCO result = controller.addCollaboratorFCO(area, "John Doe", "john@email.com", "912345678","Password123");
        
        assertNotNull(result);
        assertEquals(1, collaboratorRepo.count());
    }

    @Test
    void ensureAddCollaboratorATCCWorks() {
        AirTransportCompany company = new AirTransportCompany(new IATACompanyCode("AA"), new ICAOCompanyCode("AAA"), "American Airlines");

        CollaboratorATCC result = controller.addCollaboratorATCC("John Doe", "john@email.com", "912345678","Password123", company);

        assertNotNull(result);
        assertEquals(1, collaboratorRepoATCC.count());
    }

    @Test
    void ensureAddCollaboratorFCOFailsWithNullArea() {
        assertThrows(IllegalArgumentException.class, () -> 
            controller.addCollaboratorFCO(null, "John Doe", "john@email.com", "912345678", "Password123")
        );
    }

    @Test
    void ensureGetAirControlAreasWorks() {
        controller.getAirControlAreas();
    }
}
