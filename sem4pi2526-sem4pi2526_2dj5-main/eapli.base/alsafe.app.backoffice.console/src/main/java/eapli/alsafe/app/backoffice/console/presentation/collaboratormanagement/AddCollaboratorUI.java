package eapli.alsafe.app.backoffice.console.presentation.collaboratormanagement;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.collaboratormanagement.application.AddCollaboratorController;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.framework.infrastructure.authz.domain.model.RandomRawPassword;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

public class AddCollaboratorUI extends AbstractUI {

    private final AddCollaboratorController theController = new AddCollaboratorController();

    @Override
    protected boolean doShow() {

        AirControlArea area = null;
        AirTransportCompany company = null;
        int completedAssociations = 0;

        while (completedAssociations == 0) {
            switch (Console.readLine("Associate Collaborator to: \n 1. Air Control Area \n 2. Air Transport Company \nOption:")) {
                case "1" -> {
                    final Iterable<AirControlArea> areas = this.theController.getAirControlAreas();

                    final SelectWidget<AirControlArea> selector = new SelectWidget<>("Air Control Areas:", areas);
                    selector.show();
                    area = selector.selectedElement();
                    completedAssociations = 1;
                }
                case "2" -> {
                    final Iterable<AirTransportCompany> companies = this.theController.getAirTransportCompanies();

                    final SelectWidget<AirTransportCompany> selector = new SelectWidget<>("Air Transport Companies:", companies);
                    selector.show();
                    company = selector.selectedElement();
                    completedAssociations = 1;
                }
                default -> {
                    System.out.println("Invalid option.");
                }
            }
        }

        if (area == null && company == null) {
            return false;
        }

        final String name = Console.readLine("Collaborator Name:");
        final String email = Console.readLine("Collaborator Email:");
        final String phone = Console.readLine("Collaborator Phone:");
        String password;
        switch (Console.readLine("Generate random password? (Y/N)")) {
            case "Y", "y", "" -> {
                password = new RandomRawPassword().toString();
                System.out.println("Generated password: " + password);
            }
            case "N", "n" -> {
                password = Console.readLine("Collaborator Password:");
            }
            default -> {
                System.out.println("Invalid option. Collaborator not registered.");
                return false;
            }
        }

        try {
            if(area != null){
                final CollaboratorFCO collaboratorFCO = this.theController.addCollaboratorFCO(area, name, email, phone, password);
                System.out.println("Collaborator FCO registered successfully: " + collaboratorFCO.toString());
            }else{
                final CollaboratorATCC collaboratorATCC = this.theController.addCollaboratorATCC(name, email, phone, password, company);
                System.out.println("Collaborator ATCC registered successfully: " + collaboratorATCC.toString());

            }
        } catch (final Exception e) {
            System.out.println("Error registering collaborator: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Add Collaborator";
    }
}
