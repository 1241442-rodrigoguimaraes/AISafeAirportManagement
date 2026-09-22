# US082 – Insert Weather Data in a Flight

## 1. Requirements Engineering

### 1.1 User Story Description

> As a **Pilot**, I want to add to a flight plan of mine the weather data. If the flight plan has been previously tested, the test is deemed void because of the new weather data.

---

### 1.2 Acceptance Criteria

* **AC1:** Only authenticated users with the **Pilot** role may insert weather data into a flight plan.
* **AC2:** A pilot may only add weather data to **their own** flight plans.
* **AC3:** The pilot selects an existing flight plan from their list of flight plans.
* **AC4:** The pilot selects weather data already registered in the system.
* **AC5:** The selected weather data is associated with the flight plan.
* **AC6:** If the flight plan had been previously **tested**, the test result is **voided** (`tested = false`) after inserting new weather data.
* **AC7:** The pilot is warned before proceeding when the selected flight plan has already been tested.

---

### 1.3 Found Out Dependencies

| Dependency | Description |
|------------|-------------|
| **US030** | Authentication and authorization must be in place for the `PILOT` role. |
| **US041** | Weather data must be registered in the system before it can be selected. |
| **US080** | A flight plan must exist; `weatherData` is nullable at creation and filled by US082. |
| **US085** | Flight plan testing sets the `tested` flag via `FlightPlan.markAsTested()`; US082 clears it. |
| **US086** | Remote TCP exposure of this use case is planned but **not yet implemented**. |

---

### 1.4 Input and Output Data

**Input:**
- Selection of one of the pilot's flight plans (`SelectWidget`).
- Confirmation (`y/n`) when the selected plan was previously tested.
- Selection of weather data from the system catalogue.

**Output:**
- `WeatherData` associated with the `FlightPlan` and persisted through `FlightPlanRepository.save()`.
- Warning in the UI before confirmation when the plan was tested.
- Additional warning printed by the controller after save when the test result was voided.
- Error message on validation, ownership or authorisation failure.

---

### 1.5 System Sequence Diagram (SSD)

> See `SD_US082.puml` in this folder.

---

### 1.6 Other Relevant Remarks

* Weather data is optional at flight plan creation (US080) and added later through this use case.
* Voiding a test only resets the **`tested`** flag; **`FlightPlanStatus`** (`DRAFT`, `SUBMITTED`, `VALIDATED`, etc.) is **not** changed.
* The use case is available in the collaborators console: **Create Flight Plan** → **Insert Weather Data in Flight Plan** (option 6).

---

## 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

* **`FlightPlan`** — aggregate root with nullable `WeatherData`, boolean `tested`, and `insertWeatherData()`.
* **`WeatherData`** — weather conditions for an air control area on a given date (US041).
* **`Pilot`** — linked to `SystemUser`; ownership checked through `flightPlan.pilot().user()`.
* **`FlightPlanRepository`** — retrieves pilot plans and persists updates.
* **`WeatherDataRepository`** — lists available weather data.

---

## 3. Design

### 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|----------------|----------|--------|---------------|
| 1 | Who initiates the use case? | `Pilot` via `InsertWeatherDataInFlightUI` | MVC; UI handles selection and confirmation only. |
| 2 | How are the pilot's flight plans retrieved? | `FlightPlanRepository.findBySystemUser(authenticatedUser)` | Query `e.pilot.systemUser = :user` returns only the authenticated pilot's plans (AC2/AC3). |
| 3 | Where is weather data associated? | `FlightPlan.insertWeatherData(weatherData)` | Domain method validates non-null input and voids previous test (AC5/AC6). |
| 4 | How is ownership enforced? | `ensureFlightPlanBelongsToAuthenticatedPilot()` compares `flightPlan.pilot().user()` with session user | Extra guard beyond list filtering (AC2). |
| 5 | How is authorisation enforced? | `AuthorizationService.ensureAuthenticatedUserHasAnyOf(Roles.PILOT)` on every controller method | Consistent with other pilot use cases (AC1). |
| 6 | How is the pilot warned? | UI message + `y/n` confirmation before weather selection; controller prints warning after save if plan was tested | Satisfies AC7 and confirms voiding (AC6). |

---

### 3.2 Sequence Diagram

> See `SD_US082.puml` in this folder.

---

### 3.3 Applied Design Patterns

* **MVC** — `InsertWeatherDataInFlightUI` → `InsertWeatherDataInFlightController` → domain/repositories.
* **Repository** — `FlightPlanRepository`, `WeatherDataRepository`.
* **Aggregate Root** — `FlightPlan.insertWeatherData()` enforces invariants and voids the test flag.
* **Constructor injection** — testable controller with explicit dependencies.

---

### 3.4 Tests

