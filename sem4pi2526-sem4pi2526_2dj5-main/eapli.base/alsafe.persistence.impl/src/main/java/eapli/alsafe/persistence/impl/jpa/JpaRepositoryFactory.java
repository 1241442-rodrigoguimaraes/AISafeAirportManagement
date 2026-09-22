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
package eapli.alsafe.persistence.impl.jpa;

import eapli.alsafe.Application;
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
import eapli.alsafe.engineModelMagnement.Repositories.engineModelRepository;
import eapli.alsafe.infrastructure.persistence.RepositoryFactory;
import eapli.alsafe.pilotmanagement.repositories.PilotRepository;
import eapli.alsafe.aircraftModelMagnement.repositories.MakersRepository;
import eapli.alsafe.aircraftModelMagnement.repositories.aircraftModelRepository;
import eapli.alsafe.alsafeusermanagement.repositories.SignupRequestRepository;
import eapli.alsafe.utils.nodes.repository.NodeRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.authz.repositories.impl.jpa.JpaAutoTxUserRepository;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

/**
 *
 * Created by nuno on 21/03/16.
 */
public class JpaRepositoryFactory implements RepositoryFactory {

    @Override
    public UserRepository users(final TransactionalContext autoTx) {
        return new JpaAutoTxUserRepository(autoTx);
    }

    @Override
    public UserRepository users() {
        return new JpaAutoTxUserRepository(Application.settings().getPersistenceUnitName(),
                Application.settings().getExtendedPersistenceProperties());
    }

    @Override
    public EmailDomainRepository domains(final TransactionalContext autoTx) {
        return new JpaEmailDomainRepository(autoTx);
    }

    @Override
    public EmailDomainRepository domains() {
        return new JpaEmailDomainRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public AlSafeUserRepository alSafeUsers(final TransactionalContext autoTx) {
        return new JpaAlSafeUserRepository(autoTx);
    }

    @Override
    public AlSafeUserRepository alSafeUsers() {
        return new JpaAlSafeUserRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public SignupRequestRepository signupRequests(final TransactionalContext autoTx) {
        return new JpaSignupRequestRepository(autoTx);
    }

    @Override
    public SignupRequestRepository signupRequests() {
        return new JpaSignupRequestRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public TransactionalContext newTransactionalContext() {
        return JpaAutoTxRepository.buildTransactionalContext(Application.settings().getPersistenceUnitName(),
                Application.settings().getExtendedPersistenceProperties());
    }

    @Override
    public NodeRepository nodes(final TransactionalContext autoTx) {
        return new JpaNodeRepository(autoTx);
    }

    @Override
    public NodeRepository nodes() {
        return new JpaNodeRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public AirportRepository airports() {
        return new JpaAirportRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public AirportRepository airports(final TransactionalContext autoTx) {
        return new JpaAirportRepository(autoTx);
    }

    @Override
    public MakersRepository makers() {
        return new JpaMakerRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public MakersRepository makers(TransactionalContext autoTx) {
        return new JpaMakerRepository(autoTx);
    }

    @Override
    public AirControlAreaRepository areas() {
        return new JpaAirControlAreaRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public AirControlAreaRepository areas(final TransactionalContext autoTx) {
        return new JpaAirControlAreaRepository(autoTx);
    }

    @Override
    public AirCompanyRepository companies() {
        return new JpaAirCompanyRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public AirCompanyRepository companies(final TransactionalContext autoTx) {
        return new JpaAirCompanyRepository(autoTx);
    }

    @Override
    public CollaboratorRepositoryFCO collaboratorsFCO() {
        return new JpaCollaboratorRepositoryFCO(Application.settings().getPersistenceUnitName());
    }

    @Override
    public CollaboratorRepositoryFCO collaboratorsFCO(TransactionalContext autoTx) {
        return new JpaCollaboratorRepositoryFCO(autoTx);
    }

    @Override
    public CollaboratorRepositoryATCC collaboratorsATCC() {
        return new JpaCollaboratorRepositoryATCC(Application.settings().getPersistenceUnitName());
    }

    @Override
    public CollaboratorRepositoryATCC collaboratorsATCC(TransactionalContext autoTx) {
        return new JpaCollaboratorRepositoryATCC(autoTx);
    }

    @Override
    public engineModelRepository engineModels(final TransactionalContext autoTx) {
        return new JpaEngineModelRepository(autoTx);
    }

    @Override
    public engineModelRepository engineModels() {
        return new JpaEngineModelRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public aircraftModelRepository aircraftModels(final TransactionalContext autoTx) {
        return new JpaAircraftModelRepository(autoTx);
    }

    @Override
    public aircraftModelRepository aircraftModels() {
        return new JpaAircraftModelRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public eapli.alsafe.aircraft.repositories.AircraftRepository aircraft(final TransactionalContext autoTx) {
        return new JpaAircraftRepository(autoTx);
    }

    @Override
    public eapli.alsafe.aircraft.repositories.AircraftRepository aircraft() {
        return new JpaAircraftRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public WeatherDataRepository weatherData() {
        return new JpaWeatherDataRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public WeatherDataRepository weatherData(final TransactionalContext autoTx) {
        return new JpaWeatherDataRepository(autoTx);
    }

    @Override
    public ImportedFlightPlanRepository importedFlightPlans() {
        return new JpaImportedFlightPlanRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public ImportedFlightPlanRepository importedFlightPlans(final TransactionalContext autoTx) {
        return new JpaImportedFlightPlanRepository(autoTx);
    }

    @Override
    public FlightRouteRepository flightRoutes() {
        return new JpaFlightRouteRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public FlightRouteRepository flightRoutes(final TransactionalContext autoTx) {
        return new JpaFlightRouteRepository(autoTx);
    }

    @Override
    public PilotRepository pilots() {
        return new JpaPilotRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public PilotRepository pilots(final TransactionalContext autoTx) {
        return new JpaPilotRepository(autoTx);
    }

    @Override
    public FlightPlanRepository flightPlan() {
        return new JpaFlightPlanRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public FlightPlanRepository flightPlan(TransactionalContext autoTx) {
        return new JpaFlightPlanRepository(autoTx);
    }

}
