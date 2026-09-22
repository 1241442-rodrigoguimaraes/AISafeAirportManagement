package eapli.alsafe.persistence.impl.jpa;

import eapli.alsafe.Application;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.pilotmanagement.repositories.PilotRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JpaPilotRepository extends JpaAutoTxRepository<Pilot, Long, Long> implements PilotRepository {

    public JpaPilotRepository(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    public JpaPilotRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "id");
    }

    @Override
    public Optional<Pilot> findByEmail(final CollaboratorEmail email) {
        final Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        return matchOne("e.email=:email", params);
    }

    @Override
    public Optional<Pilot> findBySystemUser(final SystemUser systemUser) {
        final Map<String, Object> params = new HashMap<>();
        params.put("systemUser", systemUser);
        return matchOne("e.systemUser=:systemUser", params);
    }

    @Override
    public Iterable<Pilot> findByCompany(final AirTransportCompany company) {
        final Map<String, Object> params = new HashMap<>();
        params.put("icao", company.identity());
        return match("e.company.icao = :icao", params);
    }

    @Override
    public Iterable<Pilot> findActiveByCompany(final AirTransportCompany company) {
        final Map<String, Object> params = new HashMap<>();
        params.put("icao", company.identity());
        return match("e.company.icao = :icao AND e.systemUser.active = true", params);
    }
}
