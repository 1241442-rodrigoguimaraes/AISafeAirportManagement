package eapli.alsafe.aircraft.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CabinConfiguration implements Serializable {

    @Column(name = "seats_economy", nullable = false)
    private int economySeats;

    @Column(name = "seats_business", nullable = false)
    private int businessSeats;

    @Column(name = "seats_first", nullable = false)
    private int firstClassSeats;

    protected CabinConfiguration() {
    }

    /**
     * @param maxTotalSeatsAllowed maximum passenger seats allowed for this aircraft model
     */
    public CabinConfiguration(final int economySeats, final int businessSeats, final int firstClassSeats,
                              final int maxTotalSeatsAllowed) {
        if (economySeats < 0 || businessSeats < 0 || firstClassSeats < 0) {
            throw new IllegalArgumentException("Seat counts cannot be negative");
        }
        final int total = economySeats + businessSeats + firstClassSeats;
        if (total > maxTotalSeatsAllowed) {
            throw new IllegalArgumentException(
                    "Total cabin seats (" + total + ") exceeds aircraft model capacity (" + maxTotalSeatsAllowed + ")");
        }
        this.economySeats = economySeats;
        this.businessSeats = businessSeats;
        this.firstClassSeats = firstClassSeats;
    }

    public int economySeats() {
        return economySeats;
    }

    public int businessSeats() {
        return businessSeats;
    }

    public int firstClassSeats() {
        return firstClassSeats;
    }

    public int totalSeats() {
        return economySeats + businessSeats + firstClassSeats;
    }

    @Override
    public String toString() {
        return String.format("Economy=%d, Business=%d, First=%d (total=%d)",
                economySeats, businessSeats, firstClassSeats, totalSeats());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CabinConfiguration that = (CabinConfiguration) o;
        return economySeats == that.economySeats && businessSeats == that.businessSeats
                && firstClassSeats == that.firstClassSeats;
    }

    @Override
    public int hashCode() {
        return Objects.hash(economySeats, businessSeats, firstClassSeats);
    }
}
