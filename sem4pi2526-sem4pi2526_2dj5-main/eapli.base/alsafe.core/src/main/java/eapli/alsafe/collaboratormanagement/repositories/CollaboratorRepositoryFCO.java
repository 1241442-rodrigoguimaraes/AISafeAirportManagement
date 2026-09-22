package eapli.alsafe.collaboratormanagement.repositories;

import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.framework.domain.repositories.DomainRepository;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.util.Optional;

public interface CollaboratorRepositoryFCO extends DomainRepository<Long, CollaboratorFCO> {
    Optional<CollaboratorFCO> findByEmail(CollaboratorEmail email);
    Optional<CollaboratorFCO> findBySystemUser(SystemUser systemUser);
    Iterable<CollaboratorFCO> findActiveByCustomerArea(AirControlArea area);
}
