package eapli.alsafe.pilotmanagement.repositories;

import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.framework.domain.repositories.DomainRepository;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.util.Optional;

public interface PilotRepository extends DomainRepository<Long, Pilot> {

    Optional<Pilot> findByEmail(CollaboratorEmail email);

    Optional<Pilot> findBySystemUser(SystemUser systemUser);

    Iterable<Pilot> findByCompany(AirTransportCompany company);

    Iterable<Pilot> findActiveByCompany(AirTransportCompany company);
}
