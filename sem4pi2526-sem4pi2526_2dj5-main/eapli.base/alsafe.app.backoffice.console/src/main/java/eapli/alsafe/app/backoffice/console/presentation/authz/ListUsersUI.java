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
package eapli.alsafe.app.backoffice.console.presentation.authz;

import eapli.alsafe.usermanagement.application.ListUsersController;
import eapli.alsafe.usermanagement.application.dto.UserDTO;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.presentation.console.AbstractUI;

import java.util.List;

/**
 * US033 — list backoffice users as a formatted table.
 */
@SuppressWarnings({ "squid:S106" })
public class ListUsersUI extends AbstractUI {

    private final ListUsersController theController = new ListUsersController();

    @Override
    public String headline() {
        return "List Users";
    }

    @Override
    protected boolean doShow() {
        try {
            AuthzRegistry.authorizationService().ensureAuthenticatedUserHasAnyOf(
                    Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);
        } catch (final RuntimeException ex) {
            System.out.println("You are not authorized to list users.");
            return false;
        }

        final List<UserDTO> users = theController.getUsers();
        if (users.isEmpty()) {
            System.out.println("No users registered in the system.");
            return false;
        }

        System.out.printf("%-28s | %-28s | %-14s | %-28s | %-10s | %-24s | %-24s%n",
                "Email", "Name", "Phone", "Role", "Status", "Security Clearance Exp.", "Last Skills Assessment");
        System.out.println("-".repeat(160));
        for (final UserDTO u : users) {
            System.out.printf("%-28s | %-28s | %-14s | %-28s | %-10s | %-24s | %-24s%n",
                    truncate(u.email(), 28),
                    truncate(u.name(), 22),
                    truncate(u.phoneNumber(), 14),
                    truncate(u.role(), 28),
                    truncate(u.status(), 10),
                    String.valueOf(u.securityClearanceExpiration()),
                    String.valueOf(u.lastSkillsAssessmentDate()));
        }
        return false;
    }

    private static String truncate(final String s, final int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
