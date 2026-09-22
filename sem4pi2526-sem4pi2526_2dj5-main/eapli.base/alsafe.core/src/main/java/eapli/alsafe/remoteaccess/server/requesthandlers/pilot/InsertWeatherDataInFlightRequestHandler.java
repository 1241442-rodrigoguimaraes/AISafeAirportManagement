package eapli.alsafe.remoteaccess.server.requesthandlers.pilot;

import eapli.alsafe.flightPlan.application.InsertWeatherDataInFlightController;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;
import eapli.alsafe.weather.domain.WeatherData;

import java.nio.charset.StandardCharsets;

public class InsertWeatherDataInFlightRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(final Packet request) {
        final String payloadStr = new String(request.payload(), StandardCharsets.UTF_8);
        final String[] parts = payloadStr.split(";", 2);

        if (parts.length != 2) {
            return error("Invalid payload format. Expected: flightPlanId;weatherDataId");
        }

        try {
            final String flightPlanId = parts[0].trim();
            final Long weatherDataId = Long.valueOf(parts[1].trim());
            final InsertWeatherDataInFlightController controller = new InsertWeatherDataInFlightController();

            final FlightPlan flightPlan = findFlightPlan(controller, flightPlanId);
            final WeatherData weatherData = findWeatherData(controller, weatherDataId);

            final boolean wasTested = flightPlan.isTested();
            controller.insertWeatherData(flightPlan, weatherData);

            String message = "Weather data successfully added to flight plan: " + flightPlan.identity();
            if (wasTested) {
                message += " Previous test result was voided.";
            }
            return success(message);
        } catch (final Exception e) {
            return error(e.getMessage());
        }
    }

    private FlightPlan findFlightPlan(final InsertWeatherDataInFlightController controller, final String flightPlanId) {
        for (final FlightPlan flightPlan : controller.myFlightPlans()) {
            if (flightPlan.identity().toString().equalsIgnoreCase(flightPlanId)) {
                return flightPlan;
            }
        }
        throw new IllegalArgumentException("Flight plan not found for authenticated pilot: " + flightPlanId);
    }

    private WeatherData findWeatherData(final InsertWeatherDataInFlightController controller, final Long weatherDataId) {
        for (final WeatherData weatherData : controller.availableWeatherData()) {
            if (weatherData.identity() != null && weatherData.identity().equals(weatherDataId)) {
                return weatherData;
            }
        }
        throw new IllegalArgumentException("Weather data not found: " + weatherDataId);
    }

    private Packet success(final String message) {
        final byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        return new Packet((byte) 100, payload.length, payload);
    }

    private Packet error(final String message) {
        final byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        return new Packet((byte) 101, payload.length, payload);
    }
}
