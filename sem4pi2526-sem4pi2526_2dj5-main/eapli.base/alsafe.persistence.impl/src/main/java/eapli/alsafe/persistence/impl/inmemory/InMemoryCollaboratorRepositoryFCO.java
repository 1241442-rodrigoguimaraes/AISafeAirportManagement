package eapli.alsafe.persistence.impl.inmemory;

import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryFCO;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryCollaboratorRepositoryFCO extends InMemoryDomainRepository<CollaboratorFCO, Long>
        implements CollaboratorRepositoryFCO {

    static {
        InMemoryInitializer.init();
    }

    @Override
    public Optional<CollaboratorFCO> findByEmail(CollaboratorEmail email) {
        return matchOne(c -> c.email().equals(email));
    }

    @Override
    public Optional<CollaboratorFCO> findBySystemUser(eapli.framework.infrastructure.authz.domain.model.SystemUser systemUser) {
        return matchOne(c -> c.user().equals(systemUser));
    }

    @Override
    public Iterable<CollaboratorFCO> findActiveByCustomerArea(final AirControlArea area) {
        return match(c -> area.equals(c.airControlArea()) && c.user().isActive());
    }
}
