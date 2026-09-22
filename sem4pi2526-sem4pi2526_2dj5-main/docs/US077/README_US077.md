# US077 – Remove a Pilot

## 1. Requirements Engineering

### 1.1 User Story Description

> As an **Air Transport Company Collaborator**, I want to make a pilot inactive in my company's roster.

---

### 1.2 Acceptance Criteria

* **AC1:** The Air Transport Company Collaborator must only be able to deactivate pilots belonging to their own company.
* **AC2:** A pilot with flight plans currently assigned cannot be deactivated.
* **AC3:** A deactivated pilot must no longer appear in the active pilot roster (US076).
* **AC4:** The system must record/update the pilot's operational status (Active/Inactive).

---

### 1.3 Found Out Dependencies

| Dependency | Description |
|------------|-------------|
| **US075** | A pilot must first be registered in the company before they can be deactivated. |
| **US076** | The collaborator lists the active pilot roster; deactivated pilots are excluded via `Pilot.isActive()`. |
| **US078** | The same `RemovePilotController` is reused by `RemovePilotRequestHandler` (opCode 12) for TCP remote access. |
| **US030** | Authentication and authorization must be in place for the ATCC role. |

---

### 1.4 Input and Output Data

**Input:**
- Selection of a pilot from a numbered list of active pilots belonging to the authenticated collaborator's company.
- Confirmation (`Y/N`) before deactivation.

**Output:**
- Pilot's linked `SystemUser` deactivated through `UserManagementService`.
- Console message on success or on constraint violation (assigned flight plans, wrong company, already inactive).

---

### 1.6 Other Relevant Remarks

* Deactivation is logical — the `Pilot` aggregate is retained; operational status is derived from `SystemUser.isActive()`.
* Assigned flight plans are those in status **DRAFT**, **SUBMITTED** or **VALIDATED** (`FlightPlanRepository.findAssignedFlightPlansByPilot`).
* The company scope is resolved through `CollaboratorRepositoryATCC` for the authenticated ATCC user.

---

## 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

* **`Pilot`** — roster entity; `isActive()` reflects the linked `SystemUser` status.
* **`SystemUser`** (framework) — holds the active/inactive flag used at login.
* **`CollaboratorATCC`** — links the authenticated user to an `AirTransportCompany`.
* **`FlightPlan`** — checked for pending assignments before deactivation.
* **`UserManagementService`** — framework service that deactivates the pilot's system user account.

---

## 3. Design

### 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|----------------|----------|--------|---------------|
| 1 | Who initiates the use case? | `AirTransportCompanyCollaborator` via `RemovePilotUI` (local) or `RemovePilotActionRemote` (US078) | Same controller serves console and TCP clients. |
| 2 | How is the pilot marked inactive? | `UserManagementService.deactivateUser(pilot.user())` | Aligns with US032/US033 pattern; login blocking uses framework `SystemUser.isActive()`. |
| 3 | How is the company determined? | `CollaboratorRepositoryATCC.findBySystemUser()` + `findCompanyByCollaborator()` | Ensures AC1 — only pilots from the collaborator's company are eligible. |
| 4 | How is the pilot selected? | `activePilotsForCurrentCompany()` → `PilotRepository.findActiveByCompany(company)` | Same active filter used by US076. |
| 5 | How is the flight plan constraint enforced? | `FlightPlanRepository.findAssignedFlightPlansByPilot(pilot)` before deactivation | Rejects when any assigned plan exists (AC2). |
| 6 | Where is authorization enforced? | `RemovePilotController` via `AuthorizationService.ensureAuthenticatedUserHasAnyOf(Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR)` | Checked on every controller operation. |

---

### 3.2 Sequence Diagram

> See `SD_US077.puml` in this folder.

---

### 3.3 Applied Design Patterns

* **MVC** — `RemovePilotUI` / `RemovePilotActionRemote` → `RemovePilotController` → domain/repositories.
* **Application Service** — `UserManagementService` persists the inactive status on `SystemUser`.
* **Repository** — `PilotRepository`, `FlightPlanRepository`, `CollaboratorRepositoryATCC`.
* **Request Handler (US078)** — `RemovePilotRequestHandler` delegates to the same controller without duplicating business rules.

---

### 3.4 Tests

| Test ID | Test Description | Expected Result | Automated test |
|---------|------------------|-----------------|----------------|
| T1 | Deactivate a pilot with no assigned flight plans | `userSvc.deactivateUser` called; `pilot.isActive()` false. | `RemovePilotControllerTest.ensureAtccCanDeactivatePilotWithoutAssignedFlightPlans` |
| T2 | Attempt to deactivate a pilot with assigned flight plans | `IllegalStateException`; user not deactivated. | `RemovePilotControllerTest.ensurePilotWithAssignedFlightPlansCannotBeDeactivated` |
| T3 | Pilot from another company | `SecurityException`; user not deactivated. | `RemovePilotControllerTest.ensurePilotFromAnotherCompanyCannotBeDeactivated` |
| T4 | List active pilots for company | Returns only active pilots from `findActiveByCompany`. | `RemovePilotControllerTest.ensureDeactivatedPilotIsExcludedFromActivePilotList` |
| T5 | Unauthorized caller | `SecurityException`; repositories not called. | `RemovePilotControllerTest.ensureUnauthorizedCallerIsRejected` |
| T6 | Unknown pilot id | `IllegalArgumentException`. | `RemovePilotControllerTest.ensureUnknownPilotIsRejected` |

