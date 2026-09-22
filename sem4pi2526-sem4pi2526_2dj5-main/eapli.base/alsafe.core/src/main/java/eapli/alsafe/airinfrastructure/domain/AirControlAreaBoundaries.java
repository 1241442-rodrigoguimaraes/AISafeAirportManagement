package eapli.alsafe.airinfrastructure.domain;

import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serial;
import java.util.List;

@Embeddable
@EqualsAndHashCode
public class AirControlAreaBoundaries implements ValueObject {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    @ElementCollection
    private final List<Coordinate> boundaries;

    public AirControlAreaBoundaries(final List<Coordinate> boundaries) {
        Preconditions.nonNull(boundaries, "Boundaries cannot be null.");
        Preconditions.ensure(boundaries.size() == 4, "Boundaries must have 4 coordinates.");

        this.boundaries = boundaries;
    }

    protected AirControlAreaBoundaries() {
        // for ORM
        boundaries = null;
    }

    @Override
    public String toString() {
        return boundaries.toString();
    }
}
