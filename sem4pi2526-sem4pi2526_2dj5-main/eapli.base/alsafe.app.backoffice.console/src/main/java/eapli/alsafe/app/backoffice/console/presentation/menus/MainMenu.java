/*
 * Copyright (c) 2013-2024 the original author or authors.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package eapli.alsafe.app.backoffice.console.presentation.menus;

import eapli.alsafe.Application;
import eapli.alsafe.app.backoffice.console.presentation.engineModels.CreateEngineModelAction;
import eapli.alsafe.app.common.console.presentation.authz.MyUserMenu;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.ExitWithMessageAction;
import eapli.framework.presentation.console.menu.HorizontalMenuRenderer;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.MenuRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

/**
 *
 * @author Paulo Gandra Sousa
 */
public class MainMenu extends AbstractUI {

	private static final int EXIT_OPTION = 0;

	private static final int MY_USER_OPTION = 1;
	private static final int USERS_OPTION = 2;
	private static final int AREAS_OPTION = 3;
	private static final int AIRPORTS_OPTION = 4;
	private static final int COMPANIES_OPTION = 5;
	private static final int COLLABORATORS_OPTION = 6;
	private static final int ENGINE_MODELS_OPTION = 7;
    private static final int AIRCRAFT_MODELS_OPTION = 8;
	private static final int WEATHER_OPTION = 2;
	private static final int SETTINGS_OPTION = 10;

	private static final String SEPARATOR_LABEL = "--------------";

	private final AuthorizationService authz = AuthzRegistry.authorizationService();

	@Override
	public boolean show() {
		drawFormTitle();
		return doShow();
	}

	/**
	 * @return true if the user selected the exit option
	 */
	@Override
	public boolean doShow() {
		final var menu = buildMainMenu();
		final MenuRenderer renderer;
		if (Application.settings().isMenuLayoutHorizontal()) {
			renderer = new HorizontalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
		} else {
			renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
		}
		return renderer.render();
	}

	@Override
	public String headline() {

		return authz.session().map(s -> "Backoffice [ @" + s.authenticatedUser().identity() + " ]")
				.orElse("Backoffice [ ==Anonymous== ]");
	}

	private Menu buildMainMenu() {
		final var mainMenu = new Menu();

		mainMenu.addSubMenu(MY_USER_OPTION, new MyUserMenu());

		if (!Application.settings().isMenuLayoutHorizontal()) {
			mainMenu.addItem(MenuItem.separator(SEPARATOR_LABEL));
		}

		if (authz.isAuthenticatedUserAuthorizedTo(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN)) {
			mainMenu.addItem(USERS_OPTION, "Users >", new UserMenuAction());
		}

		if (authz.isAuthenticatedUserAuthorizedTo(Roles.BACKOFFICE_OPERATOR)) {
			mainMenu.addItem(AREAS_OPTION, "Air Control Areas >", new AirControlAreaMenuAction());
			mainMenu.addItem(AIRPORTS_OPTION, "Airports >", new AirportMenuAction());
			mainMenu.addItem(COMPANIES_OPTION, "Air Transport Companies >", new AirCompanyMenuAction());
			mainMenu.addItem(COLLABORATORS_OPTION, "Collaborators >", new CollaboratorMenuAction());
            mainMenu.addItem(ENGINE_MODELS_OPTION, "Engine Models >", new CreateEngineModelAction());
			mainMenu.addItem(AIRCRAFT_MODELS_OPTION, "Aircraft Models >", new AircraftModelMenuAction());
		}

		if (authz.isAuthenticatedUserAuthorizedTo(Roles.WEATHER_PERSON, Roles.PILOT, Roles.FLIGHT_CONTROL_OPERATOR)) {
			mainMenu.addSubMenu(WEATHER_OPTION, new WeatherMenuAction());
		}

		if (authz.isAuthenticatedUserAuthorizedTo(Roles.ADMIN)) {
			mainMenu.addItem(SETTINGS_OPTION, "Settings >", new AdminSettingsMenuAction());

		}

		if (!Application.settings().isMenuLayoutHorizontal()) {
			mainMenu.addItem(MenuItem.separator(SEPARATOR_LABEL));
		}

		mainMenu.addItem(EXIT_OPTION, "Exit", new ExitWithMessageAction("Bye, Bye"));

		return mainMenu;
	}
}
