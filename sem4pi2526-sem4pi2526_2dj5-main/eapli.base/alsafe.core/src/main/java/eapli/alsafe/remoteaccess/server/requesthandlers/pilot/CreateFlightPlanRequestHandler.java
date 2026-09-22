package eapli.alsafe.remoteaccess.server.requesthandlers.pilot;

import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.flightPlan.application.FlightPlanController;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.flightPlan.domain.FuelUnit;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

public class CreateFlightPlanRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(final Packet packet) {
        final String payloadStr = new String(packet.payload(), StandardCharsets.UTF_8);
        final String[] parts = payloadStr.split(";", 7);

        if (parts.length != 7) {
            return error("Invalid payload format. Expected: routeId;aircraftRegistration;designator;departureDateTime;fuelUnit;fuelAmount;base64DslContent");
        }

        try {
            final String routeId = parts[0];
            final String aircraftRegistration = parts[1];
            final String designator = parts[2];
            final LocalDateTime departureDateTime = LocalDateTime.parse(parts[3]);
            final FuelUnit fuelUnit = FuelUnit.valueOf(parts[4].toUpperCase());
            final double fuelAmount = Double.parseDouble(parts[5]);
            final String content = new String(Base64.getDecoder().decode(parts[6]), StandardCharsets.UTF_8);

            final FlightPlanController controller = new FlightPlanController();
            final Pilot pilot = controller.getAuthenticatedPilot();
            final FlightRoute route = findRoute(controller, pilot, routeId);
            final Aircraft aircraft = findAircraft(controller, pilot, aircraftRegistration);

            final FlightPlan flightPlan = controller.createFlightPlan(
                    designator, route, aircraft, pilot, departureDateTime, fuelAmount, fuelUnit, content);

            final String message = "Flight plan created successfully: " + flightPlan.identity();
            final byte[] payload = message.getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 100, payload.length, payload);
        } catch (final Exception e) {
            return error(e.getMessage());
        }
    }

    private FlightRoute findRoute(final FlightPlanController controller, final Pilot pilot, final String routeId) {
        for (final FlightRoute route : controller.getRoutesForPilot(pilot.company())) {
            if (route.identity().toString().equalsIgnoreCase(routeId)) {
                return route;
            }
        }
        throw new IllegalArgumentException("Flight route not found for authenticated pilot company: " + routeId);
    }

    private Aircraft findAircraft(final FlightPlanController controller, final Pilot pilot, final String registration) {
        for (final Aircraft aircraft : controller.getAircraftForCompany(pilot.company())) {
            if (aircraft.identity().toString().equalsIgnoreCase(registration)) {
                return aircraft;
            }
        }
        throw new IllegalArgumentException("Aircraft not found for authenticated pilot company: " + registration);
    }

    private Packet error(final String message) {
        final byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        return new Packet((byte) 101, payload.length, payload);
    }
}
