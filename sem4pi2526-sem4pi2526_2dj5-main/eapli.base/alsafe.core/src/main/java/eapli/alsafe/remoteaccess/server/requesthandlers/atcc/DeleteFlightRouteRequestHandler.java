package eapli.alsafe.remoteaccess.server.requesthandlers.atcc;

import eapli.alsafe.airinfrastructure.application.DeleteFlightRouteController;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class DeleteFlightRouteRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(Packet request) {
        String data = new String(request.payload(), StandardCharsets.UTF_8).trim();

        if (data.isEmpty()) {
            byte[] err = "Invalid payload: date is required.".getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, err.length, err);
        }

        DeleteFlightRouteController ctrl = new DeleteFlightRouteController();

        if (!data.contains(";")) {
            return handleListRoutes(ctrl, data);
        } else {
            return handleDeleteRoute(ctrl, data);
        }
    }

    private Packet handleListRoutes(DeleteFlightRouteController ctrl, String date) {
        try {
            List<String> routes = ctrl.getCompanyRoutes(date);

            if (routes.isEmpty()) {
                byte[] err = "No deletable flight routes found for the given date.".getBytes(StandardCharsets.UTF_8);
                return new Packet((byte) 101, err.length, err);
            }

            String body = String.join("\n", routes);
            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 100, payload.length, payload);
        } catch (Exception e) {
            byte[] err = e.getMessage().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, err.length, err);
        }
    }

    private Packet handleDeleteRoute(DeleteFlightRouteController ctrl, String data) {
        String[] parts = data.split(";", 2);

        if (parts.length != 2 || parts[1].isBlank()) {
            byte[] err = "Invalid payload format. Expected: date;routeName".getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, err.length, err);
        }

        String routeName = parts[1].trim();

        try {
            ctrl.deleteFlightRoute(routeName);
            byte[] success = ("Flight route '" + routeName + "' deleted successfully.").getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 100, success.length, success);
        } catch (Exception e) {
            byte[] err = e.getMessage().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, err.length, err);
        }
    }
}
