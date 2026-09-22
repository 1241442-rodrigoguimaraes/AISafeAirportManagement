package eapli.alsafe.aircraftModelMagnement.domain;

import eapli.alsafe.engineModelMagnement.Domain.engineModel;
import eapli.alsafe.engineModelMagnement.Domain.engineType;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.repositories.IntegrityViolationException;
import jakarta.persistence.*;

import java.io.Serial;
import java.util.*;

@Entity
@Table(name = "aircraft_model")
public class aircraftModel implements AggregateRoot<aircraftModelId> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @EmbeddedId
    @AttributeOverrides({
    @AttributeOverride(name = "modelName.name", column = @Column(name = "model_name_id")),
    @AttributeOverride(name = "makerName.name", column = @Column(name = "maker_name_id"))})
    private aircraftModelId id;

    @Embedded
    private aircraftModelName arModelName;

    @ManyToOne(optional = false)
    private Maker maker;

    @Enumerated(EnumType.STRING)
    private aircraftModelType type;

    @Enumerated(EnumType.STRING)
    private engineType motorization;

    @Embedded
    private maximumRange maxRange;

    @Embedded
    private aircraftModelPhysicsData physicsData;

    @ManyToMany
    private Set<engineModel> certifiedEngines = new HashSet<>();

    protected aircraftModel() {}

    public aircraftModel(final aircraftModelName arModelName, final Maker maker,
                         final aircraftModelType type, final engineType motorization,
                         final maximumRange maxRange, final aircraftModelPhysicsData physicsData,
                         final Set<engineModel> certifiedEngines) {

        validate(arModelName, maker, type, motorization, maxRange, physicsData);

        this.id = new aircraftModelId(maker.identity(), arModelName);
        this.arModelName = arModelName;
        this.maker = maker;
        this.type = type;
        this.motorization = motorization;
        this.maxRange = maxRange;
        this.physicsData = physicsData;

        for (engineModel engineModel : certifiedEngines) {
            addCertifiedEngine(engineModel);
        }
    }

    private void validate(final aircraftModelName arModelname, final Maker maker,
                          final aircraftModelType type, final engineType motorization,
                          final maximumRange maxRange, final aircraftModelPhysicsData physicsData) {

        if (arModelname == null) {
            throw new IllegalArgumentException(
                    "Aircraft model name cannot be null");
        }

        if (maker == null) {
            throw new IllegalArgumentException(
                    "Maker cannot be null");
        }

        if (type == null) {
            throw new IllegalArgumentException(
                    "Aircraft model type cannot be null");
        }

        if (motorization == null) throw new IllegalArgumentException("Motorization cannot be null");

        if (maxRange == null) {
            throw new IllegalArgumentException(
                    "Maximum range cannot be null");
        }

        if (physicsData == null) {
            throw new IllegalArgumentException(
                    "Physics data cannot be null");
        }
    }

    public void addCertifiedEngine(final engineModel engineModel) {
        if (engineModel == null) throw new IllegalArgumentException("The engine model cannot be null");
        if (engineModel.getType() != this.motorization) throw new IntegrityViolationException("Engine type " + engineModel.getType() + " is incompatible with aircraft motorization " + this.motorization);
        if (this.certifiedEngines.contains(engineModel)) throw new IntegrityViolationException("The engine model is already certified");

        this.certifiedEngines.add(engineModel);
    }

    public void removeCertifiedEngine(final engineModel engineModel) {

        if (engineModel == null) {
            throw new IllegalArgumentException(
                    "Engine model cannot be null");
        }

        if (certifiedEngines.size() <= 1) {
            throw new IllegalStateException(
                    "Aircraft model must retain at least one certified engine");
        }

        certifiedEngines.remove(engineModel);
    }

    public Set<engineModel> certifiedEngines() {
        return certifiedEngines;
    }

    public aircraftModelName name() {
        return arModelName;
    }

    public Maker maker() {
        return maker;
    }

    public aircraftModelType type() {
        return type;
    }

    public engineType motorization() {
        return motorization;
    }

    public maximumRange maximumRange() {
        return maxRange;
    }

    public aircraftModelPhysicsData physicsData() {
        return physicsData;
    }

    /**
     * Heuristic maximum passenger seats from structural payload (MZFW − empty weight), ~90 kg per passenger.
     */
    public int maxPassengerSeats() {
        final double payloadKg = physicsData.maximumZeroFuelWeight() - physicsData.emptyWeight();
        return Math.max(1, (int) Math.floor(payloadKg / 90.0));
    }

    @Override
    public aircraftModelId identity() {
        return id;
    }

    @Override
    public boolean sameAs(final Object other) {

        if (!(other instanceof aircraftModel that)) {
            return false;
        }

        return Objects.equals(this.id, that.id);
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) return true;

        if (!(o instanceof aircraftModel that)) return false;

        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return this.arModelName.toString();
    }
}
