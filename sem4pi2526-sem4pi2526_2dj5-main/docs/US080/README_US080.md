# US080 – Create a Flight Plan

## 1. Requirements Engineering

### 1.1 User Story Description

> As a **Pilot**, I want to register a flight plan for a route. I must add the aircraft, departure date/time, fuel quantity, pilot. The pilot must be of the route's company. Flight plan status is set to "draft" when created and must undergo a multi-step validation process.

---

### 1.2 Acceptance Criteria

- **AC1:** The flight plan must be associated with an existing **flight route**.
- **AC2:** The flight plan must be associated with an existing **aircraft** that is operational.
- **AC3:** The flight plan must be associated with a **pilot** that belongs to the **same company** as the selected route and aircraft.
- **AC4:** The **departure date/time** must be in the future.
- **AC5:** The **fuel quantity** must be a positive value with a valid unit.
- **AC6:** The **flight designator** (FlightPlanID) must follow the format: company IATA code (2 letters) + flight number (1–4 digits) + optional operational suffix (1 letter).
- **AC7:** The flight plan is created with status **DRAFT**.
- **AC8:** Only authenticated users with the **Pilot** role may create a flight plan.
- **AC9:** The flight plan must undergo a multi-step validation process before becoming validated.

---

### 1.3 Found Out Dependencies

| Dependency | Description |
|---|---|
| **US030** | Authentication and authorisation must be in place — only a Pilot can perform this action. |
| **US060** | An air transport company must already exist before a route can be associated with it. |
| **US070** | An aircraft must already exist in the company's fleet. |
| **US073** | A flight route must already exist before a flight plan can reference it. |
| **US075** | A pilot must already exist and be linked to a company. |
| **US052** | Airports must exist for the route to be valid. |

---

### 1.4 Input and Output Data

