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
package eapli.alsafe.usermanagement.application;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import eapli.alsafe.alsafeusermanagement.domain.AlSafeUser;
import eapli.alsafe.alsafeusermanagement.repositories.AlSafeUserRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.usermanagement.application.dto.UserDTO;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;

/**
 * Lists AlSafe backoffice users (US033).
 */
@UseCaseController
public class ListUsersController {

    private final AuthorizationService authz;
    private final AlSafeUserRepository alSafeUserRepository;
    private final UserManagementService userSvc;

    public ListUsersController() {
        this(AuthzRegistry.authorizationService(), PersistenceContext.repositories().alSafeUsers(),
                AuthzRegistry.userService());
    }

    public ListUsersController(final AuthorizationService authz, final AlSafeUserRepository alSafeUserRepository,
                               final UserManagementService userSvc) {
        if (authz == null || alSafeUserRepository == null || userSvc == null) {
            throw new IllegalArgumentException();
        }
        this.authz = authz;
        this.alSafeUserRepository = alSafeUserRepository;
        this.userSvc = userSvc;
    }

    /**
     * Returns all AlSafe users as DTOs for the backoffice list view (US033).
     */
    public List<UserDTO> getUsers() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);
        final List<UserDTO> result = new ArrayList<>();
        for (final AlSafeUser u : alSafeUserRepository.findAll()) {
            result.add(toDto(u));
        }
        return result;
    }

    public Iterable<SystemUser> allUsers() {
        authz.ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN);
        return userSvc.allUsers();
    }

    public Optional<SystemUser> find(final Username u) {
        return userSvc.userOfIdentity(u);
    }

    private static UserDTO toDto(final AlSafeUser u) {
        final SystemUser su = u.user();
        final String roleLabel = firstRoleLabel(su);
        final String status = su.isActive() ? "Active" : "Disabled";
        final LocalDate clearance = toLocalDate(u.getSecurityClearance().getSecurityClearance());
        final LocalDate skills = toLocalDate(u.getSkillsAssessment().getSkillsAssessment());
        return new UserDTO(
                su.email().toString(),
                su.name().toString(),
                u.getPhoneNumber(),
                roleLabel,
                status,
                clearance,
                skills);
    }

    private static String firstRoleLabel(final SystemUser su) {
        for (final Role r : su.roleTypes()) {
            return r.toString();
        }
        return "";
    }

    private static LocalDate toLocalDate(final Calendar c) {
        return LocalDate.ofInstant(c.toInstant(), ZoneId.systemDefault());
    }
}
