package eapli.alsafe.remoteaccess.server.requesthandlers.atcc;

import eapli.alsafe.airinfrastructure.application.CreateFlightRouteController;
import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;

import java.nio.charset.StandardCharsets;

public class CreateFlightRouteRequestHandler implements RemoteAccessRequestHandler {
    @Override
    public Packet handle(Packet request) {
        String data = new String(request.payload(), StandardCharsets.UTF_8);
        String[] parts = data.split(";");

        if (parts.length != 3) {
            byte[] errMsg = "Invalid payload format. Expected: numbers;startingAirport;endingAirport".getBytes();
            return new Packet((byte) 101, errMsg.length, errMsg);
        }

        String numbers = parts[0];
        String startingAirport = parts[1];
        String endingAirport = parts[2];

        try {
            CreateFlightRouteController ctrl = new CreateFlightRouteController();

            FlightRoute flightRoute = ctrl.createFlightRoute(numbers, startingAirport, endingAirport);

            if (flightRoute != null) {
                byte[] success = "Flight route created successfully.".getBytes();
                return new Packet((byte) 100, success.length, success);
            } else {
                byte[] fail = "Failed to create flight route.".getBytes();
                return new Packet((byte) 101, fail.length, fail);
            }
        } catch (Exception e) {
            byte[] errorMsg = e.getMessage().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, errorMsg.length, errorMsg);
        }
    }
}
