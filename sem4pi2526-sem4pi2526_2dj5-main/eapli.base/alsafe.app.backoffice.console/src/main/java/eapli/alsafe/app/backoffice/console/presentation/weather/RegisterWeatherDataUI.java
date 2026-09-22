package eapli.alsafe.app.backoffice.console.presentation.weather;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.weather.application.RegisterWeatherDataController;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.Calendar;

public class RegisterWeatherDataUI extends AbstractUI {

    private final RegisterWeatherDataController theController = new RegisterWeatherDataController();

    @Override
    protected boolean doShow() {
        final Iterable<AirControlArea> areas = this.theController.getAirControlAreas();

        final SelectWidget<AirControlArea> selector = new SelectWidget<>("Select Air Control Area:", areas);
        selector.show();
        final AirControlArea area = selector.selectedElement();

        if (area == null) {
            return false;
        }

        final Calendar date = Console.readCalendar("Date of observation (yyyy-mm-dd):", "yyyy-MM-dd");
        final int windDirection = Console.readInteger("Wind direction (0-360°):");
        final double windSpeed = Console.readDouble("Wind speed (m/s):");

        try {
            final WeatherData weatherData = this.theController.registerWeatherData(area, date, windDirection, windSpeed);
            System.out.println("\nWeather data registered successfully:");
            System.out.println(weatherData.toString());
        } catch (final Exception e) {
            System.out.println("\nError registering weather data: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Register Weather Data";
    }
}
