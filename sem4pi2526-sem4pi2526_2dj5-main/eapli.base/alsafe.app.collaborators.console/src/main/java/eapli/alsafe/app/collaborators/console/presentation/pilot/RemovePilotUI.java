package eapli.alsafe.app.collaborators.console.presentation.pilot;

import eapli.alsafe.pilotmanagement.application.RemovePilotController;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.List;

@SuppressWarnings("squid:S106")
public class RemovePilotUI extends AbstractUI {

    private final RemovePilotController controller = new RemovePilotController();

    @Override
    protected boolean doShow() {
        final List<Pilot> pilots = controller.activePilotsForCurrentCompany();
        if (pilots.isEmpty()) {
            System.out.println("There are no active pilots available to deactivate in your company's roster.");
            return false;
        }

        final SelectWidget<Pilot> selector = new SelectWidget<>("Select pilot to deactivate:", pilots);
        selector.show();
        final Pilot selected = selector.selectedElement();
        if (selected == null) {
            return false;
        }

        System.out.println("Selected: " + selected);
        final String confirm = Console.readLine("Confirm deactivation? (Y/N)");
        if (!"Y".equalsIgnoreCase(confirm)) {
            System.out.println("Operation cancelled.");
            return false;
        }

        try {
            final Pilot deactivated = controller.deactivatePilot(selected.identity());
            System.out.println("Pilot deactivated successfully: " + deactivated.name());
        } catch (final IllegalStateException e) {
            System.out.println(e.getMessage());
        } catch (final SecurityException e) {
            System.out.println("Access denied.");
        } catch (final Exception e) {
            System.out.println("Unable to deactivate pilot: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Remove Pilot";
    }
}
