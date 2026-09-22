package eapli.alsafe.collaboratormanagement.domain;

import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.domain.model.DomainFactory;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CollaboratorATCCBuilder implements DomainFactory<CollaboratorATCC> {

    private static final Logger LOGGER = LogManager.getLogger(CollaboratorATCCBuilder.class);

    private String name;
    private CollaboratorEmail email;
    private CollaboratorPhone phone;
    private SystemUser systemUser;
    private AirTransportCompany company;

    public CollaboratorATCCBuilder() {}

    public CollaboratorATCCBuilder with(final String name, final String email, final String phone,
                                        final SystemUser systemUser, final AirTransportCompany company) {
        withName(name);
        withEmail(email);
        withPhone(phone);
        withSystemUser(systemUser);
        withCompany(company);
        return this;
    }

    public CollaboratorATCCBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public CollaboratorATCCBuilder withEmail(final String email) {
        this.email = CollaboratorEmail.valueOf(email);
        return this;
    }

    public CollaboratorATCCBuilder withEmail(final CollaboratorEmail email) {
        this.email = email;
        return this;
    }

    public CollaboratorATCCBuilder withPhone(final String phone) {
        this.phone = CollaboratorPhone.valueOf(phone);
        return this;
    }

    public CollaboratorATCCBuilder withPhone(final CollaboratorPhone phone) {
        this.phone = phone;
        return this;
    }

    public CollaboratorATCCBuilder withSystemUser(final SystemUser systemUser) {
        this.systemUser = systemUser;
        return this;
    }

    public CollaboratorATCCBuilder withCompany(final AirTransportCompany company) {
        this.company = company;
        return this;
    }

    @Override
    public CollaboratorATCC build() {
        final var collaborator = new CollaboratorATCC(name, email, phone, systemUser, company);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Building CollaboratorATCC : [{}]", collaborator);
        }

        return collaborator;
    }
}
