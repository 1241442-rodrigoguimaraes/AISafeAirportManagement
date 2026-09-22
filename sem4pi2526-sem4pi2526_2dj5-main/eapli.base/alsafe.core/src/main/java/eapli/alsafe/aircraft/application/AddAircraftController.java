package eapli.alsafe.aircraft.application;

import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.aircraft.domain.AircraftCountry;
import eapli.alsafe.aircraft.domain.CabinConfiguration;
import eapli.alsafe.aircraft.domain.CrewElement;
import eapli.alsafe.aircraft.domain.RegistrationID;
import eapli.alsafe.aircraft.repositories.AircraftRepository;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
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

@UseCaseController
public class AddAircraftController {

    private final AuthorizationService authz;
    private final aircraftModelRepository aircraftModelRepository;
    private final AircraftRepository aircraftRepository;
    private final CollaboratorRepositoryATCC collaboratorRepository;
    private final AirCompanyRepository companyRepository;

    public AddAircraftController() {
        this(AuthzRegistry.authorizationService(), PersistenceContext.repositories().aircraftModels(),
                PersistenceContext.repositories().aircraft(), PersistenceContext.repositories().collaboratorsATCC(),
                PersistenceContext.repositories().companies());
    }

    public AddAircraftController(final AuthorizationService authz,
                                 final aircraftModelRepository aircraftModelRepository,
                                 final AircraftRepository aircraftRepository,
                                 final CollaboratorRepositoryATCC collaboratorRepository,
                                 final AirCompanyRepository companyRepository) {
        if (authz == null || aircraftModelRepository == null || aircraftRepository == null
                || collaboratorRepository == null || companyRepository == null) {
            throw new IllegalArgumentException();
        }
        this.authz = authz;
        this.aircraftModelRepository = aircraftModelRepository;
        this.aircraftRepository = aircraftRepository;
        this.collaboratorRepository = collaboratorRepository;
        this.companyRepository = companyRepository;
    }

    public Iterable<aircraftModel> getAircraftModels() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR, Roles.ADMIN);
        return aircraftModelRepository.getAircraftModels();
    }

    public AirTransportCompany getAuthenticatedCollaboratorCompany() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR, Roles.ADMIN);
        final var session = authz.session().orElseThrow(() -> new IllegalStateException("No authenticated session"));
        final var loggedUser = session.authenticatedUser();
        final CollaboratorATCC collaborator = collaboratorRepository.findBySystemUser(loggedUser)
                .orElseThrow(() -> new IllegalStateException("No collaborator profile for authenticated user"));
        
        return collaboratorRepository.findCompanyByCollaborator(collaborator)
                .orElseThrow(() -> new IllegalStateException("Authenticated collaborator is not linked to an air transport company"));
    }

    public boolean addAircraft(final aircraftModel model, final String registrationIDStr, final String countryStr,
                               final int economySeats, final int businessSeats, final int firstClassSeats,
                               final int crewCount) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR, Roles.ADMIN);

        final RegistrationID registrationID = RegistrationID.valueOf(registrationIDStr);
        if (aircraftRepository.existsByRegistrationID(registrationID)) {
            throw new IllegalArgumentException("An aircraft with this registration ID already exists.");
        }

        final AircraftCountry country = new AircraftCountry(countryStr);
        final int maxSeats = model.maxPassengerSeats();
        final CabinConfiguration cabinConfig = new CabinConfiguration(economySeats, businessSeats, firstClassSeats, maxSeats);

        if (crewCount < 1) {
            throw new IllegalArgumentException("At least one crew element is required.");
        }
        final List<CrewElement> crew = new ArrayList<>();
        for (int i = 0; i < crewCount; i++) {
            crew.add(new CrewElement("CREW_" + (i + 1)));
        }

        final AirTransportCompany company = getAuthenticatedCollaboratorCompany();
        return aircraftRepository.addAircraft(model, registrationID, country, cabinConfig, crew, company);
    }

    public AircraftDTO toDto(final Aircraft aircraft) {
        return new AircraftDTO(
                aircraft.identity().toString(),
                aircraft.model().toString(),
                aircraft.company().getName(),
                aircraft.cabinConfiguration().toString(),
                aircraft.maintenanceStatus().name());
    }

    public aircraftModel getAircraftModelByName(final String modelName) {
        return aircraftModelRepository.findByName(modelName).orElseThrow(() -> new IllegalArgumentException("Aircraft model not found."));
    }
}
