package eapli.alsafe.app.backoffice.console.presentation.authz;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eapli.alsafe.usermanagement.application.ActivateUserController;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

@SuppressWarnings("squid:S106")
public class ActivateUserUI extends AbstractUI {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivateUserUI.class);

    private final ActivateUserController theController = new ActivateUserController();

    @Override
    protected boolean doShow() {
        final List<SystemUser> list = new ArrayList<>();
        final Iterable<SystemUser> iterable = this.theController.deactivatedUsers();
        if (!iterable.iterator().hasNext()) {
            System.out.println("There is no deactivated User");
        } else {
            int cont = 1;
            System.out.println("SELECT User to activate\n");
            System.out.printf("%-6s%-10s%-30s%-30s%n", "Nº:", "Username", "Firstname", "Lastname");
            for (final SystemUser user : iterable) {
                list.add(user);
                System.out.printf("%-6d%-10s%-30s%-30s%n", cont, user.username(), user.name().firstName(),
                        user.name().lastName());
                cont++;
            }

            final int option = Console.readInteger("Enter user nº to activate or 0 to finish ");
            if (option == 0) {
                System.out.println("No user selected");
            } else {
                try {
                    this.theController.activateUser(list.get(option - 1));
                } catch (IntegrityViolationException | ConcurrencyException ex) {
                    LOGGER.error("Error performing the operation", ex);
                    System.out.println(
                            "Unfortunatelly there was an unexpected error in the application. Please try again and if the problem persists, contact your system admnistrator.");
                }
            }
        }
        return false;
    }

    @Override
    public String headline() {
        return "Activate User";
    }
}
