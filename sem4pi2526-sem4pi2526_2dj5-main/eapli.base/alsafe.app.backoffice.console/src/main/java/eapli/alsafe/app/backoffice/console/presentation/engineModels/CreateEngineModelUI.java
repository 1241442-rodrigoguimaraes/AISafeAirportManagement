package eapli.alsafe.app.backoffice.console.presentation.engineModels;

import eapli.alsafe.engineModelMagnement.Application.CreateEngineModelController;
import eapli.alsafe.engineModelMagnement.Domain.*;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

public class CreateEngineModelUI extends AbstractUI  {
    private final CreateEngineModelController controller =
            new CreateEngineModelController();

    @Override
    protected boolean doShow() {

        System.out.println("=== Create Engine Model ===");

        final String modelName =
                Console.readLine("Engine Model Name:");

        System.out.println("Available Makers:");

        final Iterable<Maker> makers = controller.makers();

        int index = 1;

        for (Maker maker : makers) {
            System.out.println(index + ". " + maker.identity());
            index++;
        }

        final String makerName =
                Console.readLine("Maker name:");

        Maker selectedMaker = null;

        for (Maker maker : makers) {
            if (maker.identity().toString().equalsIgnoreCase(makerName)) {
                selectedMaker = maker;
                break;
            }
        }

        if (selectedMaker == null) {
            System.out.println("Invalid maker.");
            return false;
        }

        System.out.println("Engine Types:");

        for (engineType type : engineType.values()) {
            System.out.println("- " + type);
        }

        final engineType type =
                engineType.valueOf(
                        Console.readLine("Type:").toUpperCase()
                );

        System.out.println("Fuel Types:");

        for (engineModelFuel fuelType : engineModelFuel.values()) {
            System.out.println("- " + fuelType);
        }

        final engineModelFuel fuel =
                engineModelFuel.valueOf(
                        Console.readLine("Fuel:").toUpperCase()
                );

        final double power =
                Console.readDouble("Power:");

        final double efficiency =
                Console.readDouble("Efficiency:");

        try {

            controller.registerEngineModel(
                    modelName,
                    selectedMaker,
                    type,
                    power,
                    fuel,
                    efficiency
            );

            System.out.println("Engine model created successfully.");

        } catch (final Exception e) {

            System.out.println("Error: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Create Engine Model";
    }
}
