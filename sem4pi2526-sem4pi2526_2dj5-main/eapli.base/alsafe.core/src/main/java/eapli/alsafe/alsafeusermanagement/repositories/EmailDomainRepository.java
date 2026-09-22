package eapli.alsafe.alsafeusermanagement.repositories;

import eapli.alsafe.alsafeusermanagement.domain.EmailDomain;
import eapli.framework.domain.repositories.DomainRepository;

public interface EmailDomainRepository extends DomainRepository<String, EmailDomain> {

    boolean contains(String domain);

    void save(String domain);
}
