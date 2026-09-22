package eapli.alsafe.collaboratormanagement.application;

import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;

public class CollaboratorDTO {
    public final String name;
    public final String email;
    public final String phoneNumber;
    public final String role;

    public CollaboratorDTO(final String name, final String email, final String phoneNumber, final String role) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public static CollaboratorDTO fromCollaboratorFCO(final CollaboratorFCO c) {
        return new CollaboratorDTO(
                c.name(),
                c.email().toString(),
                c.phoneNumber(),
                c.user().roleTypes().toString());
    }

    public static CollaboratorDTO fromCollaboratorATCC(final CollaboratorATCC c) {
        return new CollaboratorDTO(
                c.name(),
                c.email().toString(),
                c.phoneNumber(),
                c.user().roleTypes().toString());
    }

    @Override
    public String toString() {
        return String.format("Name: %s | Email: %s | Phone: %s | Role: %s", name, email, phoneNumber, role);
    }
}