**Input:**
- Flight designator (airline code + number, e.g. PT123)
- Flight route (selected from existing routes of the pilot's company)
- Aircraft (selected from the company's operational fleet)
- Pilot (the authenticated user's pilot profile — automatic)
- Departure date and time
- Fuel quantity
- Fuel unit (from system configuration)

**Output:**
- Confirmation of successful flight plan creation with the flight plan details (ID, route, aircraft, pilot, date/time, fuel, status: DRAFT)
- Error message if any validation fails (invalid designator, aircraft not operational, pilot not from the route's company, etc.)

---

### 1.5 System Sequence Diagram (SSD)

> See `SD_US080.puml` / `SD_US080.svg` in this folder.

---

### 1.6 Other Relevant Remarks

- The flight plan is initially created in **DRAFT** state. It must be submitted and validated through a multi-step process before it can be used in simulations.
- The `FlightPlan` aggregate stores a reference to `WeatherData` (nullable) for future integration with US082 (insert weather data in a flight).
- The user chooses the **fuel unit** (KG, L, or LBS) during flight plan creation.
- The flight designator is composed of the company's IATA code (2 letters) followed by a numeric flight number and an optional operational suffix.

---

## 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

The following concepts from the Domain Model are relevant to this US:

- `FlightPlan` – the aggregate root being created, identified by `FlightPlanID`
- `FlightRoute` – the route the flight plan follows (between two airports)
- `Aircraft` – the aircraft assigned to the flight
- `Pilot` – the pilot responsible for the flight
- `FuelQuantity` – value object encapsulating fuel amount and unit
- `FuelUnit` – enum (L, KG, LBS) for the fuel unit
- `FlightPlanStatus` – enum tracking the flight plan's validation state
- `WeatherData` – optional weather data associated with the flight (for US082)

---

## 3. Design

### 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|---|---|---|---|
| 1 | Who initiates the use case? | `Pilot` via `CreateFlightPlanUI` | Follows MVC; UI layer is responsible for interaction only. |
| 2 | How is the pilot's company determined? | The controller retrieves the authenticated `Pilot` via `PilotRepository.findBySystemUser()` | The pilot is automatically linked to their company; no manual selection needed. |
| 3 | How are available routes presented? | `FlightPlanService` queries `FlightRouteRepository.findByCompany()` filtered by the pilot's company | Only routes belonging to the pilot's company are displayed. |
| 4 | How are available aircraft presented? | `FlightPlanService` queries `AircraftRepository.findByCompany()` filtered by the pilot's company | Only aircraft belonging to the same company are displayed. |
| 5 | Where is the flight plan created? | `FlightPlanService` calls `FlightPlan.create()` static factory method | The factory validates all invariants (pilot.company == route.company, aircraft operational, future departure) before construction. |
| 6 | Where is business logic orchestrated? | `FlightPlanService` coordinates creation and validation | Keeps the controller thin and reusable across different UIs (console, remote TCP). |
| 7 | How is authorisation enforced? | The controller verifies the authenticated user's role via `AuthorizationService` before proceeding | Only users with the `PILOT` role may access this functionality. |

---

### 3.2 State Machine

```
                  submit()          approve()
    ┌─────┐    ─────────────► ┌──────────┐ ──────────► ┌──────────┐
    │DRAFT│                    │SUBMITTED │             │VALIDATED │
    └──┬──┘                    └─────┬────┘             └──────────┘
       │                            │     reject()
       │ cancel()                   ├────────────────► ┌──────────┐
       │                            │                  │ REJECTED │
       ▼                            ▼                  └──────────┘
    ┌──────────┐              ┌──────────┐
    │CANCELLED │              │CANCELLED │
    └──────────┘              └──────────┘
                                     ▲
                                     │ invalidate() (US082)
                                     │
                                ┌──────────┐
                                │VALIDATED │
                                └──────────┘
```

| Transition | Method | Precondition | Postcondition |
|---|---|---|---|
| DRAFT → SUBMITTED | `submit()` | Status == DRAFT | Status = SUBMITTED |
| SUBMITTED → VALIDATED | `approve()` | Status == SUBMITTED | Status = VALIDATED |
| SUBMITTED → REJECTED | `reject()` | Status == SUBMITTED | Status = REJECTED |
| DRAFT/SUBMITTED → CANCELLED | `cancel()` | Status == DRAFT or SUBMITTED | Status = CANCELLED |
| VALIDATED → DRAFT | `invalidate()` | Status == VALIDATED | Status = DRAFT |

---

### 3.3 Sequence Diagram

> See `SD_US080.puml` / `SD_US080.svg` in this folder.

---

### 3.4 Applied Design Patterns

- **MVC** – `CreateFlightPlanUI` → `FlightPlanController` → `FlightPlanService` → Domain/Repository
- **Application Service** – `FlightPlanService` orchestrates creation and validation, keeping the controller thin
- **Static Factory Method** – `FlightPlan.create(...)` with all required parameters and invariant validation
- **Repository** – `FlightPlanRepository`, `FlightRouteRepository`, `AircraftRepository`, `PilotRepository`
- **Value Object** – `FlightPlanID`, `FuelQuantity`
- **Enum** – `FlightPlanStatus`, `FuelUnit`
- **Aggregate Root** – `FlightPlan` enforces invariants (pilot.company == route.company, aircraft operational, valid status transitions)

---

### 3.5 Tests

| Test ID | Test Description | Expected Result |
|---|---|---|
| T1 | Create flight plan with all valid data | FlightPlan created with status DRAFT |
| T2 | Create flight plan with pilot from a different company than the route | Creation rejected; pilot-company mismatch error |
| T3 | Create flight plan with a decommissioned aircraft | Creation rejected; aircraft not operational error |
| T4 | Create flight plan with a past departure date/time | Creation rejected; date must be in the future |
| T5 | Create flight plan with invalid flight designator | Creation rejected; designator format error |
| T6 | Create flight plan with zero fuel quantity | Creation rejected; fuel must be positive |
| T7 | Create flight plan as a user without the Pilot role | Creation rejected; authorisation error |
| T8 | Submit a DRAFT flight plan (status transition) | Status changes to SUBMITTED |
| T9 | Approve a SUBMITTED flight plan | Status changes to VALIDATED |
| T10 | Reject a SUBMITTED flight plan | Status changes to REJECTED |
| T11 | Cancel a DRAFT flight plan | Status changes to CANCELLED |
| T12 | Cancel a SUBMITTED flight plan | Status changes to CANCELLED |

---

## 4. Implementation

### Package structure

```
flightplan/
├── application/
│   ├── FlightPlanController.java         @UseCaseController
│   ├── FlightPlanService.java            orquestração: criação + validação
├── domain/
│   ├── FlightPlan.java                   aggregate root (@Entity)
│   ├── FlightPlanStatus.java             enum
│   ├── FlightPlanID.java                 value object (@EmbeddedId)
│   ├── FuelQuantity.java                 value object (@Embeddable)
│   ├── FuelUnit.java                     enum (L, KG, LBS)
└── repositories/
    └── FlightPlanRepository.java         interface (DomainRepository<FlightPlanID, FlightPlan>)
```

### Key classes

- `FlightPlan` – aggregate root with static factory `create()`, state transition methods (`submit()`, `approve()`, `reject()`, `cancel()`, `invalidate()`), and nullable `weatherData` for US082.
- `FlightPlanService` – application service that creates flight plans, retrieves routes/aircraft/pilots by company, and performs validation (fuel sufficiency, pilot certification).
- `FlightPlanController` – controller that authorises the Pilot role and delegates to `FlightPlanService`.
- `CreateFlightPlanUI` – console UI that guides the pilot through route selection, aircraft selection, pilot confirmation, and data entry.

### UI

- `CreateFlightPlanUI` extends `AbstractUI`; located in `alsafe.app.collaborators.console.presentation.flightplan`
- Step by step: selects route → selects aircraft → confirms pilot → enters designator → enters date/time → enters fuel unit → enters fuel quantity
- `CreateFlightPlanAction` wraps the UI
- The option is available in `FlightPlanMenuAction` under the Flight Plans submenu (accessible for `PILOT` role)

---

## 5. Integration / Demonstration

To demonstrate the functionality:

1. Run `run-bootstrap.bat` (companies, aircraft models, airports, routes, aircraft, pilots).
2. Run `run-collaborators.bat` and log in as a user with the `PILOT` role.
3. Navigate to **Flight Plans** submenu and select **Create Flight Plan**.
4. Select a route from the available list.
5. Select an aircraft from the company's fleet.
6. Confirm the pilot (authenticated user).
7. Enter the flight designator, departure date/time, fuel unit, and fuel quantity.
8. Confirm creation.
9. Verify success message with flight plan in DRAFT status.

---

## 6. Observations

- The `FlightPlan` aggregate has a nullable `weatherData` attribute reserved for US082 (insert weather data in a flight).
- The static factory method `FlightPlan.create(...)` was chosen over a Builder pattern because all parameters are mandatory, making a builder unnecessarily verbose.
- The `FlightPlanService` is designed to be UI-agnostic, allowing reuse by both the ATCC console UI and the remote TCP client (US086).
