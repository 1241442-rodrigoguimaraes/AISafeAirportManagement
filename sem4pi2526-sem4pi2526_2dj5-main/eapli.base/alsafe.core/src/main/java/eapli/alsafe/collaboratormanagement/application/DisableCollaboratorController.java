package eapli.alsafe.collaboratormanagement.application;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryFCO;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.repositories.AirCompanyRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserManagementService;

import java.util.ArrayList;
import java.util.List;

@UseCaseController
public class DisableCollaboratorController {

    private final AuthorizationService authz;
    private final UserManagementService userSvc;
    private final AirCompanyRepository companyRepository;
    private final AirControlAreaRepository areaRepository;
    private final CollaboratorRepositoryFCO collaboratorRepositoryFCO;
    private final CollaboratorRepositoryATCC collaboratorRepositoryATCC;

    public DisableCollaboratorController() {
        this(AuthzRegistry.authorizationService(), AuthzRegistry.userService(),
                PersistenceContext.repositories().companies(), PersistenceContext.repositories().areas(),
                PersistenceContext.repositories().collaboratorsFCO(), PersistenceContext.repositories().collaboratorsATCC());
    }

    public DisableCollaboratorController(final AuthorizationService authz, final UserManagementService userSvc,
                                         final AirCompanyRepository companyRepository,
                                         final AirControlAreaRepository areaRepository,
                                         final CollaboratorRepositoryFCO collaboratorRepositoryFCO,
                                         final CollaboratorRepositoryATCC collaboratorRepositoryATCC) {
        if (authz == null || userSvc == null || companyRepository == null || areaRepository == null
                || collaboratorRepositoryFCO == null || collaboratorRepositoryATCC == null) {
            throw new IllegalArgumentException();
        }
        this.authz = authz;
        this.userSvc = userSvc;
        this.companyRepository = companyRepository;
        this.areaRepository = areaRepository;
        this.collaboratorRepositoryFCO = collaboratorRepositoryFCO;
        this.collaboratorRepositoryATCC = collaboratorRepositoryATCC;
    }

    public Iterable<AirTransportCompany> getAirTransportCompanies() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);
        return companyRepository.findAll();
    }

    public Iterable<AirControlArea> getAirControlAreas() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);
        return areaRepository.findAll();
    }

    public Iterable<CollaboratorDTO> getActiveCollaboratorsOfCompany(final AirTransportCompany company) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);
        return toDTOATCC(collaboratorRepositoryATCC.findActiveByCompany(company));
    }

    public Iterable<CollaboratorDTO> getActiveCollaboratorsOfArea(final AirControlArea area) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);
        return toDTOFCO(collaboratorRepositoryFCO.findActiveByCustomerArea(area));
    }

    public boolean disableCollaboratorFCO(final CollaboratorDTO collaboratorDTO) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);

        final CollaboratorFCO collaborator = collaboratorRepositoryFCO.findByEmail(CollaboratorEmail.valueOf(collaboratorDTO.email))
                .orElseThrow(() -> new IllegalArgumentException("Collaborator not found"));

        if (!collaborator.canBeDisabled()) {
            return false;
        }

        userSvc.deactivateUser(collaborator.user());
        return true;
    }

    public boolean disableCollaboratorATCC(final CollaboratorDTO collaboratorDTO) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);

        final CollaboratorATCC collaborator = collaboratorRepositoryATCC.findByEmail(CollaboratorEmail.valueOf(collaboratorDTO.email))
                .orElseThrow(() -> new IllegalArgumentException("Collaborator not found"));

        if (!collaborator.canBeDisabled()) {
            return false;
        }

        // Persist deactivation on SystemUser only — UserManagementService updates the authz user.
        // Do not merge/save Collaborator here: it can trigger RollbackException (version flush / stale aggregate)
        // in the same persistence context after the linked user was modified.
        userSvc.deactivateUser(collaborator.user());
        return true;
    }

    private Iterable<CollaboratorDTO> toDTOFCO(final Iterable<CollaboratorFCO> collaborators) {
        final List<CollaboratorDTO> dtos = new ArrayList<>();
        for (final CollaboratorFCO c : collaborators) {
            dtos.add(CollaboratorDTO.fromCollaboratorFCO(c));
        }
        return dtos;
    }

    private Iterable<CollaboratorDTO> toDTOATCC(final Iterable<CollaboratorATCC> collaborators) {
        final List<CollaboratorDTO> dtos = new ArrayList<>();
        for (final CollaboratorATCC c : collaborators) {
            dtos.add(CollaboratorDTO.fromCollaboratorATCC(c));
        }
        return dtos;
    }
}
