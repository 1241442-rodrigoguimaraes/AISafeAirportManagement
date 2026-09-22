package eapli.alsafe.remoteaccess.server.requesthandlers.atcc;

import eapli.alsafe.pilotmanagement.application.RemovePilotController;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;

import java.nio.charset.StandardCharsets;

public class RemovePilotRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(Packet request) {
        String payload = new String(request.payload(), StandardCharsets.UTF_8).trim();

        if (payload.isEmpty()) {
            byte[] err = "Invalid payload: pilot ID is required.".getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, err.length, err);
        }

        try {
            Long pilotId = Long.parseLong(payload);
            RemovePilotController ctrl = new RemovePilotController();
            Pilot deactivated = ctrl.deactivatePilot(pilotId);

            String msg = "Pilot '" + deactivated.name() + "' deactivated successfully.";
            byte[] success = msg.getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 100, success.length, success);

        } catch (NumberFormatException e) {
            byte[] err = ("Invalid pilot ID format: " + payload).getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, err.length, err);
        } catch (SecurityException e) {
            byte[] err = "Access denied: pilot belongs to another company.".getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, err.length, err);
        } catch (Exception e) {
            byte[] err = e.getMessage().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, err.length, err);
        }
    }
}
