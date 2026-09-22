package eapli.alsafe.persistence.impl.jpa;

import eapli.alsafe.Application;
import eapli.alsafe.utils.nodes.domain.Node;
import eapli.alsafe.utils.nodes.domain.NodeId;
import eapli.alsafe.utils.nodes.repository.NodeRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public class JpaNodeRepository extends JpaAutoTxRepository<Node, NodeId, NodeId>
        implements NodeRepository {

    public JpaNodeRepository(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    public JpaNodeRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "id");
    }

    @Override
    public Optional<Node> findLastNode() {
        final TypedQuery<Node> query = createQuery(
                "SELECT n FROM Node n ORDER BY n.id.id DESC", Node.class);
        query.setMaxResults(1);
        return query.getResultList().stream().findFirst();
    }
}
