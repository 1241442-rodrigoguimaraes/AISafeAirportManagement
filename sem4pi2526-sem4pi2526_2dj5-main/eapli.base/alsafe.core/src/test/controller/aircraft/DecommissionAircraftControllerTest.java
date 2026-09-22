package controller.aircraft;

import eapli.alsafe.aircraft.application.DecommissionAircraftController;
import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.aircraft.domain.AircraftCountry;
import eapli.alsafe.aircraft.domain.CabinConfiguration;
import eapli.alsafe.aircraft.domain.CrewElement;
import eapli.alsafe.aircraft.domain.MaintenanceStatus;
import eapli.alsafe.aircraft.domain.RegistrationID;
import eapli.alsafe.aircraft.repositories.AircraftRepository;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelName;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelPhysicsData;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelType;
import eapli.alsafe.aircraftModelMagnement.domain.maximumRange;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.IATACompanyCode;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.alsafe.companies.repositories.AirCompanyRepository;
import eapli.alsafe.engineModelMagnement.Domain.engineModel;
import eapli.alsafe.engineModelMagnement.Domain.engineModelEfficiency;
import eapli.alsafe.engineModelMagnement.Domain.engineModelFuel;
import eapli.alsafe.engineModelMagnement.Domain.engineModelName;
import eapli.alsafe.engineModelMagnement.Domain.engineModelPower;
import eapli.alsafe.engineModelMagnement.Domain.engineType;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.domain.MakerCountry;
import eapli.alsafe.aircraftModelMagnement.domain.MakerName;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DecommissionAircraftControllerTest {

    private AuthorizationService authz;
    private AircraftRepository aircraftRepository;
    private CollaboratorRepositoryATCC collaboratorRepository;
    private AirCompanyRepository companyRepository;
    private DecommissionAircraftController controller;

    private static Maker dummyMaker() {
        return new Maker(new MakerName("MakerCo"), new MakerCountry("PT"));
    }

    private static aircraftModel dummyModel() {
        final aircraftModelName name = new aircraftModelName("ModelX");
        final Maker maker = dummyMaker();
        final maximumRange range = new maximumRange(1000);
        final aircraftModelPhysicsData physics = new aircraftModelPhysicsData(
                1000, 2000, 1500, 500, 12000, 800, 100, 0.02, 0.5);
        final engineModelPower power = new engineModelPower(200.0);
        final engineModelEfficiency efficiency = new engineModelEfficiency(300.0);
        final engineModel eng = new engineModel(new engineModelName("Eng1"), maker, engineType.TURBOPROP, power,
                engineModelFuel.JET_A1, efficiency);
        final Set<engineModel> engines = new HashSet<>();
        engines.add(eng);
        return new aircraftModel(name, maker, aircraftModelType.MIXED, engineType.TURBOPROP, range, physics, engines);
    }

    private static AirTransportCompany company(final String iata, final String icao, final String name) {
        return new AirTransportCompany(new IATACompanyCode(iata), new ICAOCompanyCode(icao), name);
    }

    private static Aircraft aircraftForCompany(final RegistrationID reg, final AirTransportCompany c) {
        final aircraftModel model = dummyModel();
        final int max = model.maxPassengerSeats();
        return Aircraft.create(model, reg, new AircraftCountry("PT"),
                new CabinConfiguration(Math.min(10, max), 0, 0, max),
                List.of(new CrewElement("CREW_1")), c);
    }

    private static SystemUser dummyUser(final String username) {
        return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with(username, "Password1", "First", "Last", username + "@alsafe.com")
                .withRoles(Set.of(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR).toArray(new Role[0]))
                .build();
    }

    private void stubSessionAndCollaborator(final SystemUser user, final CollaboratorATCC collaborator,
                                            final AirTransportCompany company) {
        final UserSession session = mock(UserSession.class);
        when(session.authenticatedUser()).thenReturn(user);
        when(authz.session()).thenReturn(Optional.of(session));
        when(collaboratorRepository.findBySystemUser(user)).thenReturn(Optional.of(collaborator));
        when(collaboratorRepository.findCompanyByCollaborator(collaborator)).thenReturn(Optional.of(company)); // <-- THIS IS MISSING
    }

    @BeforeEach
    void setUp() {
        authz = mock(AuthorizationService.class);
        aircraftRepository = mock(AircraftRepository.class);
        collaboratorRepository = mock(CollaboratorRepositoryATCC.class);
        companyRepository = mock(AirCompanyRepository.class);
        when(aircraftRepository.save(any(Aircraft.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        controller = new DecommissionAircraftController(authz, aircraftRepository, collaboratorRepository, companyRepository);
    }

    @Test
    void activeFleetForCurrentCompany_excludesDecommissioned() {
        final SystemUser user = dummyUser("collab1");
        final AirTransportCompany mine = company("AA", "AAA", "Mine");
        final CollaboratorATCC collab = new CollaboratorATCC("N", CollaboratorEmail.valueOf("n@x.com"),
                CollaboratorPhone.valueOf("+351911111111"), user,mine);
        stubSessionAndCollaborator(user, collab, mine);

        final RegistrationID r1 = RegistrationID.valueOf("CS-AAA");
        final RegistrationID r2 = RegistrationID.valueOf("CS-BBB");
        final Aircraft active = aircraftForCompany(r1, mine);
        final Aircraft retired = aircraftForCompany(r2, mine);
        retired.decommission();

        when(aircraftRepository.findByCompany(mine)).thenReturn(List.of(active, retired));

        final List<Aircraft> list = controller.activeFleetForCurrentCompany();
        assertEquals(1, list.size());
        assertEquals(r1, list.get(0).identity());
        verify(authz).ensureAuthenticatedUserHasAnyOf(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR, Roles.ADMIN);
    }

    @Test
    void decommissionAircraft_sameCompany_persistsDecommissioned() {
        final AirTransportCompany mine = company("BB", "BBB", "Mine2");
        final SystemUser user = dummyUser("collab2");
        final CollaboratorATCC collab = new CollaboratorATCC("N2", CollaboratorEmail.valueOf("n2@x.com"),
                CollaboratorPhone.valueOf("+351922222222"), user, mine);
        stubSessionAndCollaborator(user, collab, mine);

        final RegistrationID reg = RegistrationID.valueOf("CS-CCC");
        final Aircraft ac = aircraftForCompany(reg, mine);
        when(aircraftRepository.ofIdentity(reg)).thenReturn(Optional.of(ac));

        final Aircraft out = controller.decommissionAircraft(reg);

        assertEquals(MaintenanceStatus.DECOMMISSIONED, out.maintenanceStatus());
        verify(aircraftRepository).save(ac);
    }

    @Test
    void decommissionAircraft_otherCompany_throwsSecurityException() {
        final AirTransportCompany mine = company("CC", "CCC", "Mine3");
        final AirTransportCompany other = company("DD", "DDD", "Other");

        final SystemUser user = dummyUser("collab3");
        final CollaboratorATCC collab = new CollaboratorATCC("N3", CollaboratorEmail.valueOf("n3@x.com"),
                CollaboratorPhone.valueOf("+351933333333"), user, mine);

        final UserSession session = mock(UserSession.class);
        when(session.authenticatedUser()).thenReturn(user);
        when(authz.session()).thenReturn(Optional.of(session));
        when(collaboratorRepository.findBySystemUser(user)).thenReturn(Optional.of(collab));
        when(collaboratorRepository.findCompanyByCollaborator(collab)).thenReturn(Optional.of(mine)); // collab → mine

        final RegistrationID reg = RegistrationID.valueOf("CS-DDD");
        final Aircraft foreign = aircraftForCompany(reg, other); // aircraft → other
        when(aircraftRepository.ofIdentity(reg)).thenReturn(Optional.of(foreign));

        assertThrows(SecurityException.class, () -> controller.decommissionAircraft(reg));
        verify(aircraftRepository, never()).save(any());
    }

    @Test
    void decommissionAircraft_unknownRegistration_throws() {
        final AirTransportCompany mine = company("EE", "EEE", "Mine4");
        final SystemUser user = dummyUser("collab4");
        final CollaboratorATCC collab = new CollaboratorATCC("N4", CollaboratorEmail.valueOf("n4@x.com"),
                CollaboratorPhone.valueOf("+351944444444"), user, mine);
        stubSessionAndCollaborator(user, collab, mine);

        final RegistrationID reg = RegistrationID.valueOf("CS-XXX");
        when(aircraftRepository.ofIdentity(reg)).thenReturn(Optional.empty());

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.decommissionAircraft(reg));
        assertTrue(ex.getMessage().contains("Unknown"));
    }

    @Test
    void decommissionAircraft_whenUnauthorized_throwsAndDoesNotQueryRepository() {
        doThrow(new SecurityException("forbidden")).when(authz).ensureAuthenticatedUserHasAnyOf(
                eq(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR), eq(Roles.ADMIN));

        assertThrows(SecurityException.class,
                () -> controller.decommissionAircraft(RegistrationID.valueOf("CS-ZZZ")));
        verify(aircraftRepository, never()).ofIdentity(any());
    }

    @Test
    void activeFleetForCurrentCompany_whenUnauthorized_throwsAndDoesNotQueryFleet() {
        doThrow(new SecurityException("forbidden")).when(authz).ensureAuthenticatedUserHasAnyOf(
                eq(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR), eq(Roles.ADMIN));

        assertThrows(SecurityException.class, () -> controller.activeFleetForCurrentCompany());
        verify(aircraftRepository, never()).findByCompany(any());
    }

    @Test
    void decommissionAircraft_whenAlreadyDecommissioned_throwsIllegalState() {
        final AirTransportCompany mine = company("FF", "FFF", "Mine5");
        final SystemUser user = dummyUser("collab5");
        final CollaboratorATCC collab = new CollaboratorATCC("N5", CollaboratorEmail.valueOf("n5@x.com"),
                CollaboratorPhone.valueOf("+351955555555"), user, mine);
        stubSessionAndCollaborator(user, collab, mine);

        final RegistrationID reg = RegistrationID.valueOf("CS-RET");
        final Aircraft ac = aircraftForCompany(reg, mine);
        ac.decommission();
        when(aircraftRepository.ofIdentity(reg)).thenReturn(Optional.of(ac));

        assertThrows(IllegalStateException.class, () -> controller.decommissionAircraft(reg));
        verify(aircraftRepository, never()).save(any());
    }

    @Test
    void constructor_rejectsNullDependencies() {
        assertThrows(IllegalArgumentException.class,
                () -> new DecommissionAircraftController(null, aircraftRepository, collaboratorRepository, companyRepository));
        assertThrows(IllegalArgumentException.class,
                () -> new DecommissionAircraftController(authz, null, collaboratorRepository,companyRepository));
        assertThrows(IllegalArgumentException.class,
                () -> new DecommissionAircraftController(authz, aircraftRepository, null, companyRepository));
        assertThrows(IllegalArgumentException.class,
                () -> new DecommissionAircraftController(authz, aircraftRepository, collaboratorRepository, null));
    }
}
