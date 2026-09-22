package eapli.alsafe.remoteaccess.server.requesthandlers.pilot;

import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.flightPlan.application.FlightPlanController;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;

import java.nio.charset.StandardCharsets;

public class ListPilotRoutesRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(final Packet packet) {
        try {
            final FlightPlanController controller = new FlightPlanController();
            final Pilot pilot = controller.getAuthenticatedPilot();

            final StringBuilder response = new StringBuilder();
            for (final FlightRoute route : controller.getRoutesForPilot(pilot.company())) {
                if (!response.isEmpty()) {
                    response.append("|");
                }
                response.append(route.identity()).append(":").append(route);
            }

            final byte[] payload = response.toString().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 100, payload.length, payload);
        } catch (final Exception e) {
            final byte[] payload = e.getMessage().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, payload.length, payload);
        }
    }
}
