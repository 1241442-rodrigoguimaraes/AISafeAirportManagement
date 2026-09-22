package eapli.alsafe.aircraft.application;


import eapli.alsafe.aircraft.domain.Aircraft;
import eapli.alsafe.aircraft.repositories.AircraftRepository;
import eapli.alsafe.aircraftModelMagnement.domain.aircraftModel;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.aircraftModelMagnement.domain.Maker;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.ArrayList;
import java.util.List;

@UseCaseController
public class ListFleetController {

    private final AuthorizationService authz;

    private final AircraftRepository aircraftRepository;

    private final aircraftModelRepository aircraftModelRepository;

    private final CollaboratorRepositoryATCC collaboratorRepository;

    public ListFleetController() {
        this(
                AuthzRegistry.authorizationService(),
                PersistenceContext.repositories().aircraft(),
                PersistenceContext.repositories().aircraftModels(),
                PersistenceContext.repositories().collaboratorsATCC()
        );
    }

    public ListFleetController(final AuthorizationService authz,
                               final AircraftRepository aircraftRepository,
                               final aircraftModelRepository aircraftModelRepository,
                               final CollaboratorRepositoryATCC collaboratorRepository) {

        this.authz = authz;
        this.aircraftRepository = aircraftRepository;
        this.aircraftModelRepository = aircraftModelRepository;
        this.collaboratorRepository = collaboratorRepository;
    }

    public AirTransportCompany authenticatedCollaboratorCompany() {

        authz.ensureAuthenticatedUserHasAnyOf(
                Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                Roles.ADMIN
        );

        final var session = authz.session()
                .orElseThrow(() -> new IllegalStateException("No authenticated session"));

        final var loggedUser = session.authenticatedUser();

        final CollaboratorATCC collaborator =
                collaboratorRepository.findBySystemUser(loggedUser)
                        .orElseThrow(() -> new IllegalStateException(
                                "No collaborator profile for authenticated user"));

        return collaboratorRepository.findCompanyByCollaborator(collaborator)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated collaborator is not linked to an air transport company"));
    }

    public Iterable<aircraftModel> aircraftModels() {

        authz.ensureAuthenticatedUserHasAnyOf(
                Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                Roles.ADMIN
        );

        return aircraftModelRepository.findAll();
    }

    public Iterable<Aircraft> fleet() {

        final AirTransportCompany company = authenticatedCollaboratorCompany();

        return aircraftRepository.findByCompany(company);
    }

    public Iterable<Aircraft> fleetByModel(final aircraftModel model) {

        if (model == null) {
            throw new IllegalArgumentException("Aircraft model cannot be null");
        }

        final AirTransportCompany company = authenticatedCollaboratorCompany();

        return aircraftRepository.findByCompanyAndModel(company, model);
    }

    public Iterable<Aircraft> fleetByMaker(final Maker maker) {

        if (maker == null) {
            throw new IllegalArgumentException("Maker cannot be null");
        }

        final List<Aircraft> result = new ArrayList<>();

        for (final Aircraft aircraft : fleet()) {
            if (aircraft.model().maker().equals(maker)) {
                result.add(aircraft);
            }
        }

        return result;
    }

    public Iterable<Aircraft> fleetByCapacity(final int capacity) {

        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }

        final List<Aircraft> result = new ArrayList<>();

        for (final Aircraft aircraft : fleet()) {
            if (aircraft.cabinConfiguration().totalSeats() == capacity) {
                result.add(aircraft);
            }
        }

        return result;
    }

    public AircraftDTO toDto(final Aircraft aircraft) {

        return new AircraftDTO(
                aircraft.identity().toString(),
                aircraft.model().toString(),
                aircraft.company().getName(),
                aircraft.cabinConfiguration().toString(),
                aircraft.maintenanceStatus().name()
        );
    }

}
