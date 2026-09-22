package eapli.alsafe.airinfrastructure.application;

import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.airinfrastructure.domain.FlightRouteBuilder;
import eapli.alsafe.airinfrastructure.repositories.FlightRouteRepository;
import eapli.alsafe.airports.domain.Airport;
import eapli.alsafe.airports.repository.AirportRepository;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.validations.Preconditions;

import java.util.List;
import java.util.stream.StreamSupport;

@UseCaseController
public class CreateFlightRouteController {

    private final AuthorizationService authz;
    private final FlightRouteBuilder routeBuilder;
    private final FlightRouteRepository frRepository;
    private final AirportRepository airportRepository;
    private final CollaboratorRepositoryATCC collaboratorRepository;

    public CreateFlightRouteController() {
        this(AuthzRegistry.authorizationService(), new FlightRouteBuilder(), PersistenceContext.repositories().flightRoutes(), PersistenceContext.repositories().airports(), PersistenceContext.repositories().collaboratorsATCC());
    }

    public CreateFlightRouteController(final AuthorizationService authz, final FlightRouteBuilder routeBuilder, final FlightRouteRepository frRepository, final AirportRepository airportRepository, final CollaboratorRepositoryATCC collaboratorRepository) {
        Preconditions.ensure(authz != null && routeBuilder != null && frRepository != null && airportRepository != null && collaboratorRepository != null, "One or more parameters of the Controller are null");

        this.authz = authz;
        this.routeBuilder = routeBuilder;
        this.frRepository = frRepository;
        this.airportRepository = airportRepository;
        this.collaboratorRepository = collaboratorRepository;
    }

    public List<String> getAllAirportsList() {
        return StreamSupport.stream(airportRepository.findAll().spliterator(), false)
                .map(Airport::getAirportName)
                .toList();
    }

    public void checkName(String name) {
        if (frRepository.findByName(name).isPresent()) throw new IntegrityViolationException("Route name already exists in the system.");
    }

    private String getCompanyInitials(String companyName) {
        String[] words = companyName.trim().split("\\s+");

        if (words.length >= 2) {
            return String.valueOf(words[0].charAt(0)).toUpperCase() +
                    String.valueOf(words[1].charAt(0)).toUpperCase();
        } else {
            return companyName.substring(0, 2).toUpperCase();
        }
    }

    public FlightRoute createFlightRoute(final String numbers, String startingAirport, String endingAirport) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final var user = authz.session().orElseThrow(() -> new IllegalStateException("No authenticated session")).authenticatedUser();
        final CollaboratorATCC atcc = collaboratorRepository.findBySystemUser(user).orElseThrow(() -> new IllegalStateException("No collaborator profile for authenticated user"));
        final AirTransportCompany company = atcc.getAtcc();

        String initials = getCompanyInitials(company.getName());
        String name = initials + numbers;

        checkName(name);

        final Airport start = airportRepository.findByName(startingAirport).orElseThrow(() -> new IllegalArgumentException("Starting airport not found."));
        final Airport end = airportRepository.findByName(endingAirport).orElseThrow(() -> new IllegalArgumentException("Ending airport not found."));

        final FlightRoute flightRoute = routeBuilder.with(name, start, end, company).build();

        return frRepository.save(flightRoute);
    }
}
