package eapli.alsafe.app.backoffice.console.presentation.nodes;

import eapli.alsafe.utils.nodes.domain.Node;
import eapli.framework.visitor.Visitor;

public class NodePrinter implements Visitor<Node> {

    @Override
    public void visit(final Node visitee) {
        System.out.printf("%-10s %-20s %-10s", visitee.identity(), visitee.getCoordinates(), visitee.getAltitude());
    }
}
