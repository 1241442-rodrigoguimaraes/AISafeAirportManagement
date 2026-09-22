package eapli.alsafe.app.backoffice.console.presentation.airinfrastructure;

import eapli.alsafe.airinfrastructure.application.RegisterAirControlAreaController;

import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.presentation.console.AbstractUI;

import eapli.framework.io.util.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterAirControlAreaUI extends AbstractUI {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterAirControlAreaUI.class);

    private final RegisterAirControlAreaController controller = new RegisterAirControlAreaController();

    @Override
    protected boolean doShow() {

        try {
            System.out.println("Coordinate 1");
            double[] coords1 = typeCoordinates();

            System.out.println("Coordinate 2");
            double[] coords2 = typeCoordinates();

            verifyCoords(coords1, coords2);

            this.controller.createCoordinate(coords1[0], coords1[1]);
            this.controller.createCoordinate(coords2[0], coords2[1]);

            String name = Console.readLine("Type the name of the air control area: ");
            double minimumFuel = Console.readDouble("Define the minimum fuel quantity an aircraft should have while landing in the air control area: ");

            System.out.println(this.controller.registerAirControlArea(name, minimumFuel));
        } catch (IntegrityViolationException | ConcurrencyException ex) {
            LOGGER.error("Error performing the operation", ex);
            System.out.println("Unfortunately there was an unexpected error in the application. Please try again and if the problem persists, contact your system administrator.");
        } catch (final IllegalArgumentException e) {
            System.out.println("Validation Error: " + e.getMessage());
        }

        return false;
    }

    private void verifyCoords(double[] coords1, double[] coords2) {
        if (coords1[0] == coords2[0] && coords1[1] == coords2[1]) throw new IllegalArgumentException("You inserted the same coordinate twice.");

        if (coords1[0] == coords2[0] || coords1[1] == coords2[1]) throw new IllegalArgumentException("The Air Control Area inserted cannot be a rectangle since the latitudes or the longitudes of the coordinates are the same.");
    }

    private double[] typeCoordinates() {
        double latitude;
        double longitude;

        do {
            latitude = Console.readDouble("Latitude (-90 to 90)");
        } while (latitude < -90 || latitude > 90);

        do {
            longitude = Console.readDouble("Longitude (-180 to 180)");
        } while (longitude < -180 || longitude > 180);

        return new double[]{latitude, longitude};
    }

    @Override
    public String headline() {
        return "Register Air Control Area";
    }
}
