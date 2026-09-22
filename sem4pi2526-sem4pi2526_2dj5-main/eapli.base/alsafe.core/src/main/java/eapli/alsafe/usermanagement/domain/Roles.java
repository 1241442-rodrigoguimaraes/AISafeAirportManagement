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
package eapli.alsafe.usermanagement.domain;

import eapli.framework.infrastructure.authz.domain.model.Role;

/**
 * 
 * @author Paulo Gandra Sousa
 *
 */
public final class Roles {
	/**
	 * Administrator
	 */
	public static final Role ADMIN = Role.valueOf("ADMIN");

	/**
	 * Backoffice Operator
	 */
	public static final Role BACKOFFICE_OPERATOR = Role.valueOf("BACKOFFICE_OPERATOR");

	/**
	 * Pilot
	 */
	public static final Role PILOT = Role.valueOf("PILOT");

	/**
	 * Flight Control Operator
	 */
	public static final Role FLIGHT_CONTROL_OPERATOR = Role.valueOf("FLIGHT_CONTROL_OPERATOR");

	/**
	 * Weather Person
	 */
	public static final Role WEATHER_PERSON = Role.valueOf("WEATHER_PERSON");

	/**
	 * Air Transport Company Collaborator
	 */
	public static final Role AIR_TRANSPORT_COMPANY_COLLABORATOR = Role.valueOf("AIR_TRANSPORT_COMPANY_COLLABORATOR");

	/**
	 * Get available role types for adding new users
	 *
     */
	public static Role[] nonUserValues() {
		return new Role[] { ADMIN, BACKOFFICE_OPERATOR, PILOT, FLIGHT_CONTROL_OPERATOR, WEATHER_PERSON, AIR_TRANSPORT_COMPANY_COLLABORATOR };
	}

    public static Role[] availableRolesToAddUser() {
        return new Role[] { ADMIN, BACKOFFICE_OPERATOR, WEATHER_PERSON };
    }

	public boolean isCollaborator(final Role role) {
		return role == ADMIN || role != BACKOFFICE_OPERATOR || role != PILOT || role != FLIGHT_CONTROL_OPERATOR
				|| role != WEATHER_PERSON || role != AIR_TRANSPORT_COMPANY_COLLABORATOR;
	}
}
