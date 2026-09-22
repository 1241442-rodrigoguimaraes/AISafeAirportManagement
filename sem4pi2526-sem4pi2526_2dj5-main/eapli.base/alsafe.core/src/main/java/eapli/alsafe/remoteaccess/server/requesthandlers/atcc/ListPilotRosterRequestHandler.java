package eapli.alsafe.remoteaccess.server.requesthandlers.atcc;

import eapli.alsafe.pilotmanagement.application.ListPilotRosterController;
import eapli.alsafe.pilotmanagement.application.PilotDTO;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;

import java.nio.charset.StandardCharsets;

public class ListPilotRosterRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(Packet request) {
        try {
            ListPilotRosterController ctrl = new ListPilotRosterController();
            Iterable<PilotDTO> roster = ctrl.activePilotRoster();

            StringBuilder sb = new StringBuilder();
            boolean hasPilots = false;

            for (PilotDTO pilot : roster) {
                sb.append(pilot).append("\n");
                hasPilots = true;
            }

            if (!hasPilots) {
                byte[] empty = "No active pilots found in your company's roster.".getBytes(StandardCharsets.UTF_8);
                return new Packet((byte) 101, empty.length, empty);
            }

            byte[] payload = sb.toString().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 100, payload.length, payload);

        } catch (Exception e) {
            byte[] errMsg = e.getMessage().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, errMsg.length, errMsg);
        }
    }
}
