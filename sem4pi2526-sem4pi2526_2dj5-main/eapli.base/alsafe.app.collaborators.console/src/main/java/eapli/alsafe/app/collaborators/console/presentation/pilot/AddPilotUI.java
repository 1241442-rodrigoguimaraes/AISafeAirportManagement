package eapli.alsafe.app.collaborators.console.presentation.pilot;

import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.pilotmanagement.application.AddPilotController;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("squid:S106")
public class AddPilotUI extends AbstractUI {

    private final AddPilotController controller = new AddPilotController();

    @Override
    protected boolean doShow() {
        final List<aircraftModel> models = new ArrayList<>();
        controller.aircraftModels().forEach(models::add);

        if (models.isEmpty()) {
            System.out.println("No aircraft models are available for pilot certification.");
            return false;
        }

        final String name = Console.readLine("Pilot name:");
        final String email = Console.readLine("Pilot email:");
        final String phone = Console.readLine("Pilot phone:");
        final String password = Console.readLine("Temporary password:");
        final Set<aircraftModel> selectedModels = selectCertifiedModels(models);

        if (selectedModels.isEmpty()) {
            System.out.println("A pilot must be certified for at least one aircraft model.");
            return false;
        }

        try {
            final Pilot pilot = controller.addPilot(name, email, phone, password, selectedModels);
            System.out.println("Pilot added successfully to your company.");
            System.out.println(pilot);
        } catch (final IllegalArgumentException e) {
            System.out.println("Error adding pilot: " + e.getMessage());
        } catch (final IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (final Exception e) {
            System.out.println("Error adding pilot: " + e.getMessage());
        }

        return false;
    }

    private Set<aircraftModel> selectCertifiedModels(final List<aircraftModel> models) {
        final Set<aircraftModel> selectedModels = new HashSet<>();
        boolean addMore;
        do {
            final SelectWidget<aircraftModel> modelSelector = new SelectWidget<>(
                    "Select a certified aircraft model:", models);
            modelSelector.show();
            final aircraftModel selectedModel = modelSelector.selectedElement();
            if (selectedModel != null) {
                selectedModels.add(selectedModel);
            }
            addMore = Console.readBoolean("Add another aircraft model certification? (y/n)");
        } while (addMore);

        return selectedModels;
    }

    @Override
    public String headline() {
        return "Add Pilot";
    }
}
