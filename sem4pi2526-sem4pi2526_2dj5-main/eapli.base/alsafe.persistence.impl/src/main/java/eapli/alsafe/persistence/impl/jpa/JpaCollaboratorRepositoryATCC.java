package eapli.alsafe.persistence.impl.jpa;

import eapli.alsafe.Application;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JpaCollaboratorRepositoryATCC extends JpaAutoTxRepository<CollaboratorATCC, Long, Long>
        implements CollaboratorRepositoryATCC {

    public JpaCollaboratorRepositoryATCC(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    public JpaCollaboratorRepositoryATCC(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "id");
    }

    @Override
    public Optional<CollaboratorATCC> findByEmail(CollaboratorEmail email) {
        final Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        return matchOne("e.email=:email", params);
    }

    @Override
    public Optional<CollaboratorATCC> findBySystemUser(SystemUser systemUser) {
        final Map<String, Object> params = new HashMap<>();
        params.put("systemUser", systemUser);
        return matchOne("e.systemUser=:systemUser", params);
    }

    @Override
    public Optional<AirTransportCompany> findCompanyByCollaborator(CollaboratorATCC collaborator) {
        final Map<String, Object> params = new HashMap<>();
        params.put("collaboratorId", collaborator.identity());
        return matchOne("e.id=:collaboratorId", params).map(CollaboratorATCC::getAtcc);
    }

    @Override
    public Iterable<CollaboratorATCC> findActiveByCompany(AirTransportCompany company) {
        final Map<String, Object> params = new HashMap<>();
        params.put("companyId", company.identity());
        return match("e.atcc.id=:companyId AND e.systemUser.active = true", params);
    }
}
