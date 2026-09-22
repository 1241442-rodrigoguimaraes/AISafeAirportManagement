package eapli.alsafe.infrastructure.bootstrappers;

import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.collaboratormanagement.application.AddCollaboratorController;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryFCO;
import eapli.alsafe.companies.repositories.AirCompanyRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollaboratorFCOBootstrapper implements Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollaboratorFCOBootstrapper.class);
    private final CollaboratorRepositoryFCO collaboratorRepositoryATCC = PersistenceContext.repositories().collaboratorsFCO();
    private final AirControlAreaRepository airControlAreaRepository = PersistenceContext.repositories().areas();
    private final AddCollaboratorController addCollaboratorController = new AddCollaboratorController();


    @Override
    public boolean execute() {
        try {
            registerCollaboratorFCO();
            return true;
        } catch (final Exception e) {
            LOGGER.error("Error bootstrapping CollaboratorATCC", e);
            return false;
        }
    }

    private void registerCollaboratorFCO() {
        if (!collaboratorRepositoryATCC.findByEmail(new CollaboratorEmail("JoVi@gmail.com")).isPresent()) {
            if (!airControlAreaRepository.findAll().iterator().hasNext()) {
                LOGGER.error("No Air Company available. Cannot register CollaboratorFCO {}");
                return;
            }

            final var collaborator = addCollaboratorController.addCollaboratorFCO(
                    airControlAreaRepository.findAll().iterator().next(),
                    "Joana Vitoria",
                    "JoVi@gmail.com",
                    "987654320",
                    "Password1"
            );
            LOGGER.info("CollaboratorFCO registered: {}", collaborator);
        } else {
            LOGGER.warn("CollaboratorFCO with email {} already exists. Skipping registration.", "JoVi@gmail.com");
        }
    }
}