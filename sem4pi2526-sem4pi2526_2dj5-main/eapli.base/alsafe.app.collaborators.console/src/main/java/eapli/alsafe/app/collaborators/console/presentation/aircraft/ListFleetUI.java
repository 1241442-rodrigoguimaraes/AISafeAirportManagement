package eapli.alsafe.app.collaborators.console.presentation.aircraft;

import eapli.alsafe.aircraft.application.ListFleetController;
import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

public class ListFleetUI extends AbstractUI {

    private final ListFleetController controller =
            new ListFleetController();

    @Override
    protected boolean doShow() {

        System.out.println("\n=== Fleet Listing ===");

        System.out.println("1 - List full fleet");
        System.out.println("2 - Filter by aircraft model");
        System.out.println("3 - Filter by maker");
        System.out.println("4 - Filter by capacity");

        final int option = Console.readInteger("Option:");

        switch (option) {

            case 1 -> showFleet(controller.fleet());

            case 2 -> filterByModel();

            case 3 -> filterByMaker();

            case 4 -> filterByCapacity();

            case 0 -> {
                System.out.println("Exiting...");
                return false;
            }

            default -> System.out.println("Invalid option.");
        }

        return false;
    }

    private void filterByModel() {

        final Iterable<aircraftModel> models =
                controller.aircraftModels();

        System.out.println("\nAvailable Aircraft Models:");

        for (final aircraftModel model : models) {
            System.out.println("- " + model.name());
        }

        final String modelName =
                Console.readLine("Aircraft model:");

        for (final aircraftModel model : models) {

            if (model.name().toString()
                    .equalsIgnoreCase(modelName)) {

                showFleet(controller.fleetByModel(model));
                return;
            }
        }

        System.out.println("Aircraft model not found.");
    }

    private void filterByMaker() {

        System.out.println("\nAvailable Makers:");

        for (final Aircraft aircraft : controller.fleet()) {
            System.out.println("- " +
                    aircraft.model().maker().identity());
        }

        final String makerName =
                Console.readLine("Maker:");

        boolean found = false;

        for (final Aircraft aircraft : controller.fleet()) {

            if (aircraft.model().maker().identity().toString()
                    .equalsIgnoreCase(makerName)) {

                printAircraft(aircraft);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No aircraft found.");
        }
    }

    private void filterByCapacity() {

        final int capacity =
                Console.readInteger("Capacity:");

        showFleet(controller.fleetByCapacity(capacity));
    }

    private void showFleet(final Iterable<Aircraft> fleet) {

        boolean found = false;

        for (final Aircraft aircraft : fleet) {

            printAircraft(aircraft);
            found = true;
        }

        if (!found) {
            System.out.println("\nNo aircraft found.");
        }
    }

    private void printAircraft(final Aircraft aircraft) {

        System.out.println("\nRegistration: "
                + aircraft.identity());

        System.out.println("Model: "
                + aircraft.model().name());

        System.out.println("Maker: "
                + aircraft.model().maker().identity());

        System.out.println("Capacity: "
                + aircraft.cabinConfiguration().totalSeats());

        System.out.println("Status: "
                + aircraft.maintenanceStatus());
    }

    @Override
    public String headline() {
        return "List Fleet";
    }
}