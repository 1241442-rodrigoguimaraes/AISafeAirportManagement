package eapli.alsafe.collaboratormanagement.domain;

import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
public class CollaboratorATCC implements AggregateRoot<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Long version;

    @Column
    private String name;

    @Embedded
    private CollaboratorEmail email;

    @Embedded
    private CollaboratorPhone phone;

    @OneToOne(optional = false)
    private SystemUser systemUser;

    @JoinColumn
    @ManyToOne(optional = false)
    @Getter
    private AirTransportCompany atcc;


    protected CollaboratorATCC() {
        // for ORM
    }

    public CollaboratorATCC(final String name, final CollaboratorEmail email,
                            final CollaboratorPhone phone, final SystemUser systemUser,
                            final AirTransportCompany atcc) {
        if (name == null || email == null || phone == null || systemUser == null || atcc == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.systemUser = systemUser;
        this.atcc = atcc;
    }

    public CollaboratorEmail email() {
        return email;
    }

    public String name() {
        return name;
    }

    public String phoneNumber() {
        return phone.toString();
    }

    public SystemUser user() {
        return systemUser;
    }




    /**
     * @return true if this collaborator's account can be disabled (currently active).
     */
    public boolean canBeDisabled() {
        return systemUser != null && systemUser.isActive();
    }

    public void changeEmailAndPhone(final CollaboratorEmail newEmail, final CollaboratorPhone newPhone) {
        if (newEmail == null || newPhone == null) {
            throw new IllegalArgumentException("Email and phone cannot be null");
        }
        this.email = newEmail;
        this.phone = newPhone;
    }

    @Override
    public boolean sameAs(Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public Long identity() {
        return this.id;
    }

    @Override
    public String toString() {
        return String.format("Collaborator: %s (%s)", name, email);
    }
}
