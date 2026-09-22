package eapli.alsafe.remoteaccess.server.requesthandlers.pilot;

import eapli.alsafe.flightPlan.application.FlightPlanController;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.flightPlan.domain.FlightPlanStatus;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;

import java.nio.charset.StandardCharsets;

public class ValidateFlightPlanRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(final Packet packet) {
        try {
            final String flightPlanId = new String(packet.payload(), StandardCharsets.UTF_8).trim();
            final FlightPlanController controller = new FlightPlanController();
            final FlightPlan plan = findFlightPlan(controller, flightPlanId);

            final FlightPlanStatus status = controller.validatePlan(plan);
            return success("Flight plan validation completed: " + plan.identity() + " -> " + status);
        } catch (final Exception e) {
            return error(e.getMessage());
        }
    }

    private FlightPlan findFlightPlan(final FlightPlanController controller, final String flightPlanId) {
        for (final FlightPlan plan : controller.getFlightPlans()) {
            if (plan.identity().toString().equalsIgnoreCase(flightPlanId)) {
                return plan;
            }
        }
        throw new IllegalArgumentException("Flight plan not found for authenticated pilot: " + flightPlanId);
    }

    private Packet success(final String message) {
        final byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        return new Packet((byte) 100, payload.length, payload);
    }

    private Packet error(final String message) {
        final byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        return new Packet((byte) 101, payload.length, payload);
    }
}
