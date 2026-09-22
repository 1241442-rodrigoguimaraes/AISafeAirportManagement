package eapli.alsafe.app.collaborators.console.presentation.airinfrastructure;

import eapli.framework.actions.Action;

public class CreateFlightRouteAction implements Action {

    @Override
    public boolean execute() {
        return new CreateFlightRouteUI().show();
    }
}
