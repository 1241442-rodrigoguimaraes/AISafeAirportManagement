package eapli.alsafe.airinfrastructure.application;

import eapli.alsafe.airinfrastructure.domain.FlightRoute;
import eapli.alsafe.airinfrastructure.repositories.FlightRouteRepository;
import eapli.alsafe.collaboratormanagement.domain.CollaboratorATCC;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.companies.domain.AirTransportCompany;
import eapli.alsafe.flightPlan.repositories.FlightPlanRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.validations.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@UseCaseController
public class DeleteFlightRouteController {

    private final AuthorizationService authz;
    private final CollaboratorRepositoryATCC collaboratorRepository;
    private final FlightPlanRepository fpRepository;
    private final FlightRouteRepository frRepository;

    public DeleteFlightRouteController() {
        this(AuthzRegistry.authorizationService(), PersistenceContext.repositories().collaboratorsATCC(), PersistenceContext.repositories().flightPlan(), PersistenceContext.repositories().flightRoutes());
    }

    public DeleteFlightRouteController(final AuthorizationService authz, final CollaboratorRepositoryATCC collaboratorRepository, final FlightPlanRepository fpRepository, final FlightRouteRepository frRepository) {
        Preconditions.ensure(authz != null && collaboratorRepository != null && fpRepository != null && frRepository != null, "One or more parameters of the Controller are null");

        this.authz = authz;
        this.collaboratorRepository = collaboratorRepository;
        this.fpRepository = fpRepository;
        this.frRepository = frRepository;
    }

    public List<String> getCompanyRoutes(String date) {
        final var user = authz.session().orElseThrow(() -> new IllegalStateException("No authenticated session")).authenticatedUser();
        final CollaboratorATCC atcc = collaboratorRepository.findBySystemUser(user).orElseThrow(() -> new IllegalStateException("No collaborator profile for authenticated user"));
        final AirTransportCompany company = atcc.getAtcc();

        Iterable<FlightRoute> routes = frRepository.findByCompany(company);
        List<FlightRoute> routesList = checkFlightPlans(StreamSupport.stream(routes.spliterator(), false).toList(), date);

        return getAllRoutesNames(routesList);
    }

    public int deleteFlightRoute(String routeName) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        try {
            final FlightRoute route = frRepository.findByName(routeName).orElseThrow(() -> new IllegalArgumentException("Unknown flight route."));
            frRepository.delete(route);

            return 0;
        } catch (IntegrityViolationException e) {
            System.err.println("Error deleting flight route " + routeName + ": " + e.getMessage());
            return 1;
        }
    }

    private List<String> getAllRoutesNames(List<FlightRoute> routesList) {
        List<String> routesNames = new ArrayList<>();

        for (FlightRoute route : routesList) {
            routesNames.add(route.getFlightRouteName().getName());
        }

        return routesNames;
    }

    private List<FlightRoute> checkFlightPlans(List<FlightRoute> routesList, String date) {
        List<FlightRoute> validRoutes = new ArrayList<>();

        for (FlightRoute route : routesList) {
            if (!fpRepository.findByFlightRouteAndAfterDate(route, date).iterator().hasNext()) validRoutes.add(route);
        }

        return validRoutes;
    }
}
