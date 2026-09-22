package eapli.alsafe.airinfrastructure.domain;

import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.framework.domain.model.DomainFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class AirControlAreaBuilder implements DomainFactory<AirControlArea> {

    private static final Logger LOGGER = LogManager.getLogger(AirControlAreaBuilder.class);

    private AirControlAreaID id;
    private String name;
    private List<Coordinate> coordinates;
    private AirControlAreaBoundaries boundaries;
    private AirControlAreaMinimumFuel minimumFuel;

    public AirControlAreaBuilder() {
        this.coordinates = new ArrayList<>();
    }

    public AirControlAreaBuilder with(final Long sequence, final String name, final List<Coordinate> boundaries, final Double minimumFuel) {
        withID(sequence);
        withName(name);
        withBoundaries(boundaries);
        withMinimumFuel(minimumFuel);

        return this;
    }

    public AirControlAreaBuilder with(final Long sequence, final String name, final Double minimumFuel) {
        withID(sequence);
        withName(name);
        withBoundaries(coordinates);
        withMinimumFuel(minimumFuel);

        return this;
    }

    public AirControlAreaBuilder withID(final Long sequence) {
        this.id = new AirControlAreaID(sequence);
        return this;
    }

    public AirControlAreaBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public AirControlAreaBuilder withBoundaries(List<Coordinate> coordinates) {
        this.boundaries = new AirControlAreaBoundaries(buildRectangle(coordinates));
        return this;
    }

    public AirControlAreaBuilder withBoundaries(AirControlAreaBoundaries boundaries) {
        this.boundaries = boundaries;
        return this;
    }

    public AirControlAreaBuilder withMinimumFuel(AirControlAreaMinimumFuel minimumFuel) {
        this.minimumFuel = minimumFuel;
        return this;
    }

    public AirControlAreaBuilder withMinimumFuel(Double minimumFuel) {
        this.minimumFuel = new AirControlAreaMinimumFuel(minimumFuel);
        return this;
    }

    public void createCoordinate(Double latitude, Double longitude) {
        coordinates.add(new Coordinate(latitude, longitude));
    }

    private List<Coordinate> buildRectangle(List<Coordinate> points) {
        double minLat = points.stream().mapToDouble(Coordinate::latitude).min().orElseThrow();
        double maxLat = points.stream().mapToDouble(Coordinate::latitude).max().orElseThrow();
        double minLon = points.stream().mapToDouble(Coordinate::longitude).min().orElseThrow();
        double maxLon = points.stream().mapToDouble(Coordinate::longitude).max().orElseThrow();

        return List.of(new Coordinate(maxLat, minLon), new Coordinate(maxLat, maxLon), new Coordinate(minLat, maxLon), new Coordinate(minLat, minLon));
    }

    @Override
    public AirControlArea build() {
        final var area =  new AirControlArea(id, name, boundaries, minimumFuel);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Building AirControlArea [{}], with ID [{}], name [{}], boundaries [{}] and minimum fuel [{}]", area, id, name, boundaries, minimumFuel);
        }
        return area;
    }
}
