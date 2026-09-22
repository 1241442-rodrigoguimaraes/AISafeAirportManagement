package eapli.alsafe.pilotmanagement.application;

import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.pilotmanagement.domain.PilotBuilder;
import eapli.alsafe.pilotmanagement.repositories.PilotRepository;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.util.HashSet;
import java.util.Set;

@UseCaseController
public class AddPilotController {

    private final AuthorizationService authz;
    private final UserManagementService userSvc;
    private final PilotRepository pilotRepository;
    private final CollaboratorRepositoryATCC collaboratorRepository;
    private final aircraftModelRepository aircraftModelRepository;

    public AddPilotController() {
        this(AuthzRegistry.authorizationService(), AuthzRegistry.userService(),
                PersistenceContext.repositories().pilots(), PersistenceContext.repositories().collaboratorsATCC(),
                PersistenceContext.repositories().aircraftModels());
    }

    public AddPilotController(final AuthorizationService authz, final UserManagementService userSvc,
                              final PilotRepository pilotRepository,
                              final CollaboratorRepositoryATCC collaboratorRepository,
                              final aircraftModelRepository aircraftModelRepository) {
        if (authz == null || userSvc == null || pilotRepository == null || collaboratorRepository == null
                || aircraftModelRepository == null) {
            throw new IllegalArgumentException();
        }
        this.authz = authz;
        this.userSvc = userSvc;
        this.pilotRepository = pilotRepository;
        this.collaboratorRepository = collaboratorRepository;
        this.aircraftModelRepository = aircraftModelRepository;
    }

    public Iterable<aircraftModel> aircraftModels() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        return aircraftModelRepository.findAll();
    }

    public Pilot addPilot(final String name, final String email, final String phone, final String password,
                          final Set<aircraftModel> certifiedAircraftModels) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final CollaboratorEmail pilotEmail = CollaboratorEmail.valueOf(email);
        if (pilotRepository.findByEmail(pilotEmail).isPresent()) {
            throw new IllegalArgumentException("A pilot with this email already exists");
        }

        final AirTransportCompany company = authenticatedCollaboratorCompany();
        final SystemUser systemUser = registerSystemUser(name, email, password, Set.of(Roles.PILOT));

        final Pilot pilot = new PilotBuilder()
                .with(name, email, phone, systemUser, company, certifiedAircraftModels)
                .build();

        return pilotRepository.save(pilot);
    }

    private SystemUser registerSystemUser(final String name, final String email, final String password,
                                          final Set<Role> roles) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Pilot name cannot be null or blank");
        }
        final String[] nameParts = name.trim().split(" ", 2);
        final String firstName = nameParts[0];
        final String lastName = nameParts.length > 1 ? nameParts[1] : nameParts[0];

        return userSvc.registerNewUser(email, password, firstName, lastName, email, new HashSet<>(roles));
    }

    private AirTransportCompany authenticatedCollaboratorCompany() {
        final var session = authz.session()
                .orElseThrow(() -> new IllegalStateException("No authenticated session"));
        final var loggedUser = session.authenticatedUser();

        final CollaboratorATCC collaborator = collaboratorRepository.findBySystemUser(loggedUser)
                .orElseThrow(() -> new IllegalStateException("No collaborator profile for authenticated user"));

        return collaboratorRepository.findCompanyByCollaborator(collaborator)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated collaborator is not linked to an air transport company"));
    }

    public aircraftModel getAircraftModelByName(final String modelName) {
        return aircraftModelRepository.findByName(modelName).orElseThrow(() -> new IllegalArgumentException("Aircraft model not found."));
    }
}
