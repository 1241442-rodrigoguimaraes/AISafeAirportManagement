package eapli.alsafe.collaboratormanagement.application;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorEmail;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorFCO;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorPhone;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryFCO;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.repositories.AirCompanyRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.Optional;

@UseCaseController
public class EditCollaboratorController {

    private final AuthorizationService authz;
    private final CollaboratorRepositoryFCO collaboratorRepositoryFCO;
    private final CollaboratorRepositoryATCC collaboratorRepositoryATCC;
    private final AirControlAreaRepository airControlAreaRepository;
    private final AirCompanyRepository airCompanyRepository;

    public EditCollaboratorController() {
        this.authz = AuthzRegistry.authorizationService();
        this.collaboratorRepositoryFCO = PersistenceContext.repositories().collaboratorsFCO();
        this.collaboratorRepositoryATCC = PersistenceContext.repositories().collaboratorsATCC();
        this.airControlAreaRepository = PersistenceContext.repositories().areas();
        this.airCompanyRepository = PersistenceContext.repositories().companies();
    }

    public EditCollaboratorController(final AuthorizationService authz,
                                      final CollaboratorRepositoryFCO collaboratorRepositoryFCO,
                                      final CollaboratorRepositoryATCC collaboratorRepositoryATCC,
                                      final AirControlAreaRepository airControlAreaRepository,
                                      final AirCompanyRepository airCompanyRepository) {
        if (authz == null || collaboratorRepositoryFCO == null || collaboratorRepositoryATCC == null 
                || airControlAreaRepository == null || airCompanyRepository == null) {
            throw new IllegalArgumentException();
        }
        this.authz = authz;
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

    public Iterable<CollaboratorFCO> activeCollaboratorsOfArea(final AirControlArea area) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);
        return collaboratorRepositoryFCO.findActiveByCustomerArea(area);
    }

    public Iterable<CollaboratorATCC> activeCollaboratorsOfCompany(final AirTransportCompany company) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);
        return collaboratorRepositoryATCC.findAll();
    }

    public CollaboratorFCO updateEmailAndPhoneFCO(final CollaboratorFCO collaborator, final String email,
                                                    final String phone) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);

        final CollaboratorEmail newEmail = CollaboratorEmail.valueOf(email);
        final CollaboratorPhone newPhone = CollaboratorPhone.valueOf(phone);

        if (!newEmail.equals(collaborator.email())) {
            final Optional<CollaboratorFCO> other = collaboratorRepositoryFCO.findByEmail(newEmail);
            if (other.isPresent() && !other.get().sameAs(collaborator)) {
                throw new IllegalArgumentException("Another collaborator already uses this email.");
            }
        }

        collaborator.changeEmailAndPhone(newEmail, newPhone);
        return collaboratorRepositoryFCO.save(collaborator);
    }

    public CollaboratorATCC updateEmailAndPhoneATCC(final CollaboratorATCC collaborator, final String email,
                                                     final String phone) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);

        final CollaboratorEmail newEmail = CollaboratorEmail.valueOf(email);
        final CollaboratorPhone newPhone = CollaboratorPhone.valueOf(phone);

        if (!newEmail.equals(collaborator.email())) {
            final Optional<CollaboratorATCC> other = collaboratorRepositoryATCC.findByEmail(newEmail);
            if (other.isPresent() && !other.get().sameAs(collaborator)) {
                throw new IllegalArgumentException("Another collaborator already uses this email.");
            }
        }

        collaborator.changeEmailAndPhone(newEmail, newPhone);
        return collaboratorRepositoryATCC.save(collaborator);
    }
}
