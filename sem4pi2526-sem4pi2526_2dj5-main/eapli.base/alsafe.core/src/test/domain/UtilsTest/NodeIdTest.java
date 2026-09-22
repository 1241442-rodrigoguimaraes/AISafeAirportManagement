package domain.UtilsTest;

import eapli.alsafe.utils.nodes.domain.NodeId;
import org.junit.Test;

public class NodeIdTest {
    @Test
    public void testNodeId() {
        NodeId nodeId = new NodeId(5);
        assert(nodeId.value() == 5);
    }

    @Test
    public void testEquals() {
        NodeId nodeId1 = new NodeId(5);
        NodeId nodeId2 = new NodeId(5);
        assert(nodeId1.equals(nodeId2));
    }

    @Test
    public void testNotEquals() {
        NodeId nodeId1 = new NodeId(5);
        NodeId nodeId2 = new NodeId(6);
        assert(!nodeId1.equals(nodeId2));
    }

    @Test
    public void testToString() {
        NodeId nodeId = new NodeId(5);
        assert(nodeId.toString().equals("5"));
    }

    @Test
    public void testCompareTo() {
        NodeId nodeId1 = new NodeId(5);
        NodeId nodeId2 = new NodeId(6);
        assert(nodeId1.compareTo(nodeId2) < 0);
        assert(nodeId2.compareTo(nodeId1) > 0);
        assert(nodeId1.compareTo(new NodeId(5)) == 0);
    }
}
