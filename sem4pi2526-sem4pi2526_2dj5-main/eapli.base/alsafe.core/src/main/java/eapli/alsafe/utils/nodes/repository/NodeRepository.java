package eapli.alsafe.utils.nodes.repository;

import eapli.alsafe.utils.nodes.domain.Node;
import eapli.alsafe.utils.nodes.domain.NodeId;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface NodeRepository extends DomainRepository<NodeId, Node> {
    Optional<Node> findLastNode();
}
