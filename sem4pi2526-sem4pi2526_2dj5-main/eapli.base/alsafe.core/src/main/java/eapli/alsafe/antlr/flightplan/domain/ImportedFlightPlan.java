package eapli.alsafe.antlr.flightplan.domain;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import jakarta.persistence.*;

import java.io.Serial;

@Entity
@Table(name = "IMPORTED_FLIGHT_PLAN")
public class ImportedFlightPlan implements AggregateRoot<FlightPlanId> {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum FlightType { REGULAR, CHARTER }

    @Version
    private Long version;

    @EmbeddedId
    private FlightPlanId flightPlanId;

    @Enumerated(EnumType.STRING)
    @Column(name = "FLIGHT_TYPE", nullable = false)
    private FlightType type;

    @Column(name = "ROUTE_ID", nullable = false)
    private String routeId;

    @Column(name = "SCHEDULED_DATE", nullable = false)
    private String scheduledDate;

    @Column(name = "SCHEDULED_TIME", nullable = false)
    private String scheduledTime;

    @Column(name = "AIRCRAFT_REGISTRATION", nullable = false)
    private String aircraftRegistration;

    protected ImportedFlightPlan() {}

    public ImportedFlightPlan(final FlightPlanId flightPlanId,
                              final FlightType type,
                              final String routeId,
                              final String scheduledDate,
                              final String scheduledTime,
                              final String aircraftRegistration) {
        if (flightPlanId == null || type == null || routeId == null
                || scheduledDate == null || scheduledTime == null || aircraftRegistration == null) {
            throw new IllegalArgumentException("All flight plan attributes are required.");
        }
        this.flightPlanId = flightPlanId;
        this.type = type;
        this.routeId = routeId;
        this.scheduledDate = scheduledDate;
        this.scheduledTime = scheduledTime;
        this.aircraftRegistration = aircraftRegistration;
    }

    public FlightPlanId flightPlanId() {
        return flightPlanId;
    }

    public FlightType type() {
        return type;
    }

    public String routeId() {
        return routeId;
    }

    public String scheduledDate() {
        return scheduledDate;
    }

    public String scheduledTime() {
        return scheduledTime;
    }

    public String aircraftRegistration() {
        return aircraftRegistration;
    }

    @Override
    public boolean equals(final Object o) {
        return DomainEntities.areEqual(this, o);
    }

    @Override
    public int hashCode() {
        return DomainEntities.hashCode(this);
    }

    @Override
    public FlightPlanId identity() {
        return flightPlanId;
    }

    @Override
    public boolean sameAs(final Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public String toString() {
        return String.format(
                "ImportedFlightPlan[id=%s, type=%s, route=%s, date=%s %s, aircraft=%s]",
                flightPlanId, type, routeId, scheduledDate, scheduledTime, aircraftRegistration);
    }
}