| Test ID | Test Description | Expected Result | Automated test |
|---------|------------------|-----------------|---------------|
| T1 | Insert weather data into an untested flight plan | Weather associated; `tested` remains false | `FlightPlanTest.ensureWeatherDataCanBeInsertedIntoUntestedFlightPlan` |
| T2 | Insert weather data into a previously tested plan | Weather associated; `tested` set to false | `FlightPlanTest.ensureInsertingWeatherDataVoidsPreviousTestResult`, `InsertWeatherDataInFlightControllerTest.ensureInsertingWeatherDataVoidsPreviousTestResult` |
| T3 | Insert null weather data | `IllegalArgumentException` | `FlightPlanTest.ensureNullWeatherDataIsRejected` |
| T4 | Pilot inserts into another pilot's flight plan | `IllegalArgumentException`; no save | `InsertWeatherDataInFlightControllerTest.ensurePilotCannotInsertWeatherDataIntoAnotherPilotsFlightPlan` |
| T5 | Unauthorised user | `SecurityException` | `InsertWeatherDataInFlightControllerTest.ensureUnauthorizedUserCannotInsertWeatherData` |
| T6 | List own flight plans | Returns plans from `findBySystemUser` | `InsertWeatherDataInFlightControllerTest.ensurePilotCanListOwnFlightPlans` |
| T7 | List available weather data | Returns all weather data from repository | `InsertWeatherDataInFlightControllerTest.ensurePilotCanListAvailableWeatherData` |
| T8 | Missing authenticated session | `IllegalStateException` | `InsertWeatherDataInFlightControllerTest.ensureMissingSessionIsRejectedWhenListingFlightPlans` |

Run:

```bash
mvn -pl alsafe.core test -Dtest=InsertWeatherDataInFlightControllerTest,FlightPlanTest
```

---

## 4. Implementation

Key implementation notes:

* **Menu:** `run-collaborators.bat` → **Create Flight Plan** → **Insert Weather Data in Flight Plan** (`FlightPlanMenuAction`, option **6**).
* **Controller:** `InsertWeatherDataInFlightController` in `eapli.alsafe.flightPlan.application`.
* **Domain:** `FlightPlan.insertWeatherData()`, `FlightPlan.markAsTested()` in `eapli.alsafe.flightPlan.domain`.
* **UI:** `InsertWeatherDataInFlightUI` + `InsertWeatherDataInFlightAction` in `alsafe.app.collaborators.console.presentation.flightplan`.
* **Authorization:** `Roles.PILOT` on `myFlightPlans()`, `availableWeatherData()` and `insertWeatherData()`.
* **Persistence:** `flightPlanRepository.save(flightPlan)` after domain update.
* **Remote:** no TCP handler registered yet (US086 scope).

### Package structure

```
flightPlan/
├── application/
│   └── InsertWeatherDataInFlightController.java   @UseCaseController
├── domain/
│   └── FlightPlan.java                            insertWeatherData(), markAsTested()
└── repositories/
    └── FlightPlanRepository.java                  findBySystemUser()

weather/
├── domain/
│   └── WeatherData.java
└── repositories/
    └── WeatherDataRepository.java                 findAll()
```

---

## 5. Integration / Demonstration

1. Run `run-bootstrap.bat` (companies, routes, aircraft, pilots, weather data).
2. Run `run-collaborators.bat` and log in as a user with the **Pilot** role.
3. Create a flight plan (US080) if none exists.
4. Optionally mark it as tested (`FlightPlan.markAsTested()` via US085 flow when available).
5. Navigate to **Create Flight Plan** → **Insert Weather Data in Flight Plan**.
6. Select one of your flight plans.
7. If tested, read the warning and confirm with `y`.
8. Select weather data from the list.
9. Verify success message and that `weatherData` is associated; if the plan was tested, confirm `tested` is now false.

---

## 6. Observations

* The early US080 design mentioned `invalidate()` on `FlightPlanStatus`; US082 actually voids the **`tested`** flag, leaving workflow status unchanged.
* `WeatherDataRepository.findAll()` exposes all registered records; there is no filtering by route area or departure date yet.
* The controller warning after save duplicates information already shown in the UI — useful for logging and for future non-console clients.

---

## 7. Initial plan vs actual implementation

| Topic | Initially planned | Actually implemented | Reason / note |
|-------|-------------------|----------------------|---------------|
| Test void mechanism | Call `FlightPlan.invalidate()` to revert status VALIDATED → DRAFT | Set **`tested = false`** inside **`insertWeatherData()`**; status unchanged | US082 voids the **test result**, not the validation workflow. |
| Weather association | Direct setter on `weatherData` field | **`insertWeatherData(WeatherData)`** domain method | Encapsulates null check and test voiding in the aggregate. |
| Pilot flight plans query | `findByPilot(authenticatedUser)` | **`findBySystemUser(authenticatedUser)`** (`e.pilot.systemUser = :user`) | Correct repository mapping in JPA and in-memory implementations. |
| Ownership enforcement | Implicit from filtered list only | **`ensureFlightPlanBelongsToAuthenticatedPilot()`** before save | Prevents tampering if a foreign `FlightPlan` instance is passed. |
| Weather data source | Filter by flight context (area/date) | **`WeatherDataRepository.findAll()`** | Simple catalogue of all registered weather data (AC4). |
| Warning before insert | Single message | **UI warning + y/n confirmation** before weather selection; **controller `System.out` warning** after save if plan was tested | AC7 in UI; feedback after voiding in controller. |
| Controller dependencies | No-arg constructor only | **No-arg + injected constructor** for unit tests | Enables `InsertWeatherDataInFlightControllerTest` with mocks. |
| UI selection | Manual numbered list | **`SelectWidget`** for flight plans and weather data | Consistent with other collaborators console screens. |
| Menu entry | Generic flight plan menu | **`FlightPlanMenuAction` option 6**, reached via **`CollaboratorsMainMenu` → Create Flight Plan** | Same submenu as create/submit/validate flight plan. |
| Remote access (US086) | Expose through TCP client | **Local console only** — no remote handler in `RemoteAccessHandlerRegistry` | Planned follow-up on US078 infrastructure. |
| Persistence after update | Update aggregate in memory only | **`flightPlanRepository.save(flightPlan)`** | Required for JPA/in-memory persistence. |
