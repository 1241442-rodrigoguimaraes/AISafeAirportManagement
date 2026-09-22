package eapli.alsafe.app.backoffice.console.presentation.menus;

import eapli.alsafe.app.backoffice.console.presentation.collaboratormanagement.AddCollaboratorAction;
import eapli.alsafe.app.backoffice.console.presentation.collaboratormanagement.DisableCollaboratorAction;
import eapli.alsafe.app.backoffice.console.presentation.collaboratormanagement.EditCollaboratorAction;
import eapli.alsafe.app.backoffice.console.presentation.collaboratormanagement.ListCollaboratorsAction;
import eapli.framework.actions.Action;
import eapli.framework.actions.Actions;
import eapli.framework.actions.menu.Menu;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class CollaboratorMenuAction implements Action {
    private static final int ADD_COLLABORATOR_OPTION = 1;
    private static final int LIST_COLLABORATORS_OPTION = 2;
    private static final int EDIT_COLLABORATOR_OPTION = 3;
    private static final int DISABLE_COLLABORATOR_OPTION = 4;
    private static final int EXIT_OPTION = 0;

    @Override
    public boolean execute() {
        final var menu = new Menu("Collaborators >");

        menu.addItem(ADD_COLLABORATOR_OPTION, "Add Collaborator", new AddCollaboratorAction());
        menu.addItem(LIST_COLLABORATORS_OPTION, "List Customer's Collaborators", new ListCollaboratorsAction());
        menu.addItem(EDIT_COLLABORATOR_OPTION, "Edit Collaborator", new EditCollaboratorAction());
        menu.addItem(DISABLE_COLLABORATOR_OPTION, "Disable Customer's Collaborator", new DisableCollaboratorAction());
        menu.addItem(EXIT_OPTION, "Return", Actions.FAIL);

        final var renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
        return renderer.render();
    }
}
