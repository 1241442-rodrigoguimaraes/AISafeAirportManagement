package domain.collaboratorTest;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaBoundaries;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaMinimumFuel;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.framework.infrastructure.authz.domain.model.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CollaboratorFCOTest {

    private SystemUser dummyUser() {
        final SystemUserBuilder userBuilder = new SystemUserBuilder(new NilPasswordPolicy(), new PlainTextEncoder());
        return userBuilder.with("username", "password", "firstName", "lastName", "email@email.com").build();
    }

    private AirControlArea dummyArea() {
        List<Coordinate> coords = List.of(
                new Coordinate(10.0, 10.0),
                new Coordinate(20.0, 10.0),
                new Coordinate(10.0, 20.0),
                new Coordinate(20.0, 20.0)
        );
        return new AirControlArea(
                new AirControlAreaID(1L),
                "Area 1",
                new AirControlAreaBoundaries(coords),
                new AirControlAreaMinimumFuel(100.0)
        );
    }

    @Test
    public void ensureCollaboratorIsCreated() {
        CollaboratorEmail email = CollaboratorEmail.valueOf("test@example.com");
        CollaboratorPhone phone = CollaboratorPhone.valueOf("912345678");
        SystemUser user = dummyUser();
        AirControlArea area = dummyArea();

        CollaboratorFCO collaboratorFCO = new CollaboratorFCO("John Doe", email, phone, user, area);
        assertNotNull(collaboratorFCO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCollaboratorCannotBeCreatedWithNullName() {
        new CollaboratorFCO(null, CollaboratorEmail.valueOf("test@example.com"),
                CollaboratorPhone.valueOf("912345678"), dummyUser(), dummyArea());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCollaboratorCannotBeCreatedWithNullEmail() {
        new CollaboratorFCO("John Doe", null,
                CollaboratorPhone.valueOf("912345678"), dummyUser(), dummyArea());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCollaboratorCannotBeCreatedWithNullPhone() {
        new CollaboratorFCO("John Doe", CollaboratorEmail.valueOf("test@example.com"),
                null, dummyUser(), dummyArea());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCollaboratorCannotBeCreatedWithNullUser() {
        new CollaboratorFCO("John Doe", CollaboratorEmail.valueOf("test@example.com"),
                CollaboratorPhone.valueOf("912345678"), null, dummyArea());
    }

    @Test
    public void ensureChangeEmailAndPhoneUpdatesContactDetails() {
        CollaboratorFCO collaboratorFCO = new CollaboratorFCO("John Doe",
                CollaboratorEmail.valueOf("old@example.com"),
                CollaboratorPhone.valueOf("912345678"),
                dummyUser(), dummyArea());

        collaboratorFCO.changeEmailAndPhone(
                CollaboratorEmail.valueOf("new@example.com"),
                CollaboratorPhone.valueOf("923456789"));

        assertEquals("new@example.com", collaboratorFCO.email().toString());
        assertEquals("923456789", collaboratorFCO.phoneNumber());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureChangeEmailAndPhoneRejectsNullEmail() {
        CollaboratorFCO collaboratorFCO = new CollaboratorFCO("John Doe",
                CollaboratorEmail.valueOf("test@example.com"),
                CollaboratorPhone.valueOf("912345678"),
                dummyUser(), dummyArea());
        collaboratorFCO.changeEmailAndPhone(null, CollaboratorPhone.valueOf("923456789"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCollaboratorCannotBeCreatedWithNullArea() {
        new CollaboratorFCO("John Doe", CollaboratorEmail.valueOf("test@example.com"),
                CollaboratorPhone.valueOf("912345678"), dummyUser(), null);
    }

    @Test
    public void ensureEqualityBasedOnEmail() {
        CollaboratorEmail email = CollaboratorEmail.valueOf("test@example.com");
        CollaboratorPhone phone = CollaboratorPhone.valueOf("912345678");
        SystemUser user1 = dummyUser();
        AirControlArea area = dummyArea();

        CollaboratorFCO c1 = new CollaboratorFCO("John", email, phone, user1, area);
        CollaboratorFCO c2 = new CollaboratorFCO("John Doe", email, phone, user1, area);

        assertTrue(c1.sameAs(c2));
    }
}
