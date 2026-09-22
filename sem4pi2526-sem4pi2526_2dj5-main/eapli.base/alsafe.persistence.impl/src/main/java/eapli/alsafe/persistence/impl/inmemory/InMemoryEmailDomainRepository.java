package eapli.alsafe.persistence.impl.inmemory;

import eapli.alsafe.alsafeusermanagement.domain.EmailDomain;
import eapli.alsafe.alsafeusermanagement.repositories.EmailDomainRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

public class InMemoryEmailDomainRepository extends InMemoryDomainRepository<EmailDomain, String>
        implements EmailDomainRepository {

    static {
        InMemoryInitializer.init();
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
