package eapli.alsafe.infrastructure.bootstrappers;

import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.utils.nodes.domain.Altitude;
import eapli.alsafe.utils.nodes.domain.Coordinate;
import eapli.alsafe.utils.nodes.domain.Node;
import eapli.alsafe.utils.nodes.domain.NodeId;
import eapli.alsafe.utils.nodes.repository.NodeRepository;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeBootstrapper implements Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeBootstrapper.class);

    private final NodeRepository nodeRepository = PersistenceContext.repositories().nodes();

    @Override
    public boolean execute() {
        try {
            return true;
        } catch (Exception e) {
            LOGGER.error("Error bootstrapping nodes", e);
            return false;
        }
    }
}
