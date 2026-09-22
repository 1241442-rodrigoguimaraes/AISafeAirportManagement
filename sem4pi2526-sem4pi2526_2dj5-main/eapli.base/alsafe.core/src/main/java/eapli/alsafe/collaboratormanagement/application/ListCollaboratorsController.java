package eapli.alsafe.collaboratormanagement.application;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryFCO;
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

@UseCaseController
public class ListCollaboratorsController {
    private final AuthorizationService authz;
    private final AirCompanyRepository companyRepository;
    private final AirControlAreaRepository areaRepository;
    private final CollaboratorRepositoryFCO collaboratorRepositoryFCO;
    private final CollaboratorRepositoryATCC collaboratorRepositoryATCC;

    public ListCollaboratorsController() {
        this.authz = AuthzRegistry.authorizationService();
        this.companyRepository = PersistenceContext.repositories().companies();
        this.areaRepository = PersistenceContext.repositories().areas();
        this.collaboratorRepositoryFCO = PersistenceContext.repositories().collaboratorsFCO();
        this.collaboratorRepositoryATCC = PersistenceContext.repositories().collaboratorsATCC();
    }

    public ListCollaboratorsController(final AuthorizationService authz, final AirCompanyRepository companyRepository,
                                       final AirControlAreaRepository areaRepository,
                                       final CollaboratorRepositoryFCO collaboratorRepositoryFCO,
                                       final CollaboratorRepositoryATCC collaboratorRepositoryATCC) {
        if (authz == null || companyRepository == null || areaRepository == null
                || collaboratorRepositoryFCO == null || collaboratorRepositoryATCC == null) {
            throw new IllegalArgumentException();
        }
        this.authz = authz;
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

    public Iterable<CollaboratorATCC> listActiveCollaboratorsOfCompany(final AirTransportCompany company) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);
        return collaboratorRepositoryATCC.findActiveByCompany(company);
    }

    public Iterable<CollaboratorDTO> listActiveCollaboratorsOfArea(final AirControlArea area) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);
        // ATCC does not have direct relation to area in the entity, but let's assume we want to find ATCCs too if they were related, 
        // however the entity CollaboratorATCC shown before only had name, email, phone, user.
        // CollaboratorFCO has AirControlArea.
        return toDTOFCO(collaboratorRepositoryFCO.findActiveByCustomerArea(area));
    }

    public Iterable<CollaboratorDTO> listAllATCC() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);
        return toDTOATCC(collaboratorRepositoryATCC.findAll());
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
