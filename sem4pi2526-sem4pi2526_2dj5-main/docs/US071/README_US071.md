# US071 – Decommission an Aircraft

## 1. Requirements Engineering

### 1.1 User Story Description

> As an **Air Transport Company Collaborator**, I want to retire an aircraft from my company’s fleet. It’s not possible to create flights for a retired aircraft.

---

### 1.2 Acceptance Criteria

* **AC1:** The aircraft's operational status must be updated to "Retired" or "Decommissioned".
* **AC2:** The system must prevent the creation of new flight plans (US080) for an aircraft that has been decommissioned.
* **AC3:** The decommissioning process is irreversible for the purpose of creating new flights.
* **AC4:** Only collaborators belonging to the same Air Transport Company as the aircraft can perform this action.

---

### 1.3 Found Out Dependencies

| Dependency | Description |
|------------|-------------|
| **US070** | Aircraft must exist in the company fleet. |
| **US061** | User must be a registered ATCC collaborator. |
| **US080** | Flight-plan logic should call `assertOperationalForNewFlightPlans()` (guard prepared on `Aircraft`). |

---

### 1.4 Input and Output Data

**Input:**
- Aircraft selected from the company’s **active** fleet (not already decommissioned).
- Confirmation `Y` / `N`.

**Output:**
- Success: status `DECOMMISSIONED`.
- Errors: empty fleet, cancelled confirmation, already retired, unknown registration, access denied (other company).

---

### 1.6 Other Relevant Remarks

* Aggregate: **`eapli.alsafe.aircraft.domain.Aircraft`** (table `fleet_aircraft`).
* Status enum: **`MaintenanceStatus.DECOMMISSIONED`** (also `OPERATIONAL`, `UNDER_MAINTENANCE`).

---

## 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

* **`Aircraft`** — aggregate root; `decommission()`, `assertOperationalForNewFlightPlans()`.
* **`RegistrationID`** — embedded identity.
* **`MaintenanceStatus`** — includes `DECOMMISSIONED`.
* **`AirTransportCompany`** — owner; company resolved from logged-in **`CollaboratorATCC`**.

---

## 3. Design

### 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|----------------|----------|--------|---------------|
| 1 | Who initiates? | ATCC via `DecommissionAircraftUI` | Primary entry: **`run-atcc.bat`**; also user console for ATCC role. |
| 2 | Status change | `Aircraft.decommission()` | Sets `DECOMMISSIONED`; throws if already decommissioned. |
| 3 | Company check | Compare aircraft `company().identity()` with collaborator’s company | AC4 — `SecurityException` if mismatch. |
| 4 | Fleet listing | `activeFleetForCurrentCompany()` filters out decommissioned | UI only offers aircraft that can still be retired. |
| 5 | Persistence | `AircraftRepository.save(aircraft)` | Standard update of existing aggregate. |
| 6 | Collaborator company | `CollaboratorRepositoryATCC.findBySystemUser` + `findCompanyByCollaborator` | Same pattern as US070. |

---

### 3.2 Sequence Diagram

> See `SD_US071.puml` / `SD_US071.svg` in this folder.

---

### 3.3 Applied Design Patterns

* **MVC** — `DecommissionAircraftUI` in `alsafe.app.common.console` → `DecommissionAircraftController`.
* **Repository** — `AircraftRepository`, `CollaboratorRepositoryATCC`.
* **Domain guard** — `assertOperationalForNewFlightPlans()` for US080 integration.

---

### 3.4 Tests

| Test ID | Test Description | Expected Result | Automated test |
|---------|------------------|-----------------|----------------|
| T1 | Decommission active aircraft | `DECOMMISSIONED`; `save` called. | `DecommissionAircraftControllerTest`, `AircraftDecommissionTest` |
| T2 | Other company’s aircraft | `SecurityException`; no save. | `DecommissionAircraftControllerTest` |
| T3 | Flight-plan guard | `"Aircraft is retired"`. | `AircraftDecommissionTest` |
| T4 | Already decommissioned | `IllegalStateException`; no save. | Both test classes |
| T5 | Active fleet list | Excludes decommissioned aircraft. | `DecommissionAircraftControllerTest` |

Run: `mvn -pl alsafe.core test -Dtest=AircraftDecommissionTest,DecommissionAircraftControllerTest`

---

## 4. Implementation

Key implementation notes:

* **Controller:** `DecommissionAircraftController` — `activeFleetForCurrentCompany()`, `decommissionAircraft(RegistrationID)`.
* **UI:** `DecommissionAircraftUI` + `DecommissionAircraftAction` in **`alsafe.app.common.console`**.
* **Menus:** **`AtccMainMenu`** (`run-atcc.bat`) and **AlSafe user** menu for `AIR_TRANSPORT_COMPANY_COLLABORATOR`.
* **Domain:** `MaintenanceStatus.DECOMMISSIONED` (not a separate `RETIRED` literal).
* **Legacy:** older `eapli.alsafe.fleetmanagement` package is **not** used by this flow.

---

## 5. Integration / Demonstration

1. Bootstrap data; ensure an aircraft exists (US070).
2. Run **`run-atcc.bat`** and log in as an ATCC collaborator.
3. **Decommission Aircraft** — select from active fleet, confirm with `Y`.
4. Confirm message shows status **DECOMMISSIONED**.
5. List fleet (US072) — aircraft should appear as decommissioned / excluded from active list.
6. (Future US080) creating a flight plan for that registration must fail via `assertOperationalForNewFlightPlans()`.

---

## 6. Observations

* Decommission is **not** exposed in the backoffice menu for operators; it belongs to the company collaborator app (like US070).
* Historical flight data for the aircraft is not deleted.

---

## 7. Initial plan vs actual implementation

| Topic | Initially planned | Actually implemented | Reason / note |
|-------|-------------------|----------------------|---------------|
| Status value | `RETIRED` or enum `ACTIVE` / `RETIRED` | **`MaintenanceStatus.DECOMMISSIONED`** | Reuses existing aircraft maintenance enum from US070 domain. |
| Package | `fleetmanagement` aggregate | **`eapli.alsafe.aircraft`** + `AircraftRepository` | Canonical fleet model aligned with US070. |
| UI entry | Collaborator types registration ID manually | **`SelectWidget`** over **`activeFleetForCurrentCompany()`** | Safer UX; only non-decommissioned company aircraft. |
| Company resolution | Generic `CollaboratorRepository` + `collaborator.company()` | **`CollaboratorRepositoryATCC`** + **`findCompanyByCollaborator`** | Matches US061 ATCC model and US070 controller. |
| Auth in UI | `checkPermission(MANAGE_FLEET)` in UI | **`AuthorizationService`** in **controller** only | Consistent with other use-case controllers. |
| Menu location | Backoffice or generic user app | **`run-atcc.bat`** (`AtccMainMenu`) + shared UI module | Fleet operations live on the ATCC application (see US070 doc). |
| US080 hook | Block in `Flight` / `FlightPlan` | **`Aircraft.assertOperationalForNewFlightPlans()`** ready on domain | AC2 guard centralized on aircraft; US080 will call it when implemented. |
| Reversibility | Irreversible for new flights | Domain does not forbid future status changes, but guard blocks new plans when decommissioned | Satisfies AC3 for scheduling purposes. |
