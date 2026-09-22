package eapli.alsafe.app.backoffice.console.presentation.airports;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airports.application.RegisterAirportController;
import eapli.alsafe.airports.domain.IATAAirportCode;
import eapli.alsafe.airports.domain.ICAOAirportCode;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.ArrayList;
import java.util.List;

public class RegisterAirportUI extends AbstractUI {

    private final RegisterAirportController theController = new RegisterAirportController();

    @Override
    protected boolean doShow() {
        String icaoCode;
        String iataCode;
        boolean icaoCodeExists;
        boolean iataCodeExists;
        do {
            icaoCode = Console.readLine("ICAO Code (4 uppercase letters)");
            icaoCodeExists = theController.icaoAlreadyExists(icaoCode);
            if (icaoCodeExists) System.out.println("An Airport with this ICAO code already exists. Please try again.");
        } while (!ICAOAirportCode.validICAOFormat(icaoCode) || icaoCodeExists);
        do {
            iataCode = Console.readLine("IATA Code (3 uppercase letters)");
            iataCodeExists = theController.iataAlreadyExists(iataCode);
            if (iataCodeExists) System.out.println("An Airport with this IATA code already exists. Please try again.");
        } while (!IATAAirportCode.validIATAFormat(iataCode) || iataCodeExists);
        final String airportName = Console.readLine("Airport Name");

        final AirControlArea airControlArea = selectAirControlArea();
        if (airControlArea == null || airControlArea.getId() == null) {
            return false;
        }

        System.out.println("\nAirport Location (Node):");
        final double minLatitude = airControlArea.getBoundaries().getBoundaries().stream()
                .mapToDouble(eapli.alsafe.utils.nodes.domain.Coordinate::latitude)
                .min()
                .orElseThrow(() -> new IllegalStateException("Air Control Area boundaries are not defined."));
        final double maxLatitude = airControlArea.getBoundaries().getBoundaries().stream()
                .mapToDouble(eapli.alsafe.utils.nodes.domain.Coordinate::latitude)
                .max()
                .orElseThrow(() -> new IllegalStateException("Air Control Area boundaries are not defined."));
        final double minLongitude = airControlArea.getBoundaries().getBoundaries().stream()
                .mapToDouble(eapli.alsafe.utils.nodes.domain.Coordinate::longitude)
                .min()
                .orElseThrow(() -> new IllegalStateException("Air Control Area boundaries are not defined."));
        final double maxLongitude = airControlArea.getBoundaries().getBoundaries().stream()
                .mapToDouble(eapli.alsafe.utils.nodes.domain.Coordinate::longitude)
                .max()
                .orElseThrow(() -> new IllegalStateException("Air Control Area boundaries are not defined."));
        double latitude;
        double longitude;
        boolean coordinatesAlreadyExist;
        do {
            do {
                latitude = Console.readDouble("Latitude ("+minLatitude+" to "+maxLatitude+")");
            }while (latitude < minLatitude || latitude > maxLatitude);

            do {
                longitude = Console.readDouble("Longitude ("+minLongitude+" to "+maxLongitude+")");
            }while (longitude < minLongitude || longitude > maxLongitude );
            coordinatesAlreadyExist = theController.coordinatesAlreadyExist(latitude, longitude);
            if (coordinatesAlreadyExist) System.out.println("An Airport already exists at this location. Please try again.");
        }while (coordinatesAlreadyExist);

        final double altitude = Console.readDouble("Altitude (meters)");

        try {
            this.theController.registerAirport(icaoCode, iataCode, airportName, latitude, longitude, altitude, airControlArea.getId());
            System.out.println("Airport registered successfully!");
        } catch (final IntegrityViolationException e) {
            System.out.println("Error: An airport with the same info already exists.");
        } catch (final IllegalArgumentException e) {
            System.out.println("Validation error: " + e.getMessage());
        } catch (final Exception e) {
            System.out.println("Unknown error: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Register Airport";
    }

    public AirControlArea selectAirControlArea() {
        final List<AirControlArea> list = new ArrayList<>();
        final Iterable<AirControlArea> iterable = this.theController.allAirControlAreas();
        
        if (!iterable.iterator().hasNext()) {
            System.out.println("Do not have any Air Control Area registered. Please register an Air Control Area before registering an Airport.");
            return null;
        }
        
        int cont = 1;
        System.out.println("\nSelect an Air Control Area:\n");
        System.out.printf("%-6s%-10s%-40s%-15s%n", "Nº:", "ID", "Name", "Fuel");
        
        for (final AirControlArea area : iterable) {
            list.add(area);
            System.out.printf("%-6d%-10s%-40s%-15s%n", cont, area.getId(), area.getName(), area.getMinimumFuel());
            cont++;
        }
        
        final int option = Console.readInteger("Select or 0 to cancel: ");
        
        if (option == 0) {
            System.out.println("Non-registered Air Control Area selected.");
            return null;
        }
        
        if (option < 1 || option > list.size()) {
            System.out.println("Invalid option selected. Please select a valid option.");
            return null;
        }
        
        return list.get(option - 1);
    }
}
