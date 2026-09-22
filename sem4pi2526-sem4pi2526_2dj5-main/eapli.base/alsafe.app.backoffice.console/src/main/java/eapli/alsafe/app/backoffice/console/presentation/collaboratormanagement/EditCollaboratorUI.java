package eapli.alsafe.app.backoffice.console.presentation.collaboratormanagement;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.collaboratormanagement.application.EditCollaboratorController;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.ArrayList;
import java.util.List;

public class EditCollaboratorUI extends AbstractUI {

    private final EditCollaboratorController theController = new EditCollaboratorController();

    @Override
    protected boolean doShow() {
        // Same order as Add / List / Disable: 1 = Area, 2 = Company
        final List<String> customerTypes = List.of("Air Control Area", "Air Transport Company");
        final SelectWidget<String> customerTypeSelector = new SelectWidget<>("Select Customer Type:", customerTypes);
        customerTypeSelector.show();
        final String customerType = customerTypeSelector.selectedElement();

        if (customerType == null) {
            return false;
        }

        if ("Air Control Area".equals(customerType)) {
            return editFCO();
        } else {
            return editATCC();
        }
    }

    private boolean editFCO() {
        final SelectWidget<AirControlArea> areaSelector = new SelectWidget<>("Select an Air Control Area:",
                theController.getAirControlAreas());
        areaSelector.show();
        final AirControlArea selectedArea = areaSelector.selectedElement();
        if (selectedArea == null) {
            return false;
        }

        final List<CollaboratorFCO> collaborators = new ArrayList<>();
        theController.activeCollaboratorsOfArea(selectedArea).forEach(collaborators::add);

        if (collaborators.isEmpty()) {
            System.out.println("No active collaborators found for this area.");
            return false;
        }

        final SelectWidget<CollaboratorFCO> collaboratorSelector = new SelectWidget<>("Select Collaborator:",
                collaborators);
        collaboratorSelector.show();
        final CollaboratorFCO selected = collaboratorSelector.selectedElement();
        if (selected == null) {
            return false;
        }

        System.out.println("Current email: " + selected.email());
        System.out.println("Current phone: " + selected.phoneNumber());

        final String newEmail = Console.readLine("New email:");
        final String newPhone = Console.readLine("New phone:");

        try {
            final CollaboratorFCO updated = theController.updateEmailAndPhoneFCO(selected, newEmail, newPhone);
            System.out.println("Collaborator updated: " + updated);
        } catch (final Exception e) {
            System.out.println("Could not update collaborator: " + e.getMessage());
        }

        return false;
    }

    private boolean editATCC() {
        final SelectWidget<AirTransportCompany> companySelector = new SelectWidget<>("Select a Company:",
                theController.getAirTransportCompanies());
        companySelector.show();
        final AirTransportCompany selectedCompany = companySelector.selectedElement();
        if (selectedCompany == null) {
            return false;
        }

        final List<CollaboratorATCC> collaborators = new ArrayList<>();
        theController.activeCollaboratorsOfCompany(selectedCompany).forEach(collaborators::add);

        if (collaborators.isEmpty()) {
            System.out.println("No active collaborators found for this company.");
            return false;
        }

        final SelectWidget<CollaboratorATCC> collaboratorSelector = new SelectWidget<>("Select Collaborator:",
                collaborators);
        collaboratorSelector.show();
        final CollaboratorATCC selected = collaboratorSelector.selectedElement();
        if (selected == null) {
            return false;
        }

        System.out.println("Current email: " + selected.email());
        System.out.println("Current phone: " + selected.phoneNumber());

        final String newEmail = Console.readLine("New email:");
        final String newPhone = Console.readLine("New phone:");

        try {
            final CollaboratorATCC updated = theController.updateEmailAndPhoneATCC(selected, newEmail, newPhone);
            System.out.println("Collaborator updated: " + updated);
        } catch (final Exception e) {
            System.out.println("Could not update collaborator: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Edit Collaborator (email & phone)";
    }
}
