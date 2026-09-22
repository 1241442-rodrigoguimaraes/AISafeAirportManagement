package eapli.alsafe.airinfrastructure.application;

import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.alsafe.airinfrastructure.domain.AirControlAreaBuilder;
import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class RegisterAirControlAreaController {

    private final AuthorizationService authz;
    private final AirControlAreaBuilder acaBuilder;
    private final AirControlAreaRepository acaRepository;

    public RegisterAirControlAreaController() {
        this(AuthzRegistry.authorizationService(), new AirControlAreaBuilder(), PersistenceContext.repositories().areas());
    }

    public RegisterAirControlAreaController(final AuthorizationService authz, final AirControlAreaBuilder acaBuilder, final AirControlAreaRepository acaRepository) {
        if (authz == null || acaBuilder == null || acaRepository == null) throw new IllegalArgumentException();

        this.authz = authz;
        this.acaBuilder = acaBuilder;
        this.acaRepository = acaRepository;
    }

    public void createCoordinate(final Double latitude, final Double longitude) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR);

        acaBuilder.createCoordinate(latitude, longitude);
    }

    public AirControlArea registerAirControlArea(final String name, final Double minimumFuel) {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR);

        final long nextSeq = acaRepository.count() + 1;
        final AirControlArea aca = acaBuilder.with(nextSeq, name, minimumFuel).build();

        for (AirControlArea existing : acaRepository.findAll()) {
            if (aca.overlaps(existing)) {
                throw new IllegalArgumentException("The new Air Control Area overlaps with an existing one: " + existing.getName());
            }
        }

        return acaRepository.save(aca);
    }
}
