package eapli.alsafe.app.collaborators.console.presentation.flightplan;

import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.flightPlan.application.FlightPlanController;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.flightPlan.domain.FlightPlanID;
import eapli.alsafe.flightPlan.domain.FuelUnit;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("squid:S106")
public class CreateFlightPlanUI extends AbstractUI {

    private final FlightPlanController ctrl = new FlightPlanController();
    private Pilot pilot;

    @Override
    protected boolean doShow() {
        try {
            pilot = ctrl.getAuthenticatedPilot();
            System.out.println();
            System.out.println("Authenticated Pilot: " + pilot.name() + " (" + pilot.email() + ")");
            System.out.println("Company: " + pilot.company().getName());
            System.out.println();

            final FlightRoute selectedRoute = selectRoute();
            if (selectedRoute == null) {
                return false;
            }

            final Aircraft selectedAircraft = selectAircraft();
            if (selectedAircraft == null) {
                return false;
            }

            String designator;
            do {
                designator = (pilot.company().getIata()+ Console.readLine("Flight number (e.g. 123, 12A):")).trim().toUpperCase();
            } while (!FlightPlanID.validFlightPlanIDFormat(designator));

            final LocalDateTime departureDateTime = readDepartureDateTime();

            String fuelUnit;
            do {
                fuelUnit = Console.readLine("Fuel unit (L, LBS, KG):");
            }while (!fuelUnit.equalsIgnoreCase("L") && !fuelUnit.equalsIgnoreCase("LBS") && !fuelUnit.equalsIgnoreCase("KG"));

            FuelUnit unit = switch (fuelUnit.toUpperCase()) {
                case "LBS" -> FuelUnit.LBS;
                case "KG" -> FuelUnit.KG;
                default -> FuelUnit.L;
            };
            double fuelAmount;
            do {
                fuelAmount = Console.readDouble("Fuel quantity (" + unit + "):");
            }while (fuelAmount <= 0);

            String filePath = Console.readLine("Enter path to flight plan file (.fp):");
            String content;
            try {
                content = java.nio.file.Files.readString(java.nio.file.Path.of(filePath));
                System.out.println("File read successfully (" + content.length() + " characters)");
            } catch (java.io.IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
                return false;
            }

            final FlightPlan flightPlan = ctrl.createFlightPlan(
                    designator, selectedRoute, selectedAircraft, pilot,
                    departureDateTime, fuelAmount, unit, content);

            System.out.println();
            System.out.println("Flight plan created successfully!");
            System.out.println("ID: " + flightPlan.identity());
            System.out.println("Route: " + flightPlan.getRoute().getFlightRouteName());
            System.out.println("Aircraft: " + flightPlan.getAircraft().identity());
            System.out.println("Pilot: " + flightPlan.getPilot().name());
            System.out.println("Departure: " + flightPlan.getDepartureDateTime().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            System.out.println("Fuel: " + flightPlan.getFuelQuantity().getFuelQuantity()
                    + " " + flightPlan.getFuelQuantity().getFuelUnit());
            System.out.println("Status: " + flightPlan.getStatus());

        } catch (final Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return false;
    }

    private FlightRoute selectRoute() {
        final Iterable<FlightRoute> routesIterable = ctrl.getRoutesForPilot(pilot.company());
        final List<FlightRoute> routes = new ArrayList<>();
        routesIterable.forEach(routes::add);

        if (routes.isEmpty()) {
            System.out.println("No flight routes available for your company.");
            return null;
        }

        final SelectWidget<FlightRoute> selector = new SelectWidget<>("Select a Flight Route:", routes);
        selector.show();
        final FlightRoute selected = selector.selectedElement();
        if (selected == null) {
            System.out.println("No route selected.");
        }
        return selected;
    }

    private Aircraft selectAircraft() {
        final Iterable<Aircraft> aircraftIterable = ctrl.getAircraftForCompany(pilot.company());
        final List<Aircraft> aircraftList = new ArrayList<>();
        aircraftIterable.forEach(aircraftList::add);

        if (aircraftList.isEmpty()) {
            System.out.println("No aircraft available for your company.");
            return null;
        }

        final SelectWidget<Aircraft> selector = new SelectWidget<>("Select an Aircraft:", aircraftList);
        selector.show();
        final Aircraft selected = selector.selectedElement();
        if (selected == null) {
            System.out.println("No aircraft selected.");
        }
        return selected;
    }

    private LocalDateTime readDepartureDateTime() {
        while (true) {
            try {
                final String dateStr = Console.readLine("Departure date (yyyy-MM-dd):");
                final String timeStr = Console.readLine("Departure time (HH:mm):");
                return LocalDateTime.parse(dateStr + "T" + timeStr,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            } catch (final DateTimeParseException e) {
                System.out.println("Invalid date/time format. Please try again.");
            }
        }
    }

    @Override
    public String headline() {
        return "Create Flight Plan";
    }
}
