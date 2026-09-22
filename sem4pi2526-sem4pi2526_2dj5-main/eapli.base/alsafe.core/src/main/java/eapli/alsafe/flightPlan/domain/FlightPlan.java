package eapli.alsafe.flightPlan.domain;

import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "FLIGHT_PLAN")
public class FlightPlan implements AggregateRoot<FlightPlanID> {

    @EmbeddedId
    private FlightPlanID flightPlanId;

    @Version
    private Long version;

    @ManyToOne(optional = false)
    @Getter
    private FlightRoute route;

    @ManyToOne(optional = false)
    @Getter
    private Aircraft aircraft;

    @ManyToOne(optional = false)
    @Getter
    private Pilot pilot;

    @Column(nullable = false)
    @Getter
    private LocalDateTime departureDateTime;

    @Getter
    @Column(nullable = false, columnDefinition = "BOOLEAN NOT NULL DEFAULT FALSE")
    private boolean tested = false;

    @Embedded
    @Getter
    private FuelQuantity fuelQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Getter
    private FlightPlanStatus status;

    @ManyToOne(optional = true)
    @Getter
    private WeatherData weatherData;

    @Lob
    @Column(name = "Content", length = 10000)
    @Getter
    private String content;

    protected FlightPlan() {}

    private FlightPlan(FlightPlanID id, FlightRoute route, Aircraft aircraft,
                       Pilot pilot, LocalDateTime departureDateTime,
                       FuelQuantity fuelQuantity, String content) {
        this.flightPlanId = id;
        this.route = route;
        this.aircraft = aircraft;
        this.pilot = pilot;
        this.departureDateTime = departureDateTime;
        this.fuelQuantity = fuelQuantity;
        this.status = FlightPlanStatus.DRAFT;
        this.content = content;

    }

    public static FlightPlan create(FlightPlanID id, FlightRoute route,
                                    Aircraft aircraft, Pilot pilot,
                                    LocalDateTime departureDateTime,
                                    FuelQuantity fuelQuantity, String content) {
        Preconditions.noneNull(id, route, aircraft, pilot, departureDateTime, fuelQuantity);
        Preconditions.ensure(pilot.company().equals(route.getCompany()),"Pilot must belong to the same company as the route");
        Preconditions.ensure(pilot.company().equals(aircraft.company()),"Pilot must belong to the same company as the aircraft");
        aircraft.assertOperationalForNewFlightPlans();
        Preconditions.ensure(departureDateTime.isAfter(LocalDateTime.now()),"Departure must be in the future");

        return new FlightPlan(id, route, aircraft, pilot, departureDateTime, fuelQuantity, content);

    }

    public void submit() {
        if (status == FlightPlanStatus.DRAFT) this.status = FlightPlanStatus.SUBMITTED;
        else throw new IllegalStateException("Only flight plans in DRAFT status can be submitted.");
    }
    public void approve() {
        if (status == FlightPlanStatus.SUBMITTED) this.status = FlightPlanStatus.VALIDATED;
        else throw new IllegalStateException("Only flight plans in SUBMITTED status can be approved.");
    }
    public void reject(){
        if (status == FlightPlanStatus.SUBMITTED) this.status = FlightPlanStatus.REJECTED;
        else throw new IllegalStateException("Only flight plans in SUBMITTED status can be rejected.");
    }
    public void cancel(){
        if (status == FlightPlanStatus.DRAFT || status == FlightPlanStatus.SUBMITTED) this.status = FlightPlanStatus.CANCELLED;
        else throw new IllegalStateException("Only flight plans in DRAFT or SUBMITTED status can be cancelled.");
    }
    public void invalidate(){
        if (status == FlightPlanStatus.VALIDATED) this.status = FlightPlanStatus.DRAFT;
        else throw new IllegalStateException("Only flight plans in VALIDATED status can be invalidated.");
    }

    @Override
    public boolean sameAs(Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public FlightPlanID identity() {
        return flightPlanId;
    }

    public String toString() {
        return String.format("FlightPlan: %s", flightPlanId);
    }

    public void insertWeatherData(final WeatherData weatherData) {
        if (weatherData == null) {
            throw new IllegalArgumentException("Weather data cannot be null");
        }
        this.weatherData = weatherData;

        if (this.tested) {
            this.tested = false;
        }
    }

    /**
     * Marks the flight plan as tested (US085). Inserting new weather data voids this flag (US082).
     */
    public void markAsTested() {
        this.tested = true;
    }

    public Pilot pilot() {
        return pilot;
    }
}
