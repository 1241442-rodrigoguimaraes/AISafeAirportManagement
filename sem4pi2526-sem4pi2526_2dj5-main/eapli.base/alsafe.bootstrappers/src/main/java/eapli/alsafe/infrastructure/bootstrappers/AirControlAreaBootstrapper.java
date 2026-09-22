package eapli.alsafe.infrastructure.bootstrappers;

import eapli.alsafe.airinfrastructure.application.RegisterAirControlAreaController;
import eapli.alsafe.airinfrastructure.domain.AirControlArea;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class AirControlAreaBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(AirControlAreaBootstrapper.class);

    @Override
    public boolean execute() {
        registerArea("Portugal", 150.0,
                new double[]{36.95, -9.50},
                new double[]{42.15, -7.50}
        );

        registerArea("Spain", 200.0,
                new double[]{35.95, -7.49},
                new double[]{43.79, -1.74}
        );

        return true;
    }

    public void registerArea(final String name, final Double minimumFuel, final double[]... coords) {
        final RegisterAirControlAreaController controller = new RegisterAirControlAreaController();

        Arrays.stream(coords).forEach(c -> controller.createCoordinate(c[0], c[1]));

        final AirControlArea area = controller.registerAirControlArea(name, minimumFuel);

        LOGGER.info("Registered Air Control Area {}", area);
    }
}
