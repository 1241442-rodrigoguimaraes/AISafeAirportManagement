package eapli.alsafe.persistence.impl.inmemory;

import eapli.alsafe.utils.nodes.domain.Node;
import eapli.alsafe.utils.nodes.domain.NodeId;
import eapli.alsafe.utils.nodes.repository.NodeRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;
import java.util.stream.StreamSupport;

public class InMemoryNodeRepository extends InMemoryDomainRepository<Node, NodeId>
        implements NodeRepository {

    public InMemoryNodeRepository() {
        super();
    }

    @Override
    public Optional<Node> findLastNode() {
        return StreamSupport.stream(findAll().spliterator(), false)
                .max((n1, n2) -> n1.identity().compareTo(n2.identity()));
    }
}
