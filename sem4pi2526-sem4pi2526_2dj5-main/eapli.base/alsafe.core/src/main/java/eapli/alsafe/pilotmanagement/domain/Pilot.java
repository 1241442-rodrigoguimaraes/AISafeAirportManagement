package eapli.alsafe.pilotmanagement.domain;

import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;

import java.io.Serial;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Pilot implements AggregateRoot<Long>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String name;

    @Embedded
    private CollaboratorEmail email;

    @Embedded
    private CollaboratorPhone phone;

    @OneToOne(optional = false)
    private SystemUser systemUser;

    @ManyToOne(optional = false)
    private AirTransportCompany company;

    @ManyToMany
    private Set<aircraftModel> certifiedAircraftModels = new HashSet<>();

    protected Pilot() {
        // for ORM
    }

    public Pilot(final String name, final CollaboratorEmail email, final CollaboratorPhone phone,
                 final SystemUser systemUser, final AirTransportCompany company,
                 final Set<aircraftModel> certifiedAircraftModels) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Pilot name cannot be null or blank");
        }
        if (email == null || phone == null || systemUser == null || company == null) {
            throw new IllegalArgumentException("Pilot mandatory data cannot be null");
        }
        if (certifiedAircraftModels == null || certifiedAircraftModels.isEmpty()) {
            throw new IllegalArgumentException("Pilot must be certified for at least one aircraft model");
        }

        this.name = name;
        this.email = email;
        this.phone = phone;
        this.systemUser = systemUser;
        this.company = company;

        for (final aircraftModel model : certifiedAircraftModels) {
            addCertification(model);
        }
    }

    public void addCertification(final aircraftModel model) {
        if (model == null) {
            throw new IllegalArgumentException("Aircraft model cannot be null");
        }
        if (certifiedAircraftModels.contains(model)) {
            throw new IllegalArgumentException("Pilot is already certified for this aircraft model");
        }

        certifiedAircraftModels.add(model);
    }

    public String name() {
        return name;
    }

    public CollaboratorEmail email() {
        return email;
    }

    public String phoneNumber() {
        return phone.toString();
    }

    public SystemUser user() {
        return systemUser;
    }

    public AirTransportCompany company() {
        return company;
    }

    public Set<aircraftModel> certifiedAircraftModels() {
        return Collections.unmodifiableSet(certifiedAircraftModels);
    }

    public boolean isActive() {
        return systemUser != null && systemUser.isActive();
    }

    /**
     * Marks the pilot as inactive in the company roster (US077).
     */
    public void deactivate() {
        if (!isActive()) {
            throw new IllegalStateException("Pilot is already inactive.");
        }
        systemUser.deactivate(Calendar.getInstance());
    }

    @Override
    public boolean sameAs(final Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public Long identity() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("Pilot: %s (%s)", name, email);
    }
}
