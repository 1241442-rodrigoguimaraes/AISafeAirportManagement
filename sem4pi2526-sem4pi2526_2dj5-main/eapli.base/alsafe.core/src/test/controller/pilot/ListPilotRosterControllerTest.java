package controller.pilot;

import eapli.alsafe.aircraftModelMagnement.domain.*;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.IATACompanyCode;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.alsafe.engineModelMagnement.Domain.*;
import eapli.alsafe.pilotmanagement.application.ListPilotRosterController;
import eapli.alsafe.pilotmanagement.application.PilotDTO;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.pilotmanagement.repositories.PilotRepository;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListPilotRosterControllerTest {

    private static class FakePilotRepository implements PilotRepository {
        private final List<Pilot> store = new ArrayList<>();

        @Override
        public Optional<Pilot> findByEmail(final CollaboratorEmail email) {
            return store.stream().filter(p -> p.email().equals(email)).findFirst();
        }

        @Override
        public Optional<Pilot> findBySystemUser(final SystemUser systemUser) {
            return store.stream().filter(p -> p.user().equals(systemUser)).findFirst();
        }

        @Override
        public Iterable<Pilot> findByCompany(final AirTransportCompany company) {
            return store.stream().filter(p -> p.company().equals(company)).toList();
        }

        @Override
        public Iterable<Pilot> findActiveByCompany(final AirTransportCompany company) {
            return store.stream().filter(p -> p.company().equals(company) && p.isActive()).toList();
        }

        @Override
        public Pilot save(final Pilot entity) {
            store.add(entity);
            return entity;
        }

        @Override
        public Iterable<Pilot> findAll() {
            return store;
        }

        @Override
        public Optional<Pilot> ofIdentity(final Long id) {
            return store.stream().filter(p -> id != null && id.equals(p.identity())).findFirst();
        }

        @Override
        public void delete(final Pilot entity) {
            store.remove(entity);
        }

        @Override
        public void deleteOfIdentity(final Long id) {
            ofIdentity(id).ifPresent(store::remove);
        }

        @Override
        public long count() {
            return store.size();
        }
    }

    private AuthorizationService authz;
    private FakePilotRepository pilotRepository;
    private CollaboratorRepositoryATCC collaboratorRepository;
    private ListPilotRosterController controller;

    private static AirTransportCompany company(final String iata, final String icao, final String name) {
        return new AirTransportCompany(new IATACompanyCode(iata), new ICAOCompanyCode(icao), name);
    }

    private static SystemUser user(final String email, final Role role) {
        return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with(email, "Password1", "Test", "User", email)
                .withRoles(role)
                .build();
    }

    private static aircraftModel aircraftModel(final String modelName) {
        final Maker maker = new Maker(new MakerName("Maker" + modelName), new MakerCountry("PT"));
        final engineModel engine = new engineModel(new engineModelName("Engine" + modelName), maker,
                engineType.TURBOFAN, new engineModelPower(1000), engineModelFuel.JET_A1,
                new engineModelEfficiency(0.9));
        return new aircraftModel(new aircraftModelName(modelName), maker, aircraftModelType.PASSENGER,
                engineType.TURBOFAN, new maximumRange(1000),
                new aircraftModelPhysicsData(1000, 2000, 1500, 500, 30000, 800, 50, 0.02, 1.2),
                Set.of(engine));
    }

    private static Pilot pilot(final String name, final String email, final AirTransportCompany company) {
        return new Pilot(name, CollaboratorEmail.valueOf(email), CollaboratorPhone.valueOf("912345678"),
                user(email, Roles.PILOT), company, Set.of(aircraftModel("A320")));
    }

    private void stubAuthenticatedAtcc(final AirTransportCompany company) {
        final SystemUser user = user("atcc@company.pt", Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        final CollaboratorATCC collaborator = new CollaboratorATCC("ATCC",
                CollaboratorEmail.valueOf("atcc@company.pt"), CollaboratorPhone.valueOf("912345679"), user,
                company);
        final UserSession session = mock(UserSession.class);
        when(session.authenticatedUser()).thenReturn(user);
        when(authz.session()).thenReturn(Optional.of(session));
        when(collaboratorRepository.findBySystemUser(user)).thenReturn(Optional.of(collaborator));
        when(collaboratorRepository.findCompanyByCollaborator(collaborator)).thenReturn(Optional.of(company));
    }

    @BeforeEach
    void setUp() {
        authz = mock(AuthorizationService.class);
        pilotRepository = new FakePilotRepository();
        collaboratorRepository = mock(CollaboratorRepositoryATCC.class);
        controller = new ListPilotRosterController(authz, pilotRepository, collaboratorRepository);
    }

    @Test
    void ensureAtccCanListOwnCompanyPilotRoster() {
        final AirTransportCompany company = company("TP", "TAP", "TAP Air");
        stubAuthenticatedAtcc(company);
        pilotRepository.save(pilot("John Pilot", "pilot@tap.pt", company));

        final List<PilotDTO> roster = new ArrayList<>();
        controller.activePilotRoster().forEach(roster::add);

        assertEquals(1, roster.size());
        assertEquals("John Pilot", roster.get(0).name());
        assertEquals("pilot@tap.pt", roster.get(0).email());
        assertEquals("TAP Air", roster.get(0).company());
        assertEquals("ACTIVE", roster.get(0).status());
        verify(authz).ensureAuthenticatedUserHasAnyOf(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
    }

    @Test
    void ensureRosterDoesNotIncludePilotsFromOtherCompanies() {
        final AirTransportCompany company = company("TP", "TAP", "TAP Air");
        final AirTransportCompany otherCompany = company("AA", "AAA", "American Airlines");
        stubAuthenticatedAtcc(company);
        pilotRepository.save(pilot("John Pilot", "pilot@tap.pt", company));
        pilotRepository.save(pilot("Other Pilot", "pilot@aa.pt", otherCompany));

        final List<PilotDTO> roster = new ArrayList<>();
        controller.activePilotRoster().forEach(roster::add);

        assertEquals(1, roster.size());
        assertEquals("pilot@tap.pt", roster.get(0).email());
    }

    @Test
    void ensureEmptyRosterReturnsEmptyList() {
        stubAuthenticatedAtcc(company("TP", "TAP", "TAP Air"));

        final List<PilotDTO> roster = new ArrayList<>();
        controller.activePilotRoster().forEach(roster::add);

        assertTrue(roster.isEmpty());
    }

    @Test
    void ensureMissingAtccProfileIsRejected() {
        final SystemUser user = user("atcc@company.pt", Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        final UserSession session = mock(UserSession.class);
        when(session.authenticatedUser()).thenReturn(user);
        when(authz.session()).thenReturn(Optional.of(session));
        when(collaboratorRepository.findBySystemUser(user)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> controller.activePilotRoster());
    }
}
