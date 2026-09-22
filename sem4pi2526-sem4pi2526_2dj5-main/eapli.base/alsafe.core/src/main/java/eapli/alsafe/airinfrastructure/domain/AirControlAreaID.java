package eapli.alsafe.airinfrastructure.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;

@Embeddable
@EqualsAndHashCode
public class AirControlAreaID implements ValueObject, Comparable<AirControlAreaID> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private final String id;

    public AirControlAreaID(final Long sequence) {
        Preconditions.nonNull(sequence, "Sequence cannot be null.");
        Preconditions.ensure(sequence > 0, "Sequence must be positive.");

        this.id = String.format("ACA-%04d", sequence);
    }

    protected AirControlAreaID() {
        // for ORM
        this.id = null;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int compareTo(AirControlAreaID o) {
        assert id != null && o.id != null;
        return id.compareTo(o.id);
    }

    public AirControlAreaID getAirControlAreaID() {
        return this;
    }
}
