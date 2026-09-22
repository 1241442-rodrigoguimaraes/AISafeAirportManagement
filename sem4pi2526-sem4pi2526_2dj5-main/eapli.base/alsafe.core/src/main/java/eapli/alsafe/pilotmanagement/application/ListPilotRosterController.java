package eapli.alsafe.pilotmanagement.application;

import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.pilotmanagement.repositories.PilotRepository;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UseCaseController
public class ListPilotRosterController {

    private final AuthorizationService authz;
    private final PilotRepository pilotRepository;
    private final CollaboratorRepositoryATCC collaboratorRepository;

    public ListPilotRosterController() {
        this(AuthzRegistry.authorizationService(), PersistenceContext.repositories().pilots(),
                PersistenceContext.repositories().collaboratorsATCC());
    }

    public ListPilotRosterController(final AuthorizationService authz, final PilotRepository pilotRepository,
                                     final CollaboratorRepositoryATCC collaboratorRepository) {
        if (authz == null || pilotRepository == null || collaboratorRepository == null) {
            throw new IllegalArgumentException();
        }
        this.authz = authz;
        this.pilotRepository = pilotRepository;
        this.collaboratorRepository = collaboratorRepository;
    }

    public Iterable<PilotDTO> activePilotRoster() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final AirTransportCompany company = authenticatedCollaboratorCompany();
        final Iterable<Pilot> pilots = pilotRepository.findActiveByCompany(company);
        final List<PilotDTO> result = new ArrayList<>();
        pilots.forEach(pilot -> result.add(toDto(pilot)));
        return result;
    }

    private PilotDTO toDto(final Pilot pilot) {
        final String certifiedModels = pilot.certifiedAircraftModels().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        return new PilotDTO(pilot.identity(), pilot.name(), pilot.email().toString(), pilot.phoneNumber(),
                pilot.company().getName(), pilot.isActive() ? "ACTIVE" : "INACTIVE", certifiedModels);
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
}
