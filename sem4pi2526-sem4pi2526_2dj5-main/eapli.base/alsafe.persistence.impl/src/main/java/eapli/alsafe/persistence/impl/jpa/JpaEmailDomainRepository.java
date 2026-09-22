package eapli.alsafe.persistence.impl.jpa;

import eapli.alsafe.Application;
import eapli.alsafe.alsafeusermanagement.domain.EmailDomain;
import eapli.alsafe.alsafeusermanagement.repositories.EmailDomainRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

public class JpaEmailDomainRepository extends JpaAutoTxRepository<EmailDomain, String, String>
        implements EmailDomainRepository {

    public JpaEmailDomainRepository(final TransactionalContext autoTx) {
        super(autoTx, "domain");
    }

    public JpaEmailDomainRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(),
                "domain");
    }

    @Override
    public boolean contains(final String domain) {
        return ofIdentity(domain).isPresent();
    }

    @Override
    public void save(final String domain) {
        final EmailDomain newDomain = new EmailDomain(domain);
        save(newDomain);
    }
}
