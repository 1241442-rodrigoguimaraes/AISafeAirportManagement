package eapli.alsafe.collaboratormanagement.application;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCOBuilder;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCCBuilder;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryFCO;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.repositories.AirCompanyRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
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
public class AddCollaboratorController {

    private final AuthorizationService authz;
    private final UserManagementService userSvc;
    private final CollaboratorRepositoryFCO collaboratorRepositoryFCO;
    private final CollaboratorRepositoryATCC collaboratorRepositoryATCC;
    private final AirControlAreaRepository airControlAreaRepository;
    private final AirCompanyRepository airCompanyRepository;

    public AddCollaboratorController() {
        this.authz = AuthzRegistry.authorizationService();
        this.userSvc = AuthzRegistry.userService();
        this.collaboratorRepositoryFCO = PersistenceContext.repositories().collaboratorsFCO();
        this.collaboratorRepositoryATCC = PersistenceContext.repositories().collaboratorsATCC();
        this.airControlAreaRepository = PersistenceContext.repositories().areas();
        this.airCompanyRepository = PersistenceContext.repositories().companies();
    }

    public AddCollaboratorController(final AuthorizationService authz, final UserManagementService userSvc,
                                     final CollaboratorRepositoryFCO collaboratorRepositoryFCO,
                                     final CollaboratorRepositoryATCC collaboratorRepositoryATCC,
                                     final AirControlAreaRepository airControlAreaRepository,
                                     final AirCompanyRepository airCompanyRepository) {
        if (authz == null || userSvc == null || collaboratorRepositoryFCO == null || collaboratorRepositoryATCC == null || airControlAreaRepository == null) {
            throw new IllegalArgumentException();
        }
        this.authz = authz;
        this.userSvc = userSvc;
        this.collaboratorRepositoryFCO = collaboratorRepositoryFCO;
        this.collaboratorRepositoryATCC = collaboratorRepositoryATCC;
        this.airControlAreaRepository = airControlAreaRepository;
        this.airCompanyRepository = airCompanyRepository;
    }

    public Iterable<AirControlArea> getAirControlAreas() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);
        return airControlAreaRepository.findAll();
    }

    public Iterable<AirTransportCompany> getAirTransportCompanies() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);
        return airCompanyRepository.findAll();
    }

    public SystemUser user(String name, String email, String password, Set<Role> roles) {
        String[] nameParts = name.split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : nameParts[0];

        final SystemUser user = userSvc.registerNewUser(email, password, firstName, lastName, email, roles);

        return user;
    }

    public CollaboratorFCO addCollaboratorFCO(final AirControlArea area, final String name,
                                              final String email, final String phone, final String password) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);

        final Set<Role> roles = new HashSet<>();
        if(area != null) roles.add(Roles.FLIGHT_CONTROL_OPERATOR);

        SystemUser user = user(name, email, password, roles);

        final CollaboratorFCO collaboratorFCO = new CollaboratorFCOBuilder()
                .with(name, email, phone, user, area)
                .build();

        return collaboratorRepositoryFCO.save(collaboratorFCO);
    }

    public CollaboratorATCC addCollaboratorATCC(final String name, final String email,
                                              final String phone, final String password,
                                                final AirTransportCompany company) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);

        final Set<Role> roles = new HashSet<>();
        roles.add(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        SystemUser user = user(name, email, password, roles);

        final CollaboratorATCC collaboratorATCC = new CollaboratorATCCBuilder()
                .with(name, email, phone, user, company)
                .build();

        return collaboratorRepositoryATCC.save(collaboratorATCC);
    }


}
