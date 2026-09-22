package eapli.alsafe.pilotmanagement.domain;

import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.domain.model.DomainFactory;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.util.HashSet;
import java.util.Set;

public class PilotBuilder implements DomainFactory<Pilot> {

    private String name;
    private CollaboratorEmail email;
    private CollaboratorPhone phone;
    private SystemUser systemUser;
    private AirTransportCompany company;
    private final Set<aircraftModel> certifiedAircraftModels = new HashSet<>();

    public PilotBuilder with(final String name, final String email, final String phone,
                             final SystemUser systemUser, final AirTransportCompany company,
                             final Set<aircraftModel> certifiedAircraftModels) {
        withName(name);
        withEmail(email);
        withPhone(phone);
        withSystemUser(systemUser);
        withCompany(company);
        withCertifiedAircraftModels(certifiedAircraftModels);
        return this;
    }

    public PilotBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public PilotBuilder withEmail(final String email) {
        this.email = CollaboratorEmail.valueOf(email);
        return this;
    }

    public PilotBuilder withEmail(final CollaboratorEmail email) {
        this.email = email;
        return this;
    }

    public PilotBuilder withPhone(final String phone) {
        this.phone = CollaboratorPhone.valueOf(phone);
        return this;
    }

    public PilotBuilder withPhone(final CollaboratorPhone phone) {
        this.phone = phone;
        return this;
    }

    public PilotBuilder withSystemUser(final SystemUser systemUser) {
        this.systemUser = systemUser;
        return this;
    }

    public PilotBuilder withCompany(final AirTransportCompany company) {
        this.company = company;
        return this;
    }

    public PilotBuilder addCertifiedAircraftModel(final aircraftModel model) {
        this.certifiedAircraftModels.add(model);
        return this;
    }

    public PilotBuilder withCertifiedAircraftModels(final Set<aircraftModel> models) {
        this.certifiedAircraftModels.clear();
        if (models != null) {
            this.certifiedAircraftModels.addAll(models);
        }
        return this;
    }

    @Override
    public Pilot build() {
        return new Pilot(name, email, phone, systemUser, company, certifiedAircraftModels);
    }
}
