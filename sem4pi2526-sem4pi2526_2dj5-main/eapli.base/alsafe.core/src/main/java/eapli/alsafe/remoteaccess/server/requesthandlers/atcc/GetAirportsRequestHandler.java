package eapli.alsafe.remoteaccess.server.requesthandlers.atcc;

import eapli.alsafe.airinfrastructure.application.CreateFlightRouteController;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class GetAirportsRequestHandler implements RemoteAccessRequestHandler {
    @Override
    public Packet handle(Packet request) {
        CreateFlightRouteController ctrl = new CreateFlightRouteController();
        List<String> airports = ctrl.getAllAirportsList();

        if (airports.isEmpty() || airports.size() == 1) return new Packet((byte) 101, 0, null);

        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (String ap : airports) {
            sb.append(index++).append(" - ").append(ap).append("\n");
        }

        byte[] payload = sb.toString().getBytes(StandardCharsets.UTF_8);
        return new Packet((byte) 100, payload.length, payload);
    }
}
