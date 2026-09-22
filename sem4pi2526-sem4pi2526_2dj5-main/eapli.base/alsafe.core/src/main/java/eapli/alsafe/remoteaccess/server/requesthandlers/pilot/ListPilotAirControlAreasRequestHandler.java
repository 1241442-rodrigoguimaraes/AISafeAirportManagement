package eapli.alsafe.remoteaccess.server.requesthandlers.pilot;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;
import eapli.alsafe.weather.application.ConsultWeatherDataController;

import java.nio.charset.StandardCharsets;

public class ListPilotAirControlAreasRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(final Packet request) {
        try {
            final ConsultWeatherDataController controller = new ConsultWeatherDataController();
            final Iterable<AirControlArea> areas = controller.getAirControlAreas();

            final StringBuilder response = new StringBuilder();
            for (final AirControlArea area : areas) {
                if (!response.isEmpty()) {
                    response.append("|");
                }
                response.append(area.identity()).append(":").append(area.getName());
            }

            final byte[] payload = response.toString().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 100, payload.length, payload);
        } catch (final Exception e) {
            final byte[] payload = e.getMessage().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, payload.length, payload);
        }
    }
}
