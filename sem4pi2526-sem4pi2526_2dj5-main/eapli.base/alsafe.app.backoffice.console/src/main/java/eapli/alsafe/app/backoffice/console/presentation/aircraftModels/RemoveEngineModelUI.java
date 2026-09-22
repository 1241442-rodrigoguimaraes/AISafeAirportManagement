package eapli.alsafe.app.backoffice.console.presentation.aircraftModels;

import eapli.alsafe.aircraftModelMagnement.application.RemoveEngineModelController;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.engineModelMagnement.Domain.engineModel;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

public class RemoveEngineModelUI extends AbstractUI {

    private final RemoveEngineModelController controller = new RemoveEngineModelController();

    @Override
    protected boolean doShow() {

        System.out.println(" === Remove Engine From Aircraft Model ===");

        final Iterable<aircraftModel> aircraftModels = controller.aircraftModels();

        System.out.println("Available Aircraft Models:");

        for (aircraftModel aircraft : aircraftModels) {
            System.out.println("- " + aircraft.name());
        }

        final aircraftModel selectedAircraft =
                readAircraftModel(aircraftModels);

        System.out.println("Certified Engines: ");

        for (engineModel engine : selectedAircraft.certifiedEngines()) {
            System.out.println("- " + engine.name());
        }

        final engineModel selectedEngine = readEngineModel(selectedAircraft);

        try {
            controller.removeEngineModel(selectedAircraft, selectedEngine);

            System.out.println("Engine model removed successfully.");

        } catch (final Exception e) {
            System.out.println("Error removing engine:" + e.getMessage());
        }

        return false;
    }

    private aircraftModel readAircraftModel(final Iterable<aircraftModel> aircraftModels) {

        aircraftModel selectedAircraft = null;

        while (selectedAircraft == null) {

            final String aircraftName =
                    Console.readLine("Choose aircraft model:");

            for (aircraftModel aircraft : aircraftModels) {
                if (aircraft.name().toString().equalsIgnoreCase(aircraftName)) {
                    selectedAircraft = aircraft;
                    break;
                }
            }

            if (selectedAircraft == null) {
                System.out.println(" Invalid aircraft model. Please try again.");
            }
        }

        return selectedAircraft;
    }

    private engineModel readEngineModel(final aircraftModel selectedAircraft) {

        engineModel selectedEngine = null;

        while (selectedEngine == null) {

            final String engineName =
                    Console.readLine("Choose engine to remove:");

            for (engineModel engine : selectedAircraft.certifiedEngines()) {
                if (engine.name().toString().equalsIgnoreCase(engineName)) {
                    selectedEngine = engine;
                    break;
                }
            }

            if (selectedEngine == null) {
                System.out.println("Invalid engine model. Please try again.");
            }
        }

        return selectedEngine;
    }

    @Override
    public String headline() {
        return "Remove Engine From Aircraft Model";
    }
}
