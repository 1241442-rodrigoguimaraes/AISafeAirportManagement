package eapli.alsafe.app.backoffice.console.presentation.weather;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.weather.application.ConsultWeatherDataController;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ConsultWeatherDataUI extends AbstractUI {

    private final ConsultWeatherDataController theController = new ConsultWeatherDataController();

    @Override
    protected boolean doShow() {
        AirControlArea area = null;

        if(!theController.isFCORole()){
            final Iterable<AirControlArea> areas = this.theController.getAirControlAreas();

            final SelectWidget<AirControlArea> selector = new SelectWidget<>("Select Air Control Area:", areas);
            selector.show();
            area = selector.selectedElement();
        }else{
            System.out.println("\nAs a Flight Control Operator, you can only consult weather data for your assigned area.");
             area = theController.getAllWeatherDataFromFCOCollaborator().orElseThrow(
                     () -> new IllegalStateException("No Weather Data found")).getAirControlArea();
        }

        if (area == null) {
            return false;
        }

        final Calendar startDate = Console.readCalendar("Start date (yyyy-MM-dd):", "yyyy-MM-dd");
        final Calendar endDate = Console.readCalendar("End date (yyyy-MM-dd):", "yyyy-MM-dd");

        try {
            final Iterable<WeatherData> results = this.theController.consultWeatherData(area, startDate, endDate);

            final List<WeatherData> list = new ArrayList<>();
            results.forEach(list::add);

            if (list.isEmpty()) {
                System.out.println("\nNo weather data found for the selected criteria.");
            } else {
                System.out.printf("%nWeather Data for %s from %tF to %tF:%n",
                        area.getName(), startDate, endDate);
                System.out.println("----------------------------------------------------");
                int i = 1;
                for (final WeatherData wd : list) {
                    System.out.printf("%d. Date: %s | %s%n", i++, wd.getWeatherDate(), wd.getWindCondition());
                }
                System.out.println("----------------------------------------------------");
            }
        } catch (final Exception e) {
            System.out.println("\nError consulting weather data: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Consult Weather Data";
    }
}
