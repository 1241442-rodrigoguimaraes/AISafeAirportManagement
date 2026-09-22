package eapli.alsafe.remoteaccess.server.requesthandlers.atcc;

import eapli.alsafe.aircraft.application.DecommissionAircraftController;
import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.aircraft.domain.RegistrationID;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;

public class DecommissionAircraftRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(Packet request) {
        String payloadStr = new String(request.payload());
        String[] parts = payloadStr.split(";");

        if (parts.length != 1) {
            String errorMsg = "Invalid payload format. Expected: aircraftId";
            return new Packet((byte) 101, errorMsg.getBytes().length, errorMsg.getBytes());
        }

        try {
            String aircraftId = parts[0];

            DecommissionAircraftController ctrl = new DecommissionAircraftController();
            RegistrationID aircraftIdObj = new RegistrationID(aircraftId);
            Aircraft decommissioned = ctrl.decommissionAircraft(aircraftIdObj);

            if (decommissioned != null) {
                byte[] success = "Aircraft decommissioned successfully.".getBytes();
                return new Packet((byte) 100, success.length, success);
            } else {
                byte[] fail = "Failed to decommission aircraft.".getBytes();
                return new Packet((byte) 101, fail.length, fail);
            }
        } catch (Exception e) {
            byte[] err = e.getMessage().getBytes();
            return new Packet((byte) 101, err.length, err);
        }
    }
}
