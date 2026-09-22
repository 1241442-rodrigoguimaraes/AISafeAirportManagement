package eapli.alsafe.app.collaborators.console.presentation.flightplan;

import eapli.alsafe.flightPlan.application.FlightPlanController;
import eapli.alsafe.flightPlan.domain.FlightPlan;
import eapli.alsafe.flightPlan.domain.FlightPlanStatus;
import eapli.framework.infrastructure.authz.application.exceptions.UnauthorizedException;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ValidateFlightPlanUI extends AbstractUI{

    private final FlightPlanController theController = new FlightPlanController();

    @Override
    protected boolean doShow() {
        
        final FlightPlan selectedFlightPlan = selectFlightPlan();
        if (selectedFlightPlan == null) {
            return false;
        }
        try {
            FlightPlanStatus status = theController.validatePlan(selectedFlightPlan);
            if (status == FlightPlanStatus.VALIDATED) System.out.println("Flight plan validated successfully.");
            else System.out.println("Flight plan rejected.");
        }catch (UnauthorizedException e) {
            System.out.println("Authentication error: " + e.getMessage());
        }catch (IllegalArgumentException e) {
            System.out.println("Rejected: " + e.getMessage());
        }catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String headline() {
        return "Validate Flight Plan";
    }

    private FlightPlan selectFlightPlan() {
        Iterable<FlightPlan> flightPlans = theController.getFlightPlans();
        final List<FlightPlan> flightPlanList = new ArrayList<>();
        for(FlightPlan flightPlan : flightPlans) {
            if (flightPlan.getStatus().equals(FlightPlanStatus.SUBMITTED)) {
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
