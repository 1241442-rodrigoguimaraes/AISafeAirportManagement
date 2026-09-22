package eapli.alsafe.persistence.impl.inmemory;

import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryCollaboratorRepositoryATCC extends InMemoryDomainRepository<CollaboratorATCC, Long>
        implements CollaboratorRepositoryATCC {

    static {
        InMemoryInitializer.init();
    }

    @Override
    public Optional<CollaboratorATCC> findByEmail(CollaboratorEmail email) {
        return matchOne(c -> c.email().equals(email));
    }

    @Override
    public Optional<CollaboratorATCC> findBySystemUser(eapli.framework.infrastructure.authz.domain.model.SystemUser systemUser) {
        return matchOne(c -> c.user().equals(systemUser));
    }

    @Override
    public Optional<AirTransportCompany> findCompanyByCollaborator(CollaboratorATCC collaborator) {
        return matchOne(c -> c.equals(collaborator)).map(CollaboratorATCC:: getAtcc);
    }

    @Override
    public Iterable<CollaboratorATCC> findActiveByCompany(AirTransportCompany company) {
        return match(c -> c.getAtcc().equals(company) && c.user().isActive());
    }
}
