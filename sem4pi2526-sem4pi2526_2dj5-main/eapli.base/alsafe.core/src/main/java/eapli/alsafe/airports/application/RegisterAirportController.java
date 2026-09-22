package eapli.alsafe.airports.application;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaID;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.airports.domain.Airport;
import eapli.alsafe.airports.domain.AirportBuilder;
import eapli.alsafe.airports.repository.AirportRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.alsafe.utils.nodes.application.NodeService;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.alsafe.utils.nodes.domain.Node;
import eapli.alsafe.utils.nodes.repository.NodeRepository;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class RegisterAirportController {

    private final AirportRepository airportRepository;
    private final NodeService nodeService;
    private final AirControlAreaRepository airControlAreaRepository;
    private final AuthorizationService authz;

    public RegisterAirportController() {
        this.airportRepository = PersistenceContext.repositories().airports();
        final NodeRepository nodeRepository = PersistenceContext.repositories().nodes();
        this.airControlAreaRepository = PersistenceContext.repositories().areas();
        this.nodeService = new NodeService(nodeRepository);
        this.authz = AuthzRegistry.authorizationService();
    }

    public RegisterAirportController(final AirportRepository airportRepository,
                                   final AirControlAreaRepository airControlAreaRepository,
                                   final NodeService nodeService,
                                   final AuthorizationService authz) {
        this.airportRepository = airportRepository;
        this.airControlAreaRepository = airControlAreaRepository;
        this.nodeService = nodeService;
        this.authz = authz;
    }

    public Airport registerAirport(final String icaoCode, final String iataCode,
                                   final String airportName,
                                   final Double latitude, final Double longitude,
                                   final Double altitudeMeters,
                                   final AirControlAreaID airControlAreaId) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR);

        final Node location = nodeService.createNode(latitude, longitude, altitudeMeters);

        final AirControlArea airControlArea = airControlAreaRepository.findById(airControlAreaId).orElseThrow(() -> new IllegalArgumentException("Air Control Area not found"));

        final Airport airport = new AirportBuilder()
                .with(icaoCode, iataCode, airportName, location, airControlArea)
                .build();

        return airportRepository.save(airport);
    }

    public Iterable<Node> allNodes() {
        return nodeService.allNodes();
    }

    public Iterable<Airport> allAirports() {
        return airportRepository.findAll();
    }

    public Iterable<AirControlArea> allAirControlAreas() {
        return airControlAreaRepository.findAll();
    }

    public boolean icaoAlreadyExists(String icaoCode) {
        return airportRepository.existsByICAOCode(icaoCode);
    }

    public boolean iataAlreadyExists(String iataCode) {
        return airportRepository.existsByIATACode(iataCode);
    }

    public boolean coordinatesAlreadyExist(Double latitude, Double longitude) {
        return airportRepository.sameCoordinates(new Coordinate(latitude, longitude));
    }

}
