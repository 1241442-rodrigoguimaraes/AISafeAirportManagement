package eapli.alsafe.infrastructure.bootstrappers;

import eapli.alsafe.collaboratormanagement.application.AddCollaboratorController;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.repositories.AirCompanyRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollaboratorATCCBootstrapper implements Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollaboratorATCCBootstrapper.class);
    private final CollaboratorRepositoryATCC collaboratorRepositoryATCC = PersistenceContext.repositories().collaboratorsATCC();
    private final AirCompanyRepository airCompanyRepository = PersistenceContext.repositories().companies();
    private final AddCollaboratorController addCollaboratorController = new AddCollaboratorController();


    @Override
    public boolean execute() {
        try {
            registerCollaboratorATCC();
            return true;
        } catch (final Exception e) {
            LOGGER.error("Error bootstrapping CollaboratorATCC", e);
            return false;
        }
    }

    private void registerCollaboratorATCC() {
        if (!collaboratorRepositoryATCC.findByEmail(new CollaboratorEmail("jv@gmail.com")).isPresent()) {
            if (!airCompanyRepository.findAll().iterator().hasNext()) {
                LOGGER.error("No Air Company available. Cannot register CollaboratorATCC {}");
                return;
            }

            final var collaborator = addCollaboratorController.addCollaboratorATCC(
                    "João Vitor",
                    "jv@gmail.com",
                    "987654321",
                    "Password1",
                    airCompanyRepository.findAll().iterator().next()
            );
            LOGGER.info("CollaboratorATCC registered: {}", collaborator);
        } else {
            LOGGER.warn("CollaboratorATCC with email {} already exists. Skipping registration.", "jv@gmail.com");
        }
    }
}