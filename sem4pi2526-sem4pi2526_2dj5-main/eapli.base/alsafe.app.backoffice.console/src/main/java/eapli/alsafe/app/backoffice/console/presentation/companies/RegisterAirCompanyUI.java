package eapli.alsafe.app.backoffice.console.presentation.companies;

import eapli.alsafe.companies.application.RegisterAirCompanyController;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterAirCompanyUI extends AbstractUI {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterAirCompanyUI.class);

    private final RegisterAirCompanyController controller = new RegisterAirCompanyController();

    @Override
    public boolean doShow() {
        try {
            String iata = Console.readLine("Enter the IATA code of the company (2 uppercase letters): ");

            while (iata.length() != 2) {
                iata = Console.readLine("Invalid input. Please enter a valid IATA code (2 uppercase letters): ");
            }

            iata = iata.toUpperCase();
            controller.checkIATA(iata);

            String icao = Console.readLine("Enter the ICAO code of the company (2 or 3 uppercase letters): ");

            while (icao.length() != 2 && icao.length() != 3) {
                icao = Console.readLine("Invalid input. Please enter a valid ICAO code (2 or 3 uppercase letters): ");
            }

            icao = icao.toUpperCase();
            controller.checkICAO(icao);

            String name = Console.readLine("Enter the name of the company: ");

            while (name.isBlank()) {
                name = Console.readLine("Invalid input. Please enter a valid name: ");
            }

            controller.checkName(name);

            String company = String.valueOf(controller.createAirTransportCompany(iata, icao, name));
            System.out.println(company);
        } catch (ConcurrencyException ex) {
            LOGGER.error("Error performing the operation", ex);
            System.out.println("Unfortunately there was an unexpected error in the application. Please try again and if the problem persists, contact your system administrator.");
        } catch (final IntegrityViolationException | IllegalArgumentException e) {
            System.out.println("Validation Error: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Register Air Transport Company";
    }
}