Run: `mvn -pl alsafe.core test -Dtest=RemovePilotControllerTest`

---

## 4. Implementation

Key implementation notes:

* **Menu (local):** `run-collaborators.bat` → **Remove Pilot** (`CollaboratorsMainMenu`, option 8).
* **Menu (remote, US078):** `AtccRemoteApp` → **Remove a Pilot** (`AtccMainMenu`, opCode 12 via `RemovePilotActionRemote`).
* **Controller:** `RemovePilotController` in `eapli.alsafe.pilotmanagement.application`.
* **UI:** `RemovePilotUI` in `alsafe.app.collaborators.console.presentation.pilot`.
* **Remote handler:** `RemovePilotRequestHandler` in `eapli.alsafe.remoteaccess.server.requesthandlers.atcc` (opCode **12**, payload = pilot id).
* **Authorization:** `Roles.AIR_TRANSPORT_COMPANY_COLLABORATOR`.
* **Status change:** `userSvc.deactivateUser(pilot.user())` — no call to `PilotRepository.save()` or `Pilot.deactivate()` from the controller.

---

## 5. Integration / Demonstration

### Local console

1. Run `run-bootstrap.bat` (companies, pilots, routes).
2. Run `run-collaborators.bat` and log in as an **Air Transport Company Collaborator**.
3. Open **List Pilots** (US076) and note active pilots.
4. Select **Remove Pilot**, pick a pilot with no assigned flight plans, confirm with `Y`.
5. Verify the pilot no longer appears in the active roster.
6. Attempt to deactivate a pilot with assigned flight plans — operation must be rejected.

### Remote (US078)

1. Start `RemoteServer` (TCP port **2223**) and optionally `LoggingServer` (UDP port **8888**).
2. Run `AtccRemoteApp`, authenticate as ATCC.
3. Use **List Pilot Roster** (opCode 11) then **Remove a Pilot** (opCode 12).

---

## 6. Observations

* `Pilot.deactivate()` exists in the domain but the controller uses `UserManagementService` for consistency with framework authz (same approach as US032).
* Remote deactivation reuses the local controller; TCP handlers do not maintain a server-side session — authorization relies on the server-side auth context established at login.
* Only flight plans in DRAFT, SUBMITTED or VALIDATED block deactivation; completed or cancelled plans do not.

---

## 7. Initial plan vs actual implementation

| Topic | Initially planned | Actually implemented | Reason / note |
|-------|-------------------|----------------------|---------------|
| UI location | `RemovePilotUI` in backoffice (`alsafe.app.backoffice.console.presentation.pilots`) | **`RemovePilotUI`** in **`alsafe.app.collaborators.console.presentation.pilot`** | ATCC operations live in the collaborators console, not backoffice. |
| Menu entry | `PilotMenuAction` submenu | Direct item in **`CollaboratorsMainMenu`** (option 8) | Matches other ATCC features (Add Pilot, List Pilots). |
| Controller method for listing | `getPilots()` | **`activePilotsForCurrentCompany()`** | Explicit name; delegates to `PilotRepository.findActiveByCompany`. |
| Deactivation operation | `pilot.deactivate()` + `PilotRepository.save()` | **`UserManagementService.deactivateUser(pilot.user())`** | Active flag is on framework `SystemUser`; `Pilot.isActive()` reads `systemUser.isActive()`. |
| Company resolution | Implicit from session | **`CollaboratorRepositoryATCC`** (`findBySystemUser` + `findCompanyByCollaborator`) | Same pattern as `ListPilotRosterController` and other ATCC controllers. |
| Assigned flight plans query | Generic `findByPilot` | **`findAssignedFlightPlansByPilot`** filtering DRAFT/SUBMITTED/VALIDATED | Matches US requirement for pending assignments only. |
| Confirmation | Selection by number only | **Y/N confirmation** in `RemovePilotUI` and `RemovePilotActionRemote` | Explicit guard before irreversible deactivation. |
| Remote access | Not in initial US077 scope | **`RemovePilotRequestHandler`** (opCode 12) via **US078** infrastructure | Controller designed for reuse; remote client lists roster (opCode 11) then sends pilot id (opCode 12). |
| Tester console | Not planned | Also available in **`TesterMainMenu`** | Uses same `RemovePilotAction` for integration testing. |
