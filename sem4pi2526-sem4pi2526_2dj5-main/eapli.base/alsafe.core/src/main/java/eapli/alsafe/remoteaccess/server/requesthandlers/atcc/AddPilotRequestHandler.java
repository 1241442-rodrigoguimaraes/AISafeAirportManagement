package eapli.alsafe.remoteaccess.server.requesthandlers.atcc;

import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.pilotmanagement.application.AddPilotController;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class AddPilotRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(Packet request) {
        String pilotData = new String(request.payload(), StandardCharsets.UTF_8);
        String[] parts = pilotData.split(";");

        if (parts.length != 5) {
            byte[] errMsg = "Invalid payload format. Expected: name;email;password;selectedModels".getBytes();
            return new Packet((byte) 101, errMsg.length, errMsg);
        }

        String name = parts[0];
        String email = parts[1];
        String phone = parts[2];
        String password = parts[3];
        String selectedModels = parts[4];

        try {
            AddPilotController ctrl = new AddPilotController();
            Set<aircraftModel> models = createSet(selectedModels, ctrl);

            Pilot newPilot = ctrl.addPilot(name, email, phone, password, models);

            if (newPilot != null) {
                byte[] success = "Pilot added successfully.".getBytes();
                return new Packet((byte) 100, success.length, success);
            } else {
                byte[] fail = "Failed to add pilot.".getBytes();
                return new Packet((byte) 101, fail.length, fail);
            }
        } catch (Exception e) {
            byte[] errMsg = e.getMessage().getBytes();
            return new Packet((byte) 101, errMsg.length, errMsg);
        }
    }

    private Set<aircraftModel> createSet(String selectedModels, AddPilotController ctrl) {
        String[] parts = selectedModels.split(",");
        Set<aircraftModel> models = new HashSet<>();

        for (String modelName : parts) {
            modelName = modelName.trim();
            aircraftModel model = ctrl.getAircraftModelByName(modelName);
            models.add(model);
        }

        return models;
    }
}
