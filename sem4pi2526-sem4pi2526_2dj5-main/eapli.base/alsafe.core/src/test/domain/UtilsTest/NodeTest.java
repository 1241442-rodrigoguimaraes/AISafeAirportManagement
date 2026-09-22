package domain.UtilsTest;

import eapli.alsafe.utils.nodes.domain.Altitude;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.alsafe.utils.nodes.domain.Node;
import eapli.alsafe.utils.nodes.domain.NodeId;
import org.junit.Test;

public class NodeTest {
    @Test
    public void testNode() {
        Node nd = new Node(new NodeId(1), new Coordinate(10.0,20.0),new Altitude(100.0));
        assert(nd.getCoordinates().latitude() == 10.0);
        assert(nd.getCoordinates().longitude() == 20.0);
        assert(nd.getAltitude().meters() == 100.0);
        assert(nd.identity().value() == 1);
    }

    @Test
    public void testEquals() {
        Node nd1 = new Node(new NodeId(1), new Coordinate(10.0,20.0),new Altitude(100.0));
        Node nd2 = new Node(new NodeId(1), new Coordinate(10.0,20.0),new Altitude(100.0));
        assert(nd1.equals(nd2));
    }

    @Test
    public void testNotEquals() {
        Node nd1 = new Node(new NodeId(1), new Coordinate(10.0,20.0),new Altitude(100.0));
        Node nd2 = new Node(new NodeId(2), new Coordinate(15.0,25.0),new Altitude(150.0));
    }

    @Test
    public void testMoveTo() {
        Node nd = new Node(new NodeId(1), new Coordinate(10.0,20.0),new Altitude(100.0));
        nd.moveTo(new Coordinate(15.0,25.0));
        assert(nd.getCoordinates().latitude() == 15.0);
        assert(nd.getCoordinates().longitude() == 25.0);
    }

    @Test
    public void testChangeAltitude() {
        Node nd = new Node(new NodeId(1), new Coordinate(10.0,20.0),new Altitude(100.0));
        nd.changeAltitude(new Altitude(150.0));
        assert(nd.getAltitude().meters() == 150.0);
    }

    @Test
    public void testSameAs() {
        Node nd = new Node(new NodeId(1), new Coordinate(10.0,20.0),new Altitude(100.0));
        assert(nd.sameAs(nd));
    }
}

