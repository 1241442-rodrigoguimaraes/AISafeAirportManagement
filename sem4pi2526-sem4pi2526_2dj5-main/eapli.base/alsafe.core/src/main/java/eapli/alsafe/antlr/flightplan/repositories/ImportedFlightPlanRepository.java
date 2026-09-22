package eapli.alsafe.antlr.flightplan.repositories;

import eapli.alsafe.antlr.flightplan.domain.FlightPlanId;
import eapli.alsafe.antlr.flightplan.domain.ImportedFlightPlan;
import eapli.framework.domain.repositories.DomainRepository;

public interface ImportedFlightPlanRepository extends DomainRepository<FlightPlanId, ImportedFlightPlan> {
}
