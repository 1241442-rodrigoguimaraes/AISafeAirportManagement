package eapli.alsafe.app.backoffice.console.presentation.aircraftModels;

import eapli.alsafe.aircraftModelMagnement.application.AddCertifiedEngineController;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.engineModelMagnement.Domain.engineModel;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddCertifiedEngineUI extends AbstractUI {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddCertifiedEngineUI.class);

    private final AddCertifiedEngineController controller = new AddCertifiedEngineController();

    @Override
    protected boolean doShow() {

        try {
            Iterable<aircraftModel> ams = controller.getAllAircraftModels();
            final SelectWidget<aircraftModel> selector = new SelectWidget<>("Select Aircraft Model:", ams);
            selector.show();
            aircraftModel am = selector.selectedElement();

            if (am == null) {
                System.out.println("Aborting operation. No aircraft model selected.");
                return false;
            }

            Iterable<engineModel> ems = controller.getCompatibleEngineModels(am);
            final SelectWidget<engineModel> selector2 = new SelectWidget<>("Select Engine Model:", ems);
            selector2.show();
            engineModel em = selector2.selectedElement();

            if (em == null) {
                System.out.println("Aborting operation. No engine model selected.");
                return false;
            }

            controller.addCertifiedEngine(am, em);
            System.out.println("Engine " + em.getName() + " added successfully to the aircraft model " + am.name());
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
        return "Add Certified Engine";
    }
}
