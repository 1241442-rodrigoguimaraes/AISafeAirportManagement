package eapli.alsafe.companies.application;

import eapli.alsafe.companies.domain.AirCompanyBuilder;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.companies.repositories.AirCompanyRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class RegisterAirCompanyController {

    private final AuthorizationService authz;
    private final AirCompanyBuilder atcBuilder;
    private final AirCompanyRepository atcRepository;

    public RegisterAirCompanyController() {
        this(AuthzRegistry.authorizationService(), new AirCompanyBuilder(), PersistenceContext.repositories().companies());
    }

    public RegisterAirCompanyController(final AuthorizationService authz, final AirCompanyBuilder atcBuilder, final AirCompanyRepository atcRepository) {
        if (authz == null || atcBuilder == null || atcRepository == null) throw new IllegalArgumentException();

        this.authz = authz;
        this.atcBuilder = atcBuilder;
        this.atcRepository = atcRepository;
    }

    public void checkIATA(final String iata) {
        if (atcRepository.findByIATACode(iata).isPresent()) throw new IntegrityViolationException("IATA code already registered");
    }

    public void checkICAO(final String icao) {
        if (atcRepository.findByICAOCode(icao).isPresent()) throw new IntegrityViolationException("ICAO code already registered");
    }

    public void checkName(final String name) {
        if (atcRepository.findByName(name).isPresent()) throw new IntegrityViolationException("Name already registered");
    }

    public AirTransportCompany createAirTransportCompany(final String iata, final String icao, final String name) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR);

        final AirTransportCompany company = atcBuilder.with(iata, icao, name).build();

        return atcRepository.save(company);
    }
}
