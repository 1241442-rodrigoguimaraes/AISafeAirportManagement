package eapli.alsafe.remoteaccess.server.requesthandlers.atcc;

import eapli.alsafe.aircraft.application.AddAircraftController;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;

import java.nio.charset.StandardCharsets;

public class ListAircraftModelsRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(Packet packet) {
        AddAircraftController ctrl = new AddAircraftController();
        Iterable<aircraftModel> models = ctrl.getAircraftModels();

        if(!models.iterator().hasNext()) return new Packet((byte) 101, 0, null);

        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (aircraftModel m : models) {
            sb.append(index++).append(" - ").append(m).append("\n");
        }

       byte[] payload = sb.toString().getBytes(StandardCharsets.UTF_8);
       return new Packet((byte) 100, payload.length, payload);
    }
}
