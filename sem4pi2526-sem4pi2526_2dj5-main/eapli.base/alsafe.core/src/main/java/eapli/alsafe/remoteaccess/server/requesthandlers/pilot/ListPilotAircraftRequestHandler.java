package eapli.alsafe.remoteaccess.server.requesthandlers.pilot;

import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.flightPlan.application.FlightPlanController;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;

import java.nio.charset.StandardCharsets;

public class ListPilotAircraftRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(final Packet packet) {
        try {
            final FlightPlanController controller = new FlightPlanController();
            final Pilot pilot = controller.getAuthenticatedPilot();

            final StringBuilder response = new StringBuilder();
            for (final Aircraft aircraft : controller.getAircraftForCompany(pilot.company())) {
                if (!response.isEmpty()) {
                    response.append("|");
                }
                response.append(aircraft.identity()).append(":")
                        .append("Aircraft ").append(aircraft.identity())
                        .append(" - Model ").append(oneLine(aircraft.model().toString()))
                        .append(" - Company ").append(oneLine(aircraft.company().getName()))
                        .append(" - Status ").append(aircraft.maintenanceStatus());
            }

            final byte[] payload = response.toString().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 100, payload.length, payload);
        } catch (final Exception e) {
            final byte[] payload = e.getMessage().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, payload.length, payload);
        }
    }

    private String oneLine(final String value) {
        return value.replace('|', '/')
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }
}
