package eapli.alsafe.remoteaccess.server.requesthandlers.atcc;

import eapli.alsafe.aircraft.application.AddAircraftController;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;

import java.nio.charset.StandardCharsets;

public class AddAircraftRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(Packet request) {
        String payloadStr = new String(request.payload());
        String[] parts = payloadStr.split(";");

        if (parts.length != 7) {
            String errorMsg = "Invalid payload format. Expected: model;ID;country;economy;business;firstClass;crewCount";
            return new Packet((byte) 101, errorMsg.getBytes().length, errorMsg.getBytes());
        }

        try {
            String model       = parts[0];
            String registrationID = parts[1];
            String country       = parts[2];
            int economy          = Integer.parseInt(parts[3]);
            int business         = Integer.parseInt(parts[4]);
            int firstClass       = Integer.parseInt(parts[5]);
            int crewCount        = Integer.parseInt(parts[6]);

            AddAircraftController ctrl = new AddAircraftController();

            aircraftModel airModel = ctrl.getAircraftModelByName(model);

            boolean ok = ctrl.addAircraft(airModel, registrationID, country, economy, business, firstClass, crewCount);

            if (ok) {
                byte[] success = "Aircraft registered successfully.".getBytes(StandardCharsets.UTF_8);
                return new Packet((byte) 100, success.length, success);
            } else {
                byte[] fail = "Failed to register aircraft.".getBytes(StandardCharsets.UTF_8);
                return new Packet((byte) 101, fail.length, fail);
            }
        } catch (IllegalArgumentException e) {
            byte[] err = e.getMessage().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, err.length, err);
        }
    }
}
