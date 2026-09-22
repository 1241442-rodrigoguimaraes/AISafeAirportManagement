package flightplan;

import eapli.alsafe.antlr.domain.FlightPlanDSL;
import eapli.alsafe.antlr.domain.FlightProfile;
import eapli.alsafe.antlr.domain.FuelInfo;
import eapli.alsafe.antlr.domain.Leg;
import eapli.alsafe.antlr.domain.ProfileEntry;
import eapli.alsafe.antlr.flightplan.application.FlightPlanMapper;
import eapli.alsafe.antlr.flightplan.domain.FlightPlanId;
import eapli.alsafe.antlr.flightplan.domain.ImportedFlightPlan;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanDSLMapperTest {

    @Test
    void toEntity_shouldMapAllScalarFields() {
        final FlightPlanDSL dsl = new FlightPlanDSL(
                "TP123",
                FlightPlanDSL.FlightType.CHARTER,
                "ROUTE1",
                "2025-05-01",
                "09:00",
                "CS-TUA",
                List.of(new Leg("OPO", "MAD", new FuelInfo(4200, FuelInfo.FuelUnit.KG),
                        new FlightProfile(List.of(), 0, "knots", List.of()), List.of())));

        final ImportedFlightPlan entity = FlightPlanMapper.toEntity(dsl);

        assertEquals(new FlightPlanId("TP123"), entity.flightPlanId());
        assertEquals(ImportedFlightPlan.FlightType.CHARTER, entity.type());
        assertEquals("ROUTE1", entity.routeId());
        assertEquals("2025-05-01", entity.scheduledDate());
        assertEquals("09:00", entity.scheduledTime());
        assertEquals("CS-TUA", entity.aircraftRegistration());
    }
}
