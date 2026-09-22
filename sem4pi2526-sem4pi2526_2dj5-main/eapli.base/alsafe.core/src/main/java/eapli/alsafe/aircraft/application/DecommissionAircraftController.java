package eapli.alsafe.aircraft.application;

import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.aircraft.domain.MaintenanceStatus;
import eapli.alsafe.aircraft.domain.RegistrationID;
import eapli.alsafe.aircraft.repositories.AircraftRepository;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.repositories.AirCompanyRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * US071 — decommission (retire) an aircraft from the company's fleet.
 */
@UseCaseController
public class DecommissionAircraftController {

    private final AuthorizationService authz;
    private final AircraftRepository aircraftRepository;
    private final CollaboratorRepositoryATCC collaboratorRepository;
    private final AirCompanyRepository companyRepository;

    public DecommissionAircraftController() {
        this(AuthzRegistry.authorizationService(), PersistenceContext.repositories().aircraft(),
                PersistenceContext.repositories().collaboratorsATCC(), PersistenceContext.repositories().companies());
    }

    public DecommissionAircraftController(final AuthorizationService authz,
                                          final AircraftRepository aircraftRepository,
                                          final CollaboratorRepositoryATCC collaboratorRepository,
                                          final AirCompanyRepository companyRepository) {
        if (authz == null || aircraftRepository == null || collaboratorRepository == null || companyRepository == null) {
            throw new IllegalArgumentException();
        }
        this.authz = authz;
        this.aircraftRepository = aircraftRepository;
        this.collaboratorRepository = collaboratorRepository;
        this.companyRepository = companyRepository;
    }

    /**
     * Aircraft that are not yet decommissioned for the authenticated collaborator's company.
     */
    public List<Aircraft> activeFleetForCurrentCompany() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR, Roles.ADMIN);
        final AirTransportCompany company = currentCompanyOrThrow();
        final List<Aircraft> list = new ArrayList<>();
        for (final Aircraft a : aircraftRepository.findByCompany(company)) {
            if (a.maintenanceStatus() != MaintenanceStatus.DECOMMISSIONED) {
                list.add(a);
            }
        }
        return list;
    }

    public Aircraft decommissionAircraft(final RegistrationID registrationId) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR, Roles.ADMIN);
        final AirTransportCompany company = currentCompanyOrThrow();

        final Aircraft aircraft = aircraftRepository.ofIdentity(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown aircraft registration."));

        if (!aircraft.company().identity().equals(company.identity())) {
            throw new SecurityException("Access denied: aircraft belongs to another company.");
        }

        aircraft.decommission();
        return aircraftRepository.save(aircraft);
    }

    private AirTransportCompany currentCompanyOrThrow() {
    final var session = authz.session().orElseThrow(() -> new IllegalStateException("No authenticated session"));
    final var loggedUser = session.authenticatedUser();
    final CollaboratorATCC collaborator = collaboratorRepository.findBySystemUser(loggedUser)
            .orElseThrow(() -> new IllegalStateException("No collaborator profile for authenticated user"));

    return collaboratorRepository.findCompanyByCollaborator(collaborator)
            .orElseThrow(() -> new IllegalStateException("Authenticated collaborator is not linked to an air transport company"));
}
}
