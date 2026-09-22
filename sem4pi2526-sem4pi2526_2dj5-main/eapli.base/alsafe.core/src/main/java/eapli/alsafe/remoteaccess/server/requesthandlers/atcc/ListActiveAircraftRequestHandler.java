package eapli.alsafe.remoteaccess.server.requesthandlers.atcc;

import eapli.alsafe.aircraft.application.DecommissionAircraftController;
import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ListActiveAircraftRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(Packet request) {
        DecommissionAircraftController ctrl = new DecommissionAircraftController();
        List<Aircraft> activeFleet = ctrl.activeFleetForCurrentCompany();

        if (activeFleet.isEmpty()) return new Packet((byte) 101, 0, null);

        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (Aircraft ac : activeFleet) {
            sb.append(index++).append(" - ").append(ac).append("\n");
        }

        byte[] payload = sb.toString().getBytes(StandardCharsets.UTF_8);
        return new Packet((byte) 100, payload.length, payload);
    }
}
