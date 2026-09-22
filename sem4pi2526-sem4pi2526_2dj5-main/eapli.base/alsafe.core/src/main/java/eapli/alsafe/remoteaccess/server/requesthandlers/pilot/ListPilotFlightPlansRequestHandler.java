package eapli.alsafe.remoteaccess.server.requesthandlers.pilot;

import eapli.alsafe.flightPlan.application.FlightPlanController;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

public class ListPilotFlightPlansRequestHandler implements RemoteAccessRequestHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public Packet handle(final Packet packet) {
        try {
            final FlightPlanController controller = new FlightPlanController();
            final StringBuilder response = new StringBuilder();

            for (final FlightPlan plan : controller.getFlightPlans()) {
                if (!response.isEmpty()) {
                    response.append("|");
                }
                response.append(plan.identity()).append(";")
                        .append(plan.getRoute().identity()).append(";")
                        .append(plan.getAircraft().identity()).append(";")
                        .append(plan.getDepartureDateTime().format(FORMATTER)).append(";")
                        .append(plan.getFuelQuantity().getFuelQuantity()).append(";")
                        .append(plan.getFuelQuantity().getFuelUnit()).append(";")
                        .append(plan.getStatus());
            }

            final byte[] payload = response.toString().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 100, payload.length, payload);
        } catch (final Exception e) {
            final byte[] payload = e.getMessage().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, payload.length, payload);
        }
    }
}
