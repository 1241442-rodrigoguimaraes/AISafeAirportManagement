package eapli.alsafe.collaboratormanagement.domain;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.framework.domain.model.DomainFactory;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CollaboratorFCOBuilder implements DomainFactory<CollaboratorFCO> {

    private static final Logger LOGGER = LogManager.getLogger(CollaboratorFCOBuilder.class);

    private String name;
    private CollaboratorEmail email;
    private CollaboratorPhone phone;
    private SystemUser systemUser;
    private AirControlArea airControlArea;

    public CollaboratorFCOBuilder() {}

    public CollaboratorFCOBuilder with(final String name, final String email, final String phone,
                                       final SystemUser systemUser, final AirControlArea airControlArea) {
        withName(name);
        withEmail(email);
        withPhone(phone);
        withSystemUser(systemUser);
        withAirControlArea(airControlArea);
        return this;
    }

    public CollaboratorFCOBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public CollaboratorFCOBuilder withEmail(final String email) {
        this.email = CollaboratorEmail.valueOf(email);
        return this;
    }

    public CollaboratorFCOBuilder withEmail(final CollaboratorEmail email) {
        this.email = email;
        return this;
    }

    public CollaboratorFCOBuilder withPhone(final String phone) {
        this.phone = CollaboratorPhone.valueOf(phone);
        return this;
    }

    public CollaboratorFCOBuilder withPhone(final CollaboratorPhone phone) {
        this.phone = phone;
        return this;
    }

    public CollaboratorFCOBuilder withSystemUser(final SystemUser systemUser) {
        this.systemUser = systemUser;
        return this;
    }

    public CollaboratorFCOBuilder withAirControlArea(final AirControlArea airControlArea) {
        this.airControlArea = airControlArea;
        return this;
    }

    @Override
    public CollaboratorFCO build() {
        final var collaborator = new CollaboratorFCO(name, email, phone, systemUser, airControlArea);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Building CollaboratorFCO : [{}]", collaborator);
        }

        return collaborator;
    }
}
