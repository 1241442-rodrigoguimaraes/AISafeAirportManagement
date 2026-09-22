package eapli.alsafe.remoteaccess.server.requesthandlers.weatherperson;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;
import eapli.alsafe.weather.application.RegisterWeatherDataController;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.framework.time.util.Calendars;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;


public class RegisterWeatherDataRequestHandler implements RemoteAccessRequestHandler {

    @Override
    public Packet handle(Packet request) {
        String payloadStr = new String(request.payload());
        String[] parts = payloadStr.split(";");

        if (parts.length != 4) {
            String errorMsg = "Invalid payload format. Expected: areaId;date;windDirect;windSpeed";
            return new Packet((byte) 101, errorMsg.getBytes().length, errorMsg.getBytes());
        }

        try {
            String areaId = parts[0];
            String date = parts[1];
            int windDirect = Integer.parseInt(parts[2]);
            double windSpeed = Double.parseDouble(parts[3]);

            RegisterWeatherDataController ctrl = new RegisterWeatherDataController();

            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            WeatherData result = ctrl.registerWeatherData(ctrl.getAirControlArea(areaId), Calendars.fromDate(dateFormat.parse(date)), windDirect, windSpeed);


            if(result != null){
                return new Packet((byte) 100, result.toString().getBytes().length, result.toString().getBytes());
            }else {
                byte[] fail = "Failed to register weather data.".getBytes(StandardCharsets.UTF_8);
                return new Packet((byte) 101, fail.length, fail);
            }
        } catch (IllegalArgumentException | ParseException e) {
            byte[] err = e.getMessage().getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, err.length, err);
        }
    }
}