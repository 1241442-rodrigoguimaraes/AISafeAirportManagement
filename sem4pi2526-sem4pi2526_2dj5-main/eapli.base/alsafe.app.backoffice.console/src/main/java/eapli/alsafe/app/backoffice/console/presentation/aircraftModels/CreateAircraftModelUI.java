package eapli.alsafe.app.backoffice.console.presentation.aircraftModels;

import eapli.alsafe.aircraftModelMagnement.application.CreateAircraftModelController;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModelType;
import eapli.alsafe.engineModelMagnement.Domain.engineModel;
import eapli.alsafe.engineModelMagnement.Domain.engineType;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreateAircraftModelUI extends AbstractUI {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateAircraftModelUI.class);

    private final CreateAircraftModelController controller = new CreateAircraftModelController();

    @Override
    protected boolean doShow() {

        System.out.println("=== Create Aircraft Model ===");

        final String modelName = Console.readLine("Aircraft Model Name:");

        System.out.println("\nAvailable Makers:");

        final Iterable<Maker> makers = controller.makers();

        for (Maker maker : makers) {
            System.out.println("- " + maker.identity());
        }

        final String makerName = Console.readLine("Choose maker:");

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

        System.out.println("\nAircraft Types:");

        for (aircraftModelType type : aircraftModelType.values()) {
            System.out.println("- " + type);
        }

        final aircraftModelType type = readAircraftModelType();

        engineType motorization = selectEngineTypes(controller.getEngineTypes());

        final double maximumRange = readPositiveDouble("Maximum Range (km):");
        final double emptyWeight = readPositiveDouble("Empty Weight:");
        final double mtow = readPositiveDouble("Maximum Takeoff Weight:");
        final double mzfw = readPositiveDouble("Maximum Zero Fuel Weight:");
        final double fuelCapacity = readPositiveDouble("Maximum Fuel Capacity:");
        final double serviceCeiling = readPositiveDouble("Service Ceiling:");
        final double cruiseSpeed = readPositiveDouble("Cruise Speed:");
        final double wingArea = readPositiveDouble("Wing Area:");
        final double dragCoefficient = readPositiveDouble("Drag Coefficient:");
        final double liftCoefficient = readPositiveDouble("Lift Coefficient:");

        final Iterable<engineModel> engineModels = controller.engineModels(motorization);

        System.out.println("\nAvailable Engine Models:");

        for (engineModel engine : engineModels) {
            System.out.println("- " + engine.name());
        }

        final Set<engineModel> selectedEngines = readEngineModels(engineModels);

        try {
            controller.registerAircraftModel(
                    modelName,
                    selectedMaker,
                    type,
                    motorization,
                    maximumRange,
                    emptyWeight,
                    mtow,
                    mzfw,
                    fuelCapacity,
                    serviceCeiling,
                    cruiseSpeed,
                    wingArea,
                    dragCoefficient,
                    liftCoefficient,
                    selectedEngines
            );

            System.out.println("Aircraft Model created successfully.");
        } catch (ConcurrencyException ex) {
            LOGGER.error("Error performing the operation", ex);
            System.out.println("Unfortunately there was an unexpected error in the application. Please try again and if the problem persists, contact your system administrator.");
        } catch (final IntegrityViolationException | IllegalArgumentException e) {
            System.out.println("Validation Error: " + e.getMessage());
        }

        return false;
    }

    private aircraftModelType readAircraftModelType() {

        while (true) {
            try {
                return aircraftModelType.valueOf(
                        Console.readLine("Type:")
                                .trim()
                                .toUpperCase()
                );
            } catch (final IllegalArgumentException e) {
                System.out.println("Invalid aircraft type. Please try again.");
            }
        }
    }

    private engineType selectEngineTypes(final List<engineType> motorizations) {
        for (int i = 0; i < motorizations.size(); i++) {
            System.out.println((i + 1) + " - " + motorizations.get(i));
        }

        int motorization = Console.readInteger("Choose the aircraft model motorization: ");

        while (motorization < 0 || motorization >= motorizations.size()) {
            motorization = Console.readInteger("Invalid input. Please try again: ");
        }

        return motorizations.get(motorization - 1);
    }

    private double readPositiveDouble(final String label) {

        double value;

        do {
            value = Console.readDouble(label);

            if (value <= 0) {
                System.out.println("Value must be positive. Please try again.");
            }

        } while (value <= 0);

        return value;
    }

    private Set<engineModel> readEngineModels(final Iterable<engineModel> engineModels) {
        System.out.println("Insert engine names one by one. Type 'done' when finished.");
        final Set<engineModel> selectedEngines = new HashSet<>();

        while (selectedEngines.isEmpty()) {
            while (true) {
                final String engineName = Console.readLine("Engine:");
                if (engineName.equalsIgnoreCase("done")) break;

                boolean found = false;

                for (engineModel engine : engineModels) {
                    if (engine.name().toString().equalsIgnoreCase(engineName)) {
                        if (selectedEngines.contains(engine)) {
                            System.out.println("Engine model already selected.");
                            found = true;
                            break;
                        }

                        selectedEngines.add(engine);
                        found = true;
                        System.out.println("Engine model added.");
                        break;
                    }
                }

                if (!found) System.out.println("Engine model not found.");
            }

            if (selectedEngines.isEmpty()) System.out.println("You must select at least one engine model.");
        }

        return selectedEngines;
    }

    @Override
    public String headline() {
        return "Create Aircraft Model";
    }
}
