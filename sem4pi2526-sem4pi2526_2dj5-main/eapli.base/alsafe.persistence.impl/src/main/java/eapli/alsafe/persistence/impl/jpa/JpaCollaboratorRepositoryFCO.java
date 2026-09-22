package eapli.alsafe.persistence.impl.jpa;

import eapli.alsafe.Application;
import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryFCO;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JpaCollaboratorRepositoryFCO extends JpaAutoTxRepository<CollaboratorFCO, Long, Long>
        implements CollaboratorRepositoryFCO {

    public JpaCollaboratorRepositoryFCO(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    public JpaCollaboratorRepositoryFCO(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "id");
    }

    @Override
    public Optional<CollaboratorFCO> findByEmail(CollaboratorEmail email) {
        final Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        return matchOne("e.email=:email", params);
    }

    @Override
    public Optional<CollaboratorFCO> findBySystemUser(SystemUser systemUser) {
        final Map<String, Object> params = new HashMap<>();
        params.put("systemUser", systemUser);
        return matchOne("e.systemUser=:systemUser", params);
    }

    @Override
    public Iterable<CollaboratorFCO> findActiveByCustomerArea(final AirControlArea area) {
        final Map<String, Object> params = new HashMap<>();
        params.put("area", area);
        return match("e.airControlArea = :area AND e.systemUser.active = true", params);
    }
}
