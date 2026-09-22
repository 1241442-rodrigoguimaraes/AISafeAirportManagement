package eapli.alsafe.app.collaborators.console.presentation.aircraft;

import eapli.alsafe.aircraft.application.AddAircraftController;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("squid:S106")
public class AddAircraftUI extends AbstractUI {

    private final AddAircraftController ctrl = new AddAircraftController();

    @Override
    protected boolean doShow() {
        final Iterable<aircraftModel> modelsIterable = ctrl.getAircraftModels();
        final List<aircraftModel> models = new ArrayList<>();
        modelsIterable.forEach(models::add);
        if (models.isEmpty()) {
            System.out.println("No aircraft models are available.");
            return false;
        }

        final SelectWidget<aircraftModel> modelSelector = new SelectWidget<>("Select an Aircraft Model:", models);
        modelSelector.show();
        final aircraftModel selectedModel = modelSelector.selectedElement();
        if (selectedModel == null) {
            return false;
        }

        final String registrationID = Console.readLine("Aircraft Registration Number:");
        final String country = Console.readLine("Registration Country:");
        final int economy = Console.readInteger("Number of Economy seats:");
        final int business = Console.readInteger("Number of Business seats:");
        final int firstClass = Console.readInteger("Number of First Class seats:");
        final int crewCount = Console.readInteger("Number of crew elements:");

        try {
            final boolean ok = ctrl.addAircraft(selectedModel, registrationID, country, economy, business, firstClass,
                    crewCount);
            if (ok) {
                System.out.println("Aircraft registered successfully for your company.");
                System.out.println("Model: " + selectedModel + " | Registration: " + registrationID.trim().toUpperCase());
            }
        } catch (final IllegalArgumentException e) {
            System.out.println("Error registering aircraft: " + e.getMessage());
        } catch (final IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (final Exception e) {
            System.out.println("Error registering aircraft: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Add Aircraft to Fleet";
    }
}
