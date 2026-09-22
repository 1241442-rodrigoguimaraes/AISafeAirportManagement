package eapli.alsafe.engineModelMagnement.Domain;

import eapli.alsafe.aircraftModelMagnement.domain.MakerName;



import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.util.Objects;
import java.io.Serializable;

@Embeddable
public class engineModelId implements ValueObject,Serializable, Comparable<engineModelId> {

    @Embedded
    private engineModelName modelName;

    @Embedded
    private MakerName makerName;

    protected engineModelId() {
    }

    public engineModelId(final engineModelName modelName, final MakerName makerName) {
        if (modelName == null || makerName == null) {
            throw new IllegalArgumentException("Model name and maker name cannot be null");
        }
        this.modelName = modelName;
        this.makerName = makerName;
    }

    public engineModelName modelName() {
        return modelName;
    }

    public MakerName makerName() {
        return makerName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof engineModelId)) return false;
        final engineModelId that = (engineModelId) o;
        return Objects.equals(this.modelName, that.modelName)
                && Objects.equals(this.makerName, that.makerName);
    }

    @Override
    public int compareTo(final engineModelId other) {
        if (other == null) return 1;

        int cmp = this.modelName.toString().compareTo(other.modelName.toString());
        if (cmp != 0) return cmp;

        return this.makerName.toString().compareTo(other.makerName.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.modelName, this.makerName);
    }

     @Override
    public String toString() {
         return String.format("%s - %s", this.makerName, this.modelName);
    }
}
