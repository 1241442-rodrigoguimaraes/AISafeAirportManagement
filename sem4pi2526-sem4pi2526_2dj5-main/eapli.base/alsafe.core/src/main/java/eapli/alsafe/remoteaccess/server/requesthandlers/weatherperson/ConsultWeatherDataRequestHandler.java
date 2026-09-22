package eapli.alsafe.remoteaccess.server.requesthandlers.weatherperson;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;
import eapli.alsafe.weather.application.ConsultWeatherDataController;
import eapli.alsafe.weather.application.RegisterWeatherDataController;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.framework.time.util.Calendars;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ConsultWeatherDataRequestHandler implements RemoteAccessRequestHandler {
    @Override
    public Packet handle(Packet request) {
        String payloadStr = new String(request.payload());
        String[] parts = payloadStr.split(";");

        if (parts.length != 3) {
            String errorMsg = "Invalid payload format. Expected: areaId;startDate;endDate";
            return new Packet((byte) 101, errorMsg.getBytes().length, errorMsg.getBytes());
        }

        try {
            String areaId = parts[0];
            String startDate = parts[1];
            String endDate = parts[2];

            ConsultWeatherDataController ctrl = new ConsultWeatherDataController();
            RegisterWeatherDataController ctrlRegister = new RegisterWeatherDataController();
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Iterable<WeatherData> result = ctrl.consultWeatherData(ctrlRegister.getAirControlArea(areaId), Calendars.fromDate(dateFormat.parse(startDate)), Calendars.fromDate(dateFormat.parse(endDate)));

            StringBuilder sb = new StringBuilder();
            for (WeatherData data : result) {
                sb.append(data.toString());
            }

            byte[] payload = sb.toString().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 100, payload.length, payload);
        } catch (IllegalArgumentException | ParseException e) {
            byte[] err = e.getMessage().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, err.length, err);
        }
    }
}