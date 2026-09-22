package eapli.alsafe.utils.nodes.application;

import eapli.alsafe.utils.nodes.domain.Altitude;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.alsafe.utils.nodes.domain.Node;
import eapli.alsafe.utils.nodes.domain.NodeId;
import eapli.alsafe.utils.nodes.repository.NodeRepository;

import java.util.Optional;

public class NodeService {

    private final NodeRepository nodeRepository;

    public NodeService(final NodeRepository nodeRepository) {
        if (nodeRepository == null)
            throw new IllegalArgumentException();
        this.nodeRepository = nodeRepository;
    }


    public Node createNode(final Double latitude,
                           final Double longitude, final Double altitudeMeters) {
        final Coordinate coordinates = new Coordinate(latitude, longitude);
        final Altitude altitude = new Altitude(altitudeMeters);

        final Integer nextId = nodeRepository.findLastNode()
                .map(node -> node.identity().value() + 1)
                .orElse(1);

        final Node node = new Node(new NodeId(nextId), coordinates, altitude);

        return nodeRepository.save(node);
    }

    public Node moveNode(final NodeId id, final Double newLatitude,
                         final Double newLongitude) {
        final Node node = nodeRepository.ofIdentity(id)
                .orElseThrow(() -> new IllegalArgumentException("Node não encontrado: " + id));

        node.moveTo(new Coordinate(newLatitude, newLongitude));
        return nodeRepository.save(node);
    }

    public Node changeAltitude(final NodeId id, final Double newAltitudeMeters) {
        final Node node = nodeRepository.ofIdentity(id)
                .orElseThrow(() -> new IllegalArgumentException("Node não encontrado: " + id));

        node.changeAltitude(new Altitude(newAltitudeMeters));
        return nodeRepository.save(node);
    }

    public Iterable<Node> allNodes() {
        return nodeRepository.findAll();
    }

    public Optional<Node> nodeById(final NodeId id) {
        return nodeRepository.ofIdentity(id);
    }
}