package eapli.alsafe.flightPlan.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
public final class FlightPlanID implements Comparable<FlightPlanID> {

    @Column(name = "FLIGHT_ID")
    @Getter
    private String flightId;

    protected FlightPlanID() {
    }

    public FlightPlanID(String flightId) {
        if (!validFlightPlanIDFormat(flightId)) {
            throw new IllegalArgumentException("Invalid flight plan ID format. It should be 2 letters followed by 1 to 4 digits and an optional letter.");
        }
        this.flightId = flightId;
    }

    @Override
    public int compareTo(FlightPlanID o) {
        return this.flightId.compareTo(o.flightId);
    }

    public static boolean validFlightPlanIDFormat(String flightId){
        if (flightId != null && !flightId.isBlank()) {
            return flightId.matches("(?i)[a-zA-Z]{2}[0-9]{1,4}[a-zA-Z]?");
        }else {
            return false;
        }
    }

    public String toString() {
        return flightId;
    }
}
