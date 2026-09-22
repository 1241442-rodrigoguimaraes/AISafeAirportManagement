package eapli.alsafe.app.collaborators.console.presentation.flightplan;

import eapli.alsafe.flightPlan.application.FlightPlanController;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.flightPlan.domain.FlightPlanStatus;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.ArrayList;
import java.util.List;

public class ListFlightPlanUI extends AbstractUI{

    private final FlightPlanController theController = new FlightPlanController();

    @Override
    protected boolean doShow() {

        Iterable<FlightPlan> flightPlans = theController.getFlightPlans();
        for(FlightPlan flightPlan : flightPlans) {
            System.out.println(flightPlan.toString() + " - " + flightPlan.getStatus());
        }
        return false;
    }

    @Override
    public String headline() {
        return "List Flight Plan";
    }
}
