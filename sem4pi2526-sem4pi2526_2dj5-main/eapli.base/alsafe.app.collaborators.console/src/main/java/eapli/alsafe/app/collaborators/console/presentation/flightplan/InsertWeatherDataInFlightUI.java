package eapli.alsafe.app.collaborators.console.presentation.flightplan;

import eapli.alsafe.flightPlan.application.InsertWeatherDataInFlightController;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.weather.domain.WeatherData;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.ArrayList;
import java.util.List;

public class InsertWeatherDataInFlightUI extends AbstractUI {

    private final InsertWeatherDataInFlightController controller =
            new InsertWeatherDataInFlightController();

    @Override
    protected boolean doShow() {
        final List<FlightPlan> flightPlans = new ArrayList<>();
        controller.myFlightPlans().forEach(flightPlans::add);

        if (flightPlans.isEmpty()) {
            System.out.println("You have no flight plans available.");
            return false;
        }

        final SelectWidget<FlightPlan> fpSelector =
                new SelectWidget<>("Select a flight plan:", flightPlans);
        fpSelector.show();
        final FlightPlan selectedPlan = fpSelector.selectedElement();
        if (selectedPlan == null) {
            return false;
        }

        if (selectedPlan.isTested()) {
            System.out.println("WARNING: This flight plan has already been tested. "
                    + "Adding weather data will void the test result.");
            System.out.print("Do you wish to continue? (y/n): ");
            final String confirm = Console.readLine("");
            if (!confirm.equalsIgnoreCase("y")) {
                System.out.println("Operation cancelled.");
                return false;
            }
        }

        final List<WeatherData> weatherList = new ArrayList<>();
        controller.availableWeatherData().forEach(weatherList::add);

        if (weatherList.isEmpty()) {
            System.out.println("No weather data available in the system.");
            return false;
        }

        final SelectWidget<WeatherData> wdSelector =
                new SelectWidget<>("Select weather data to add:", weatherList);
        wdSelector.show();
        final WeatherData selectedWeather = wdSelector.selectedElement();
        if (selectedWeather == null) {
            return false;
        }

        try {
            controller.insertWeatherData(selectedPlan, selectedWeather);
            System.out.println("Weather data successfully added to the flight plan.");
        } catch (final Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Insert Weather Data in Flight Plan (US082)";
    }
}