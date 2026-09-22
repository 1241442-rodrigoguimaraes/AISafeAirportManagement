package eapli.alsafe.persistence.impl.inmemory;

import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.pilotmanagement.repositories.PilotRepository;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryPilotRepository extends InMemoryDomainRepository<Pilot, Long> implements PilotRepository {

    static {
        InMemoryInitializer.init();
    }

    @Override
    public Optional<Pilot> findByEmail(final CollaboratorEmail email) {
        return matchOne(p -> p.email().equals(email));
    }

    @Override
    public Optional<Pilot> findBySystemUser(final SystemUser systemUser) {
        return matchOne(p -> p.user().equals(systemUser));
    }

    @Override
    public Iterable<Pilot> findByCompany(final AirTransportCompany company) {
        return match(p -> p.company().equals(company));
    }

    @Override
    public Iterable<Pilot> findActiveByCompany(final AirTransportCompany company) {
        return match(p -> p.company().equals(company) && p.isActive());
    }
}
