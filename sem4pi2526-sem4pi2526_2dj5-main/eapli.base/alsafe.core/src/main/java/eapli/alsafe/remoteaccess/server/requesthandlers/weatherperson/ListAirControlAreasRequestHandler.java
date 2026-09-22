package eapli.alsafe.remoteaccess.server.requesthandlers.weatherperson;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;
import eapli.alsafe.weather.application.RegisterWeatherDataController;

import java.nio.charset.StandardCharsets;

public class ListAirControlAreasRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(Packet request) {
        try {
            RegisterWeatherDataController ctrl = new RegisterWeatherDataController();
            Iterable<AirControlArea> areas = ctrl.getAirControlAreas();

            StringBuilder sb = new StringBuilder();
            for (AirControlArea area : areas) {
                if (!sb.isEmpty()) sb.append("|");
                sb.append(area.identity()).append(":").append(area.getName());
            }

            byte[] payload = sb.toString().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 100, payload.length, payload);
        } catch (Exception e) {
            byte[] err = e.getMessage().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, err.length, err);
        }
    }
}