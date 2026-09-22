package eapli.alsafe.alsafeusermanagement.domain;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Entity
@EqualsAndHashCode(of = "domain")
public class EmailDomain implements AggregateRoot<String> {

    @Id
    @Getter
    private String domain;

    protected EmailDomain() {
        // for ORM
    }

    public EmailDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public boolean sameAs(Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public String identity() {
        return this.domain;
    }

    @Override
    public String toString() {
        return "EmailDomain : " + domain;
    }
}
