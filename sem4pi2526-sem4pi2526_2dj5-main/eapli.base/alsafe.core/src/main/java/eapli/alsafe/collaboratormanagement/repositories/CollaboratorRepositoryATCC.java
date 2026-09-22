package eapli.alsafe.collaboratormanagement.repositories;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface CollaboratorRepositoryATCC extends DomainRepository<Long, CollaboratorATCC> {
    Optional<CollaboratorATCC> findByEmail(CollaboratorEmail email);
    Optional<CollaboratorATCC> findBySystemUser(eapli.framework.infrastructure.authz.domain.model.SystemUser systemUser);
    Optional<AirTransportCompany> findCompanyByCollaborator(CollaboratorATCC collaborator);
    Iterable<CollaboratorATCC> findActiveByCompany(AirTransportCompany company);
}
