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
package eapli.alsafe.infrastructure.persistence;

import eapli.alsafe.airinfrastructure.repositories.AirControlAreaRepository;
import eapli.alsafe.airinfrastructure.repositories.FlightRouteRepository;
import eapli.alsafe.airports.repository.AirportRepository;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryATCC;
import eapli.alsafe.collaboratormanagement.repositories.CollaboratorRepositoryFCO;
import eapli.alsafe.flightPlan.repositories.FlightPlanRepository;
import eapli.alsafe.weather.repositories.WeatherDataRepository;
import eapli.alsafe.alsafeusermanagement.repositories.AlSafeUserRepository;
import eapli.alsafe.alsafeusermanagement.repositories.EmailDomainRepository;
import eapli.alsafe.companies.repositories.AirCompanyRepository;
import eapli.alsafe.aircraftModelMagnement.repositories.MakersRepository;
import eapli.alsafe.aircraft.repositories.AircraftRepository;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
import eapli.alsafe.antlr.flightplan.repositories.ImportedFlightPlanRepository;
import eapli.alsafe.pilotmanagement.repositories.PilotRepository;
import eapli.alsafe.alsafeusermanagement.repositories.SignupRequestRepository;
import eapli.alsafe.engineModelMagnement.Repositories.engineModelRepository;
import eapli.alsafe.utils.nodes.repository.NodeRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;


/**
 * @author Paulo Gandra Sousa
 *
 */
public interface RepositoryFactory {

    /**
     * factory method to create a transactional context to use in the repositories
     *
     * @return
     */
    TransactionalContext newTransactionalContext();

    /**
     *
     * @param autoTx
     *            the transactional context to enrol
     * @return
     */
    UserRepository users(TransactionalContext autoTx);

    /**
     * repository will be created in auto transaction mode
     *
     * @return
     */
    UserRepository users();

    EmailDomainRepository domains(TransactionalContext autoTx);

    EmailDomainRepository domains();

    /**
     *
     * @param autoTx the transactional context to enroll
     *
     * @return the alsafe users repository
     */
    AlSafeUserRepository alSafeUsers(TransactionalContext autoTx);

    /**
     * repository will be created in auto transaction mode
     *
     * @return the alsafe users repository
     */
    AlSafeUserRepository alSafeUsers();

    /**
     *
     * @param autoTx
     *            the transactional context to enroll
     * @return
     */
    SignupRequestRepository signupRequests(TransactionalContext autoTx);

    /**
     * repository will be created in auto transaction mode
     *
     * @return
     */
    SignupRequestRepository signupRequests();

    AirportRepository airports();

    AirportRepository airports(TransactionalContext tx);

    NodeRepository nodes();

    NodeRepository nodes(TransactionalContext autoTx);

    MakersRepository makers();

    MakersRepository makers(TransactionalContext autoTx);

    AirControlAreaRepository areas();

    AirControlAreaRepository areas(TransactionalContext autoTx);

    AirCompanyRepository companies();

    AirCompanyRepository companies(TransactionalContext autoTx);

    engineModelRepository engineModels();

    engineModelRepository engineModels(TransactionalContext autoTx);

    CollaboratorRepositoryFCO collaboratorsFCO();

    CollaboratorRepositoryFCO collaboratorsFCO(TransactionalContext autoTx);

    CollaboratorRepositoryATCC collaboratorsATCC();

    CollaboratorRepositoryATCC collaboratorsATCC(TransactionalContext autoTx);

    aircraftModelRepository aircraftModels();

    aircraftModelRepository aircraftModels(TransactionalContext autoTx);

    AircraftRepository aircraft();

    AircraftRepository aircraft(TransactionalContext autoTx);

    WeatherDataRepository weatherData();

    WeatherDataRepository weatherData(TransactionalContext autoTx);

    ImportedFlightPlanRepository importedFlightPlans();

    ImportedFlightPlanRepository importedFlightPlans(TransactionalContext autoTx);

    FlightRouteRepository flightRoutes();

    FlightRouteRepository flightRoutes(TransactionalContext autoTx);

    PilotRepository pilots();

    PilotRepository pilots(TransactionalContext autoTx);

    FlightPlanRepository flightPlan();

    FlightPlanRepository flightPlan(TransactionalContext autoTx);
}
