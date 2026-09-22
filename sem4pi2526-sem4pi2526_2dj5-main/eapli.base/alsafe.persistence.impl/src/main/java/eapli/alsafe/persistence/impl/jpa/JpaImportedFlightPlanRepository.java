package eapli.alsafe.persistence.impl.jpa;

import eapli.alsafe.Application;
import eapli.alsafe.antlr.flightplan.domain.FlightPlanId;
import eapli.alsafe.antlr.flightplan.domain.ImportedFlightPlan;
import eapli.alsafe.antlr.flightplan.repositories.ImportedFlightPlanRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

public class JpaImportedFlightPlanRepository extends JpaAutoTxRepository<ImportedFlightPlan, FlightPlanId, FlightPlanId>
        implements ImportedFlightPlanRepository {

    public JpaImportedFlightPlanRepository(final TransactionalContext autoTx) {
        super(autoTx, "flightPlanId");
    }

    public JpaImportedFlightPlanRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "flightPlanId");
    }
}
