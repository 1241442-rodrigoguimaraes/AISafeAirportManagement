package eapli.alsafe.app.backoffice.console.presentation.collaboratormanagement;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.collaboratormanagement.application.CollaboratorDTO;
import eapli.alsafe.collaboratormanagement.application.ListCollaboratorsController;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.ArrayList;
import java.util.List;

public class ListCollaboratorsUI extends AbstractUI {
    private final ListCollaboratorsController ctrl = new ListCollaboratorsController();

    @Override
    protected boolean doShow() {
        final List<String> customerTypes = List.of("Air Transport Company", "Air Control Area", "ATCC");
        final SelectWidget<String> customerTypeSelector = new SelectWidget<>("Select Category:", customerTypes);
        customerTypeSelector.show();
        final String customerType = customerTypeSelector.selectedElement();

        if (customerType == null) {
            return false;
        }

        final List<CollaboratorATCC> collaboratorsATCC = new ArrayList<>();

        final List<CollaboratorDTO> collaborators = new ArrayList<>();

        if ("Air Transport Company".equals(customerType)) {
            final SelectWidget<AirTransportCompany> companySelector = new SelectWidget<>("Select a Company:",
                    ctrl.getAirTransportCompanies());
            companySelector.show();
            final AirTransportCompany selectedCompany = companySelector.selectedElement();
            if (selectedCompany == null) {
                return false;
            }
            ctrl.listActiveCollaboratorsOfCompany(selectedCompany).forEach(collaboratorsATCC::add);
        } else if ("Air Control Area".equals(customerType)) {
            final SelectWidget<AirControlArea> areaSelector = new SelectWidget<>("Select an Air Control Area:",
                    ctrl.getAirControlAreas());
            areaSelector.show();
            final AirControlArea selectedArea = areaSelector.selectedElement();
            if (selectedArea == null) {
                return false;
            }
            ctrl.listActiveCollaboratorsOfArea(selectedArea).forEach(collaborators::add);
        } else {
            ctrl.listAllATCC().forEach(collaborators::add);
        }

        System.out.println("Active collaborators:");
        if (!collaboratorsATCC.isEmpty()) {
            for (final CollaboratorATCC collaborator : collaboratorsATCC) {
                System.out.println(collaborator);
            }
        } else if (!collaborators.isEmpty()) {
            for (final CollaboratorDTO collaborator : collaborators) {
                System.out.println(collaborator);
            }
        }else {
            System.out.println("No active collaborators found.");
            return false;
        }

        return false;
    }

    @Override
    public String headline() {
        return "List Customer's Collaborators";
    }
}
