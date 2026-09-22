package eapli.alsafe.pilotmanagement.application;

import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.flightPlan.repositories.FlightPlanRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.pilotmanagement.domain.Pilot;
import eapli.alsafe.pilotmanagement.repositories.PilotRepository;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserManagementService;

import java.util.ArrayList;
import java.util.List;

/**
 * US077 — deactivate a pilot from the company's roster.
 */
@UseCaseController
public class RemovePilotController {

    private final AuthorizationService authz;
    private final UserManagementService userSvc;
    private final PilotRepository pilotRepository;
    private final CollaboratorRepositoryATCC collaboratorRepository;
    private final FlightPlanRepository flightPlanRepository;

    public RemovePilotController() {
        this(AuthzRegistry.authorizationService(), AuthzRegistry.userService(),
                PersistenceContext.repositories().pilots(), PersistenceContext.repositories().collaboratorsATCC(),
                PersistenceContext.repositories().flightPlan());
    }

    public RemovePilotController(final AuthorizationService authz, final UserManagementService userSvc,
                                 final PilotRepository pilotRepository,
                                 final CollaboratorRepositoryATCC collaboratorRepository,
                                 final FlightPlanRepository flightPlanRepository) {
        if (authz == null || userSvc == null || pilotRepository == null || collaboratorRepository == null
                || flightPlanRepository == null) {
            throw new IllegalArgumentException();
        }
        this.authz = authz;
        this.userSvc = userSvc;
        this.pilotRepository = pilotRepository;
        this.collaboratorRepository = collaboratorRepository;
        this.flightPlanRepository = flightPlanRepository;
    }

    public List<Pilot> activePilotsForCurrentCompany() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        final AirTransportCompany company = authenticatedCollaboratorCompany();
        final List<Pilot> pilots = new ArrayList<>();
        pilotRepository.findActiveByCompany(company).forEach(pilots::add);
        return pilots;
    }

    public Pilot deactivatePilot(final Long pilotId) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        final AirTransportCompany company = authenticatedCollaboratorCompany();

        final Pilot pilot = pilotRepository.ofIdentity(pilotId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown pilot."));

        if (!pilot.company().identity().equals(company.identity())) {
            throw new SecurityException("Access denied: pilot belongs to another company.");
        }

        if (!pilot.isActive()) {
            throw new IllegalStateException("Pilot is already inactive.");
        }

        if (hasAssignedFlightPlans(pilot)) {
            throw new IllegalStateException("Cannot deactivate a pilot with assigned flight plans.");
        }

        userSvc.deactivateUser(pilot.user());
        return pilot;
    }

    private boolean hasAssignedFlightPlans(final Pilot pilot) {
        return flightPlanRepository.findAssignedFlightPlansByPilot(pilot).iterator().hasNext();
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
