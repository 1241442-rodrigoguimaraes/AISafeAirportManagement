package eapli.alsafe.persistence.impl.jpa;

import eapli.alsafe.Application;
import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.flightPlan.domain.FlightPlanID;
import eapli.alsafe.flightPlan.domain.FlightPlanStatus;
import eapli.alsafe.flightPlan.repositories.FlightPlanRepository;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JpaFlightPlanRepository extends JpaAutoTxRepository<FlightPlan, FlightPlanID, FlightPlanID> implements FlightPlanRepository {

    public JpaFlightPlanRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "flightPlanID");
    }

    public JpaFlightPlanRepository(final TransactionalContext autoTx) {
        super(autoTx, "flightPlanID");
    }

    @Override
    public Iterable<FlightPlan> findBySystemUser(SystemUser user) {
        Map<String, Object> params = new HashMap<>();
        params.put("user", user);
        return match("e.pilot.systemUser = :user", params);
    }

    @Override
    public Iterable<FlightPlan> findAssignedFlightPlansByPilot(final Pilot pilot) {
        final Map<String, Object> params = new HashMap<>();
        params.put("pilot", pilot);
        params.put("statuses", List.of(FlightPlanStatus.DRAFT, FlightPlanStatus.SUBMITTED,
                FlightPlanStatus.VALIDATED));
        return match("e.pilot = :pilot AND e.status IN :statuses", params);
    }

    @Override
    public Iterable<FlightPlan> findByPilot(final SystemUser pilot) {
        return findBySystemUser(pilot);
    }

    @Override
    public Iterable<FlightPlan> findByFlightRouteAndAfterDate(final FlightRoute flightRoute, final String date) {
        final Map<String, Object> params = new HashMap<>();
        params.put("flightRoute", flightRoute);
        LocalDateTime fromDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay();
        params.put("date", fromDate);
        return match("e.route = :flightRoute AND e.departureDateTime >= :date", params);
    }
}
