package eapli.alsafe.app.collaborators.console.presentation.aircraft;

import eapli.alsafe.aircraft.application.DecommissionAircraftController;
import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.aircraft.domain.RegistrationID;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.List;

@SuppressWarnings("squid:S106")
public class DecommissionAircraftUI extends AbstractUI {

    private final DecommissionAircraftController ctrl = new DecommissionAircraftController();

    @Override
    protected boolean doShow() {
        final List<Aircraft> fleet = ctrl.activeFleetForCurrentCompany();
        if (fleet.isEmpty()) {
            System.out.println("No aircraft available to decommission in your company's fleet.");
            return false;
        }

        final SelectWidget<Aircraft> selector = new SelectWidget<>("Select aircraft to retire from the fleet:", fleet);
        selector.show();
        final Aircraft selected = selector.selectedElement();
        if (selected == null) {
            return false;
        }

        System.out.println("Selected: " + selected);
        final String confirm = Console.readLine("Confirm decommission? (Y/N)");
        if (!"Y".equalsIgnoreCase(confirm)) {
            System.out.println("Operation cancelled.");
            return false;
        }

        try {
            final RegistrationID id = selected.identity();
            final Aircraft updated = ctrl.decommissionAircraft(id);
            System.out.println("Aircraft retired. Status is now " + updated.maintenanceStatus() + ".");
        } catch (final IllegalStateException e) {
            System.out.println(e.getMessage());
        } catch (final SecurityException e) {
            System.out.println("Access denied.");
        } catch (final Exception e) {
            System.out.println("Unable to decommission: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Decommission Aircraft";
    }
}
