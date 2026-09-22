package eapli.alsafe.alsafeusermanagement.domain;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;

@Entity
@EqualsAndHashCode(of = "email")
public class AlSafeUser implements AggregateRoot<AlSafeUserEmail> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @OneToOne(optional = false)
    @JoinColumn(nullable = false, unique = true)
    private SystemUser systemUser;

    @EmbeddedId
    private AlSafeUserEmail email;

    @Getter
    private String phoneNumber;

    @Getter
    @Embedded
    private SecurityClearance securityClearance;

    @Getter
    @Embedded
    private SkillsAssessment skillsAssessment;

    public AlSafeUser(final SystemUser systemUser, final String phoneNumber, final SecurityClearance securityClearance, final SkillsAssessment skillsAssessment) {
        if (systemUser == null) throw new IllegalArgumentException();
        if (phoneNumber == null) throw new IllegalArgumentException();
        if (securityClearance == null) throw new IllegalArgumentException();
        if (skillsAssessment == null) throw new IllegalArgumentException();

        this.systemUser = systemUser;
        this.email = AlSafeUserEmail.fromSystemUserEmail(systemUser.email());
        this.phoneNumber = phoneNumber;
        this.securityClearance = securityClearance;
        this.skillsAssessment = skillsAssessment;
    }

    protected AlSafeUser() {
        // for ORM
    }

    public SystemUser user() {
        return this.systemUser;
    }

    @Override
    public AlSafeUserEmail identity() {
        return this.email;
    }

    @Override
    public boolean sameAs(Object other) {
        return DomainEntities.areEqual(this, other);
    }
}
