/*
 * Copyright (c) 2013-2024 the original author or authors.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package eapli.alsafe.persistence.impl.inmemory;

import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.airinfrastructure.repositories.FlightRouteRepository;
import eapli.alsafe.airports.repository.AirportRepository;
import eapli.alsafe.antlr.flightplan.repositories.ImportedFlightPlanRepository;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryFCO;
import eapli.alsafe.flightPlan.repositories.FlightPlanRepository;
import eapli.alsafe.weather.repositories.WeatherDataRepository;
import eapli.alsafe.alsafeusermanagement.repositories.AlSafeUserRepository;
import eapli.alsafe.alsafeusermanagement.repositories.EmailDomainRepository;
import eapli.alsafe.companies.repositories.AirCompanyRepository;
import eapli.alsafe.infrastructure.persistence.RepositoryFactory;
import eapli.alsafe.pilotmanagement.repositories.PilotRepository;
import eapli.alsafe.aircraftModelMagnement.repositories.MakersRepository;
import eapli.alsafe.engineModelMagnement.Repositories.engineModelRepository;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.alsafe.usermanagement.domain.UserBuilderHelper;
import eapli.alsafe.alsafeusermanagement.repositories.SignupRequestRepository;
import eapli.alsafe.utils.nodes.repository.NodeRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.authz.repositories.impl.inmemory.InMemoryUserRepository;

/**
 *
 * Created by nuno on 20/03/16.
 */
public class InMemoryRepositoryFactory implements RepositoryFactory {

	@Override
	public UserRepository users(final TransactionalContext tx) {
		final var repo = new InMemoryUserRepository();
		// ensure we have at least a power user to be able to use the application
		final var userBuilder = UserBuilderHelper.builder();
		userBuilder.withUsername("admin").withPassword("Password1").withName("joe", "power")
				.withEmail("joe@email.org").withRoles(Roles.ADMIN);
		final var newUser = userBuilder.build();
		repo.save(newUser);
		return repo;
	}

	@Override
	public UserRepository users() {
		return users(null);
	}

    @Override
    public EmailDomainRepository domains(final TransactionalContext tx) {
        return new InMemoryEmailDomainRepository();
    }

    @Override
    public EmailDomainRepository domains() {
        return domains(null);
    }

	@Override
    public AlSafeUserRepository alSafeUsers(final TransactionalContext tx) {
		return new InMemoryAlSafeUserRepository();
	}

	@Override
    public AlSafeUserRepository alSafeUsers() {
		return alSafeUsers(null);
	}

	@Override
	public SignupRequestRepository signupRequests() {
		return signupRequests(null);
	}

    @Override
    public NodeRepository nodes() {
        return new InMemoryNodeRepository();
    }

    @Override
    public NodeRepository nodes(final TransactionalContext tx) {
        return new InMemoryNodeRepository();
    }

    @Override
    public AirportRepository airports() {
        return new InMemoryAirportRepository();
    }

    @Override
    public AirportRepository airports(final TransactionalContext tx) {
        return new InMemoryAirportRepository();
    }

    @Override
    public SignupRequestRepository signupRequests(final TransactionalContext tx) {
        return new InMemorySignupRequestRepository();
    }

    @Override
    public MakersRepository makers() {
        return makers(null);
    }

    @Override
    public MakersRepository makers(TransactionalContext autoTx) {
        return new InMemoryMakersRepository();
    }

    @Override
    public AirControlAreaRepository areas() {
        return areas(null);
    }

    @Override
    public AirControlAreaRepository areas(TransactionalContext autoTx) {
        return new InMemoryAirControlAreaRepository();
    }

    @Override
    public AirCompanyRepository companies() {
        return companies(null);
    }

    @Override
    public AirCompanyRepository companies(TransactionalContext autoTx) {
        return new InMemoryAirCompanyRepository();
    }

    @Override
    public CollaboratorRepositoryFCO collaboratorsFCO() {
        return collaboratorsFCO(null);
    }

    @Override
    public CollaboratorRepositoryFCO collaboratorsFCO(TransactionalContext autoTx) {
        return new InMemoryCollaboratorRepositoryFCO();
    }

    @Override
    public CollaboratorRepositoryATCC collaboratorsATCC() {
        return collaboratorsATCC(null);
    }

    @Override
    public CollaboratorRepositoryATCC collaboratorsATCC(TransactionalContext autoTx) {
        return new InMemoryCollaboratorRepositoryATCC();
    }

    @Override
    public TransactionalContext newTransactionalContext() {
		// in memory does not support transactions...
		return null;
	}

    @Override
    public engineModelRepository engineModels() {
        return engineModels(null);
    }

    @Override
    public engineModelRepository engineModels(TransactionalContext autoTx) {
        return new InMemoryEngineModelRepository();
    }

    @Override
    public aircraftModelRepository aircraftModels() {
        return aircraftModels(null);
    }

    @Override
    public aircraftModelRepository aircraftModels(TransactionalContext autoTx) {
        return new InMemoryAircraftModelRepository();
    }

    @Override
    public eapli.alsafe.aircraft.repositories.AircraftRepository aircraft() {
        return aircraft(null);
    }

    @Override
    public eapli.alsafe.aircraft.repositories.AircraftRepository aircraft(TransactionalContext autoTx) {
        return new InMemoryAircraftRepository();
    }

    @Override
    public WeatherDataRepository weatherData() {
        return weatherData(null);
    }

    @Override
    public WeatherDataRepository weatherData(TransactionalContext autoTx) {
        return new InMemoryWeatherDataRepository();
    }

    @Override
    public ImportedFlightPlanRepository importedFlightPlans() {
        return importedFlightPlans(null);
    }

    @Override
    public ImportedFlightPlanRepository importedFlightPlans(final TransactionalContext autoTx) {
        return new InMemoryImportedFlightPlanRepository();
    }

    @Override
    public FlightRouteRepository flightRoutes() {
        return flightRoutes(null);
    }

    @Override
    public FlightRouteRepository flightRoutes(final TransactionalContext autoTx) {
        return new InMemoryFlightRouteRepository();
    }

    @Override
    public PilotRepository pilots() {
        return pilots(null);
    }

    @Override
    public PilotRepository pilots(final TransactionalContext autoTx) {
        return new InMemoryPilotRepository();
    }

    @Override
    public FlightPlanRepository flightPlan() {
        return flightPlan(null);
    }

    @Override
    public FlightPlanRepository flightPlan(final TransactionalContext autoTx) {
        return new InMemoryFlightPlanRepository();
    }
}
