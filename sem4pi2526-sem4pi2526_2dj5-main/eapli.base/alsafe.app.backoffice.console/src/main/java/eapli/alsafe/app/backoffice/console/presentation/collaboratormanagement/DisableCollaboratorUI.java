package eapli.alsafe.app.backoffice.console.presentation.collaboratormanagement;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.collaboratormanagement.application.CollaboratorDTO;
import eapli.alsafe.collaboratormanagement.application.DisableCollaboratorController;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.ArrayList;
import java.util.List;

public class DisableCollaboratorUI extends AbstractUI {

    private final DisableCollaboratorController ctrl = new DisableCollaboratorController();

    @Override
    protected boolean doShow() {
        // Same order as Add Collaborator: 1 = Area, 2 = Company
        final List<String> customerTypes = List.of("Air Control Area", "Air Transport Company");
        final SelectWidget<String> customerTypeSelector = new SelectWidget<>("Select Customer Type:", customerTypes);
        customerTypeSelector.show();
        final String customerType = customerTypeSelector.selectedElement();

        if (customerType == null) {
            return false;
        }

        if ("Air Control Area".equals(customerType)) {
            return disableFCO();
        } else {
            return disableATCC();
        }
    }

    private boolean disableFCO() {
        final SelectWidget<AirControlArea> areaSelector = new SelectWidget<>("Select an Air Control Area:",
                ctrl.getAirControlAreas());
        areaSelector.show();
        final AirControlArea selectedArea = areaSelector.selectedElement();
        if (selectedArea == null) {
            return false;
        }

        final List<CollaboratorDTO> collaborators = new ArrayList<>();
        ctrl.getActiveCollaboratorsOfArea(selectedArea).forEach(collaborators::add);

        if (collaborators.isEmpty()) {
            System.out.println("No active collaborators found for this area.");
            return false;
        }

        final SelectWidget<CollaboratorDTO> collaboratorSelector = new SelectWidget<>("Select collaborator to disable:",
                collaborators);
        collaboratorSelector.show();
        final CollaboratorDTO selected = collaboratorSelector.selectedElement();
        if (selected == null) {
            return false;
        }

        final String confirm = Console.readLine("Are you sure you want to disable " + selected.name + "? (y/n): ");
        if (!"y".equalsIgnoreCase(confirm) && !"yes".equalsIgnoreCase(confirm)) {
            System.out.println("Operation cancelled.");
            return false;
        }

        try {
            final boolean ok = ctrl.disableCollaboratorFCO(selected);
            if (ok) {
                System.out.println("Collaborator was disabled successfully.");
            } else {
                System.out.println("Could not disable collaborator (already inactive or invalid state).");
            }
        } catch (final IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (final Exception e) {
            System.out.println("Error disabling collaborator: " + e.getMessage());
        }

        return false;
    }

    private boolean disableATCC() {
        final SelectWidget<AirTransportCompany> companySelector = new SelectWidget<>("Select a Company:",
                ctrl.getAirTransportCompanies());
        companySelector.show();
        final AirTransportCompany selectedCompany = companySelector.selectedElement();
        if (selectedCompany == null) {
            return false;
        }

        final List<CollaboratorDTO> collaborators = new ArrayList<>();
        ctrl.getActiveCollaboratorsOfCompany(selectedCompany).forEach(collaborators::add);

        if (collaborators.isEmpty()) {
            System.out.println("No active collaborators found for this company.");
            return false;
        }

        final SelectWidget<CollaboratorDTO> collaboratorSelector = new SelectWidget<>("Select collaborator to disable:",
                collaborators);
        collaboratorSelector.show();
        final CollaboratorDTO selected = collaboratorSelector.selectedElement();
        if (selected == null) {
            return false;
        }

        final String confirm = Console.readLine("Are you sure you want to disable " + selected.name + "? (y/n): ");
        if (!"y".equalsIgnoreCase(confirm) && !"yes".equalsIgnoreCase(confirm)) {
            System.out.println("Operation cancelled.");
            return false;
        }

        try {
            final boolean ok = ctrl.disableCollaboratorATCC(selected);
            if (ok) {
                System.out.println("Collaborator was disabled successfully.");
            } else {
                System.out.println("Could not disable collaborator (already inactive or invalid state).");
            }
        } catch (final IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (final Exception e) {
            System.out.println("Error disabling collaborator: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Disable a Customer's Collaborator";
    }
}
