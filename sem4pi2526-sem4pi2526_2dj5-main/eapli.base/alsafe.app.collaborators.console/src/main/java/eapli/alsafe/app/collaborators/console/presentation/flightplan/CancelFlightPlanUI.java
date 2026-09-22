package eapli.alsafe.app.collaborators.console.presentation.flightplan;

import eapli.alsafe.flightPlan.application.FlightPlanController;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.flightPlan.domain.FlightPlanStatus;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CancelFlightPlanUI extends AbstractUI{

    private final FlightPlanController theController = new FlightPlanController();

    @Override
    protected boolean doShow() {
        
        final FlightPlan selectedFlightPlan = selectFlightPlan();
        boolean retry = true;
        if (selectedFlightPlan == null) {
            return false;
        }
        while (retry) {
            try {
                theController.cancelPlan(selectedFlightPlan);
                System.out.println("Flight plan cancelled successfully.");
                retry = false;
            }
            catch (IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
                retry = false;
            }
            catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return false;
    }

    @Override
    public String headline() {
        return "Cancel Flight Plan";
    }

    private FlightPlan selectFlightPlan() {
        Iterable<FlightPlan> flightPlans = theController.getFlightPlans();
        final List<FlightPlan> flightPlanList = new ArrayList<>();
        for(FlightPlan flightPlan : flightPlans) {
            if (!flightPlan.getStatus().equals(FlightPlanStatus.CANCELLED)) {
                flightPlanList.add(flightPlan);
            }
        }

        if (flightPlanList.isEmpty()) {
            System.out.println("No Flight Plans available for this Pilot.");
            return null;
        }

        final SelectWidget<FlightPlan> selector = new SelectWidget<>("Select an Flight Plan:", flightPlanList);
        selector.show();
        final FlightPlan selected = selector.selectedElement();
        if (selected == null) {
            System.out.println("No Flight Plan selected.");
        }
        return selected;
    }
}
