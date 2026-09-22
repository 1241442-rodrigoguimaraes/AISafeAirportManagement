package eapli.alsafe.persistence.impl.inmemory;

import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.flightPlan.domain.FlightPlanID;
import eapli.alsafe.flightPlan.domain.FlightPlanStatus;
import eapli.alsafe.flightPlan.repositories.FlightPlanRepository;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Set;

public class InMemoryFlightPlanRepository extends InMemoryDomainRepository<FlightPlan, FlightPlanID> implements FlightPlanRepository {

    public InMemoryFlightPlanRepository() {
        super();
    }

    @Override
    public Iterable<FlightPlan> findBySystemUser(SystemUser user) {
        return match(e -> e.getPilot().user().equals(user));
    }

    @Override
    public Iterable<FlightPlan> findAssignedFlightPlansByPilot(final Pilot pilot) {
        final Set<FlightPlanStatus> assignedStatuses = EnumSet.of(
                FlightPlanStatus.DRAFT, FlightPlanStatus.SUBMITTED, FlightPlanStatus.VALIDATED);
        return match(e -> e.getPilot().equals(pilot) && assignedStatuses.contains(e.getStatus()));
    }

    @Override
    public Iterable<FlightPlan> findByPilot(final SystemUser pilot) {
        return match(e -> e.getPilot().user().equals(pilot));
    }

    @Override
    public Iterable<FlightPlan> findByFlightRouteAndAfterDate(final FlightRoute route, final String date) {
        LocalDateTime fromDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay();
        return match(e -> e.getRoute().equals(route) && !e.getDepartureDateTime().isBefore(fromDate));
    }
}
