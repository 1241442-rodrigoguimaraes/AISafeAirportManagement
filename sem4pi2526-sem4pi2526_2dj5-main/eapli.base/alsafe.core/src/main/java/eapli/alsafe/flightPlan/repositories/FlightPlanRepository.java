package eapli.alsafe.flightPlan.repositories;

import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.flightPlan.domain.FlightPlanID;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.framework.domain.repositories.DomainRepository;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

public interface FlightPlanRepository extends DomainRepository<FlightPlanID, FlightPlan> {
    Iterable<FlightPlan> findBySystemUser(SystemUser user);

    Iterable<FlightPlan> findAssignedFlightPlansByPilot(Pilot pilot);

    Iterable<FlightPlan> findByPilot(SystemUser pilot);

    Iterable<FlightPlan> findByFlightRouteAndAfterDate(FlightRoute flightRoute, String date);
}
