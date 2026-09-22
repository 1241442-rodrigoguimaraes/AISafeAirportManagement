package eapli.alsafe.aircraftModelMagnement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import java.util.Objects;
import java.io.Serializable;

@Embeddable
public class aircraftModelId implements ValueObject, Serializable, Comparable<aircraftModelId>{

    @Embedded
    private MakerName makerName;

    @Embedded
    private aircraftModelName modelName;

    protected aircraftModelId() {
    }

    public aircraftModelId(final MakerName makerName, final aircraftModelName modelName) {
        if (makerName == null || modelName == null) {
            throw new IllegalArgumentException("Maker name and model name cannot be null");
        }
        this.makerName = makerName;
        this.modelName = modelName;
    }

    public MakerName makerName() {
        return makerName;
    }

    public aircraftModelName modelName() {
        return modelName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof aircraftModelId)) return false;
        final aircraftModelId that = (aircraftModelId) o;
        return Objects.equals(this.makerName, that.makerName)
                && Objects.equals(this.modelName, that.modelName);
    }

    @Override
    public int compareTo(final aircraftModelId other) {
        if (other == null) return 1;

        int cmp = this.makerName.toString().compareTo(other.makerName.toString());
        if (cmp != 0) return cmp;

        return this.modelName.toString().compareTo(other.modelName.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.makerName, this.modelName);
    }

    @Override
    public String toString() {
        return String.format("%s %s", this.makerName, this.modelName);
    }




}
