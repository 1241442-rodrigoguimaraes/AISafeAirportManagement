package eapli.alsafe.app.collaborators.console.presentation.pilot;

import eapli.alsafe.pilotmanagement.application.ListPilotRosterController;
import eapli.alsafe.pilotmanagement.application.PilotDTO;
import eapli.framework.presentation.console.AbstractUI;

@SuppressWarnings("squid:S106")
public class ListPilotRosterUI extends AbstractUI {

    private final ListPilotRosterController controller = new ListPilotRosterController();

    @Override
    protected boolean doShow() {
        try {
            final Iterable<PilotDTO> roster = controller.activePilotRoster();
            boolean hasPilots = false;

            for (final PilotDTO pilot : roster) {
                hasPilots = true;
                System.out.println(pilot);
            }

            if (!hasPilots) {
                System.out.println("There are no active pilots in your company's roster.");
            }
        } catch (final IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (final Exception e) {
            System.out.println("Error listing pilot roster: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Pilot Roster";
    }
}
