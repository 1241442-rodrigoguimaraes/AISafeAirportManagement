package controller.aircraft;

import eapli.alsafe.aircraft.application.AddAircraftController;
import eapli.alsafe.aircraft.domain.AircraftCountry;
import eapli.alsafe.aircraft.domain.CabinConfiguration;
import eapli.alsafe.aircraft.domain.RegistrationID;
import eapli.alsafe.aircraft.repositories.AircraftRepository;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelName;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelPhysicsData;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelType;
import eapli.alsafe.aircraftModelMagnement.domain.maximumRange;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
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

/**
 * Unit tests for US070 — {@link AddAircraftController}.
 */
class AddAircraftControllerTest {

    private AuthorizationService authz;
    private aircraftModelRepository aircraftModelRepository;
    private AircraftRepository aircraftRepository;
    private CollaboratorRepositoryATCC collaboratorRepository;
    private AirCompanyRepository companyRepository;
    private AddAircraftController controller;

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

    private static AirTransportCompany company() {
        return new AirTransportCompany(new IATACompanyCode("TP"), new ICAOCompanyCode("TAP"), "TAP Air");
    }

    private static SystemUser atccUser() {
        return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with("atcc@tap.com", "Password1", "Joao", "Silva", "atcc@tap.com")
                .withRoles(Set.of(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR).toArray(new Role[0]))
                .build();
    }

    private void stubAuthenticatedAtcc(final AirTransportCompany company) {
        final SystemUser user = atccUser();
        final CollaboratorATCC collaborator = new CollaboratorATCC("Joao",
                CollaboratorEmail.valueOf("atcc@tap.com"),
                CollaboratorPhone.valueOf("+351911111111"), user, company);
        final UserSession session = mock(UserSession.class);
        when(session.authenticatedUser()).thenReturn(user);
        when(authz.session()).thenReturn(Optional.of(session));
        when(collaboratorRepository.findBySystemUser(user)).thenReturn(Optional.of(collaborator));
        when(collaboratorRepository.findCompanyByCollaborator(collaborator)).thenReturn(Optional.of(company));
    }

    @BeforeEach
    void setUp() {
        authz = mock(AuthorizationService.class);
        aircraftModelRepository = mock(aircraftModelRepository.class);
        aircraftRepository = mock(AircraftRepository.class);
        collaboratorRepository = mock(CollaboratorRepositoryATCC.class);
        companyRepository = mock(AirCompanyRepository.class);
        controller = new AddAircraftController(authz, aircraftModelRepository, aircraftRepository,
                collaboratorRepository, companyRepository);
    }

    @Test
    void addAircraft_withValidData_persistsAircraft() {
        final AirTransportCompany company = company();
        stubAuthenticatedAtcc(company);
        final aircraftModel model = dummyModel();
        final int max = model.maxPassengerSeats();

        when(aircraftRepository.existsByRegistrationID(RegistrationID.valueOf("CS-TST"))).thenReturn(false);
        when(aircraftRepository.addAircraft(eq(model), eq(RegistrationID.valueOf("CS-TST")),
                any(AircraftCountry.class), any(CabinConfiguration.class), any(List.class), eq(company)))
                .thenReturn(true);

        final boolean ok = controller.addAircraft(model, "CS-TST", "PT",
                Math.min(10, max), 0, 0, 1);

        assertTrue(ok);
        verify(aircraftRepository).addAircraft(eq(model), eq(RegistrationID.valueOf("CS-TST")),
                any(AircraftCountry.class), any(CabinConfiguration.class), any(List.class), eq(company));
    }

    @Test
    void addAircraft_duplicateRegistration_throws() {
        stubAuthenticatedAtcc(company());
        final aircraftModel model = dummyModel();

        when(aircraftRepository.existsByRegistrationID(RegistrationID.valueOf("CS-DUP"))).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> controller.addAircraft(model, "CS-DUP", "PT", 10, 0, 0, 1));
        verify(aircraftRepository, never()).addAircraft(any(), any(), any(), any(), any(), any());
    }

    @Test
    void addAircraft_zeroCrew_throws() {
        stubAuthenticatedAtcc(company());
        final aircraftModel model = dummyModel();
        final int max = model.maxPassengerSeats();

        when(aircraftRepository.existsByRegistrationID(any())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> controller.addAircraft(model, "CS-NOCREW", "PT", Math.min(5, max), 0, 0, 0));
    }

    @Test
    void addAircraft_cabinExceedsCapacity_throws() {
        stubAuthenticatedAtcc(company());
        final aircraftModel model = dummyModel();
        final int max = model.maxPassengerSeats();

        when(aircraftRepository.existsByRegistrationID(any())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> controller.addAircraft(model, "CS-BIG", "PT", max + 1, 0, 0, 1));
    }

    @Test
    void getAuthenticatedCollaboratorCompany_returnsLinkedCompany() {
        final AirTransportCompany company = company();
        stubAuthenticatedAtcc(company);

        assertEquals(company, controller.getAuthenticatedCollaboratorCompany());
    }

    @Test
    void addAircraft_whenUnauthorized_throws() {
        doThrow(new SecurityException("forbidden")).when(authz)
                .ensureAuthenticatedUserHasAnyOf(eq(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR), eq(Roles.ADMIN));

        assertThrows(SecurityException.class,
                () -> controller.addAircraft(dummyModel(), "CS-ZZZ", "PT", 10, 0, 0, 1));
        verify(aircraftRepository, never()).existsByRegistrationID(any());
    }
}
