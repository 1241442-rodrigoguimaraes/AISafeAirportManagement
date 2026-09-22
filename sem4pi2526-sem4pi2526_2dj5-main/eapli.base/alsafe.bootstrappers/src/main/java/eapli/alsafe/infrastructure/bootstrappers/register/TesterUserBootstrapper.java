package eapli.alsafe.infrastructure.bootstrappers.register;

import java.util.HashSet;
import java.util.Set;

import eapli.alsafe.infrastructure.bootstrappers.AbstractUserBootstrapper;
import eapli.alsafe.infrastructure.bootstrappers.TestDataConstants;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.actions.Action;
import eapli.framework.infrastructure.authz.domain.model.Role;

public class TesterUserBootstrapper extends AbstractUserBootstrapper implements Action {

    @Override
    public boolean execute() {
        final Set<Role> roles = new HashSet<>();
        roles.add(Roles.ADMIN);
        roles.add(Roles.BACKOFFICE_OPERATOR);
        roles.add(Roles.PILOT);
        roles.add(Roles.FLIGHT_CONTROL_OPERATOR);
        roles.add(Roles.WEATHER_PERSON);
        roles.add(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        registerUser("tester", TestDataConstants.PASSWORD1, "Tester", "User", "tester@alsafe.com", "999999999", roles);
        return true;
    }
}
