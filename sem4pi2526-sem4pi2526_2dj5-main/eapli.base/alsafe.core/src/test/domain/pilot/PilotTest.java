package domain.pilot;

import eapli.alsafe.aircraftModelMagnement.domain.*;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.IATACompanyCode;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.alsafe.engineModelMagnement.Domain.*;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PilotTest {

    private static SystemUser pilotUser() {
        return new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder())
                .with("pilot@tap.pt", "Password1", "John", "Pilot", "pilot@tap.pt")
                .withRoles(Roles.PILOT)
                .build();
    }

    private static AirTransportCompany company() {
        return new AirTransportCompany(new IATACompanyCode("TP"), new ICAOCompanyCode("TAP"), "TAP Air");
    }

    private static aircraftModel aircraftModel() {
        final Maker maker = new Maker(new MakerName("MakerA"), new MakerCountry("PT"));
        final engineModel engine = new engineModel(new engineModelName("E1"), maker, engineType.TURBOFAN,
                new engineModelPower(1000), engineModelFuel.JET_A1, new engineModelEfficiency(0.9));
        return new aircraftModel(new aircraftModelName("A320"), maker, aircraftModelType.PASSENGER,
                engineType.TURBOFAN, new maximumRange(1000),
                new aircraftModelPhysicsData(1000, 2000, 1500, 500, 30000, 800, 50, 0.02, 1.2),
                Set.of(engine));
    }

    private static Pilot validPilot() {
        return new Pilot("John Pilot", CollaboratorEmail.valueOf("pilot@tap.pt"),
                CollaboratorPhone.valueOf("912345678"), pilotUser(), company(), Set.of(aircraftModel()));
    }

    @Test
    void ensureValidPilotCreation() {
        final Pilot pilot = validPilot();

        assertEquals("John Pilot", pilot.name());
        assertEquals("pilot@tap.pt", pilot.email().toString());
        assertEquals("912345678", pilot.phoneNumber());
        assertEquals(company(), pilot.company());
        assertEquals(1, pilot.certifiedAircraftModels().size());
        assertTrue(pilot.isActive());
    }

    @Test
    void ensurePilotWithoutNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Pilot(" ", CollaboratorEmail.valueOf("pilot@tap.pt"),
                CollaboratorPhone.valueOf("912345678"), pilotUser(), company(), Set.of(aircraftModel())));
    }

    @Test
    void ensurePilotWithoutEmailIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Pilot("John Pilot", null,
                CollaboratorPhone.valueOf("912345678"), pilotUser(), company(), Set.of(aircraftModel())));
    }

    @Test
    void ensurePilotWithoutPhoneIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Pilot("John Pilot",
                CollaboratorEmail.valueOf("pilot@tap.pt"), null, pilotUser(), company(), Set.of(aircraftModel())));
    }

    @Test
    void ensurePilotWithoutSystemUserIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Pilot("John Pilot",
                CollaboratorEmail.valueOf("pilot@tap.pt"), CollaboratorPhone.valueOf("912345678"), null,
                company(), Set.of(aircraftModel())));
    }

    @Test
    void ensurePilotWithoutCompanyIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Pilot("John Pilot",
                CollaboratorEmail.valueOf("pilot@tap.pt"), CollaboratorPhone.valueOf("912345678"), pilotUser(),
                null, Set.of(aircraftModel())));
    }

    @Test
    void ensurePilotWithoutCertificationsIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Pilot("John Pilot",
                CollaboratorEmail.valueOf("pilot@tap.pt"), CollaboratorPhone.valueOf("912345678"), pilotUser(),
                company(), Set.of()));
    }

    @Test
    void ensureDuplicatedCertificationIsRejected() {
        final Pilot pilot = validPilot();
        final aircraftModel model = pilot.certifiedAircraftModels().iterator().next();

        assertThrows(IllegalArgumentException.class, () -> pilot.addCertification(model));
    }

    @Test
    void ensureActivePilotCanBeDeactivated() {
        final Pilot pilot = validPilot();

        pilot.deactivate();

        assertFalse(pilot.isActive());
    }

    @Test
    void ensureInactivePilotCannotBeDeactivatedTwice() {
        final Pilot pilot = validPilot();
        pilot.deactivate();

        assertThrows(IllegalStateException.class, pilot::deactivate);
    }
}
