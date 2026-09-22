package eapli.alsafe.app.remoteaccess.menus;

import eapli.framework.presentation.console.AbstractUI;

public abstract class ClientBaseUI extends AbstractUI {

    @Override
    public String headline() {

        return "Client Node [Remote Access Menu]";
    }

    @Override
    protected void drawFormTitle(final String title) {
        final var titleBorder = BORDER.substring(0, 2) + " " + title;
        System.out.println(titleBorder);
        drawFormBorder();
    }
}
