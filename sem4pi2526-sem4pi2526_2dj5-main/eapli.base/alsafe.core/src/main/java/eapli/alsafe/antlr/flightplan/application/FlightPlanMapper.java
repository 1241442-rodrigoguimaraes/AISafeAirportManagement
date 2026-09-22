package eapli.alsafe.antlr.flightplan.application;

import eapli.alsafe.antlr.domain.FlightPlanDSL;
import eapli.alsafe.antlr.flightplan.domain.FlightPlanId;
import eapli.alsafe.antlr.flightplan.domain.ImportedFlightPlan;

/**
 * Maps DSL parse results ({@link FlightPlanDSL}) to persistence aggregates ({@link ImportedFlightPlan}).
 */
public final class FlightPlanMapper {

    private FlightPlanMapper() {}

    public static ImportedFlightPlan toEntity(final FlightPlanDSL dslPlan) {
        return new ImportedFlightPlan(
                new FlightPlanId(dslPlan.getFlightId()),
                toEntityType(dslPlan.getType()),
                dslPlan.getRouteId(),
                dslPlan.getDate(),
                dslPlan.getTime(),
                dslPlan.getAircraft());
    }

    private static ImportedFlightPlan.FlightType toEntityType(final FlightPlanDSL.FlightType type) {
        return switch (type) {
            case REGULAR -> ImportedFlightPlan.FlightType.REGULAR;
            case CHARTER -> ImportedFlightPlan.FlightType.CHARTER;
        };
    }
}
