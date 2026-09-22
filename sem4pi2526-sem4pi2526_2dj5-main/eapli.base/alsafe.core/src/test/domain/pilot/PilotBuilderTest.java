package domain.pilot;

import eapli.alsafe.aircraftModelMagnement.domain.*;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.domain.IATACompanyCode;
import eapli.alsafe.companies.domain.ICAOCompanyCode;
import eapli.alsafe.engineModelMagnement.Domain.*;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.pilotmanagement.domain.PilotBuilder;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.infrastructure.authz.domain.model.NilPasswordPolicy;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PilotBuilderTest {

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

    @Test
    void ensureBuilderCreatesPilot() {
        final Pilot pilot = new PilotBuilder()
                .with("John Pilot", "pilot@tap.pt", "912345678", pilotUser(), company(), Set.of(aircraftModel()))
                .build();

        assertEquals("John Pilot", pilot.name());
        assertEquals("pilot@tap.pt", pilot.email().toString());
        assertEquals(1, pilot.certifiedAircraftModels().size());
    }

    @Test
    void ensureBuilderWithoutCertificationsIsRejected() {
        final PilotBuilder builder = new PilotBuilder()
                .withName("John Pilot")
                .withEmail("pilot@tap.pt")
                .withPhone("912345678")
                .withSystemUser(pilotUser())
                .withCompany(company());

        assertThrows(IllegalArgumentException.class, builder::build);
    }
}
