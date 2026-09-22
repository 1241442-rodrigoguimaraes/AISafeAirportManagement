package eapli.alsafe.remoteaccess.server.requesthandlers.atcc;

import eapli.alsafe.aircraft.application.ListFleetController;
import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class ListAircraftRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(Packet request) {
        ListFleetController ctrl = new ListFleetController();
        Iterable<Aircraft> fleet = Collections.emptyList();

        if (request.payload() == null || request.length() == 0) {
            fleet = ctrl.fleet();
        } else {
            String data = new String(request.payload(), StandardCharsets.UTF_8);
            String[] parts = data.split(";", 2);
            int option = Integer.parseInt(parts[0]);

            switch (option) {
                case 1 -> fleet = ctrl.fleet();
                case 2 -> {
                    if (parts.length > 1) {
                        String modelName = parts[1];
                        for (aircraftModel model : ctrl.aircraftModels()) {
                            if (model.name().toString().equalsIgnoreCase(modelName)) {
                                fleet = ctrl.fleetByModel(model);
                                break;
                            }
                        }
                    }
                }
                case 3 -> {
                    if (parts.length > 1) {
                        String makerName = parts[1];
                        for (Aircraft ac : ctrl.fleet()) {
                            Maker maker = ac.model().maker();
                            if (maker.identity().toString().equalsIgnoreCase(makerName)) {
                                fleet = ctrl.fleetByMaker(maker);
                                break;
                            }
                        }
                    }
                }
                case 4 -> {
                    if (parts.length > 1) {
                        int capacity = Integer.parseInt(parts[1]);
                        fleet = ctrl.fleetByCapacity(capacity);
                    }
                }
            }
        }

        if (!fleet.iterator().hasNext()) {
            return new Packet((byte) 101, 0, null);
        }

        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (Aircraft ac : fleet) {
            sb.append(index++).append(" - ").append(ac).append("\n");
        }

        byte[] payload = sb.toString().getBytes(StandardCharsets.UTF_8);
        return new Packet((byte) 100, payload.length, payload);
    }
}
