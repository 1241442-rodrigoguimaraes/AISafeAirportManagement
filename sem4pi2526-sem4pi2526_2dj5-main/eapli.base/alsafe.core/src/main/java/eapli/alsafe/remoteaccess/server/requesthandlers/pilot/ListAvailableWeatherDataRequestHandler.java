package eapli.alsafe.remoteaccess.server.requesthandlers.pilot;

import eapli.alsafe.flightPlan.application.InsertWeatherDataInFlightController;
import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;
import eapli.alsafe.weather.domain.WeatherData;

import java.nio.charset.StandardCharsets;

public class ListAvailableWeatherDataRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(final Packet request) {
        try {
            final InsertWeatherDataInFlightController controller = new InsertWeatherDataInFlightController();
            final StringBuilder response = new StringBuilder();

            for (final WeatherData weatherData : controller.availableWeatherData()) {
                if (!response.isEmpty()) {
                    response.append("|");
                }
                response.append(weatherData.identity()).append(":").append(oneLine(weatherData.toString()));
            }

            final byte[] payload = response.toString().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 100, payload.length, payload);
        } catch (final Exception e) {
            final byte[] payload = e.getMessage().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, payload.length, payload);
        }
    }

    private String oneLine(final String value) {
        return value.replace('|', '/')
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }
}
