package eapli.alsafe.aircraft.domain;

import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "fleet_aircraft")
public class Aircraft implements AggregateRoot<RegistrationID>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private RegistrationID registrationId;

    @Version
    private Long version;

    @ManyToOne(optional = false)
    private aircraftModel aircraftModel;

    @ManyToOne(optional = false)
    private AirTransportCompany company;

    @Embedded
    private CabinConfiguration cabinConfiguration;

    @Embedded
    private AircraftCountry registrationCountry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MaintenanceStatus maintenanceStatus;

    @OneToMany(mappedBy = "aircraft", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrewElement> crewElements = new ArrayList<>();

    protected Aircraft() {
    }

    public static Aircraft create(final aircraftModel model, final RegistrationID registrationId,
                                  final AircraftCountry country, final CabinConfiguration cabinConfig,
                                  final List<CrewElement> crew, final AirTransportCompany company) {
        if (model == null || registrationId == null || country == null || cabinConfig == null || company == null) {
            throw new IllegalArgumentException("Model, registration, country, cabin configuration and company are required");
        }
        if (crew == null || crew.isEmpty()) {
            throw new IllegalArgumentException("At least one crew element is required");
        }
        final Aircraft aircraft = new Aircraft();
        aircraft.registrationId = registrationId;
        aircraft.aircraftModel = model;
        aircraft.company = company;
        aircraft.cabinConfiguration = cabinConfig;
        aircraft.registrationCountry = country;
        aircraft.maintenanceStatus = MaintenanceStatus.OPERATIONAL;
        aircraft.crewElements = new ArrayList<>();
        for (final CrewElement element : crew) {
            element.setAircraft(aircraft);
            aircraft.crewElements.add(element);
        }
        return aircraft;
    }

    public aircraftModel model() {
        return aircraftModel;
    }

    public AirTransportCompany company() {
        return company;
    }

    public CabinConfiguration cabinConfiguration() {
        return cabinConfiguration;
    }

    public AircraftCountry registrationCountry() {
        return registrationCountry;
    }

    public MaintenanceStatus maintenanceStatus() {
        return maintenanceStatus;
    }

    /**
     * Retires the aircraft from active service (US071). Irreversible for scheduling new flights (AC3).
     */
    public void decommission() {
        if (maintenanceStatus == MaintenanceStatus.DECOMMISSIONED) {
            throw new IllegalStateException("Aircraft is already retired.");
        }
        this.maintenanceStatus = MaintenanceStatus.DECOMMISSIONED;
    }

    /**
     * Guard for future flight-plan creation (US080 / AC2).
     */
    public void assertOperationalForNewFlightPlans() {
        if (maintenanceStatus == MaintenanceStatus.DECOMMISSIONED) {
            throw new IllegalStateException("Aircraft is retired");
        }
    }

    public List<CrewElement> crewElements() {
        return Collections.unmodifiableList(crewElements);
    }

    @Override
    public RegistrationID identity() {
        return registrationId;
    }

    @Override
    public boolean sameAs(final Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final Aircraft that)) {
            return false;
        }
        return Objects.equals(registrationId, that.registrationId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(registrationId);
    }

    @Override
    public String toString() {
        return String.format("Aircraft: %s | Model: %s | Company: %s | Status: %s",
                registrationId, aircraftModel, company.getName(), maintenanceStatus);
    }
}
