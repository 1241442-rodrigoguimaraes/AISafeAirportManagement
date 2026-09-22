package eapli.alsafe.persistence.impl.inmemory;

import eapli.alsafe.antlr.flightplan.domain.FlightPlanId;
import eapli.alsafe.antlr.flightplan.domain.ImportedFlightPlan;
import eapli.alsafe.antlr.flightplan.repositories.ImportedFlightPlanRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

public class InMemoryImportedFlightPlanRepository extends InMemoryDomainRepository<ImportedFlightPlan, FlightPlanId>
        implements ImportedFlightPlanRepository {

    public InMemoryImportedFlightPlanRepository() {
        super();
    }
}
