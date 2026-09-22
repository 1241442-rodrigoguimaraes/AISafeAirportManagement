# US070 – Add an Aircraft to an Air Transport Company

## 1. Requirements Engineering

### 1.1 User Story Description

> As an **Air Transport Company Collaborator**, I want to add an aircraft to my company's fleet.
>
> The aircraft is of a given model, including the engines, and the number of seats of each type/class must be provided. Total number of seats cannot exceed the model's capacity. An aircraft is identified by the aircraft registration number, that is unique. Also, an aircraft is registered in a country (it may not be the company's home country). An aircraft also has an operational status.

---

### 1.2 Acceptance Criteria

- **AC1:** The aircraft must be associated with an existing **aircraft model**.
- **AC2:** The **registration ID** must be unique across the entire system.
- **AC3:** The **cabin configuration** (number of seats per class) must be provided and the total must not exceed the model's maximum capacity.
- **AC4:** The **registration country** must be valid.
- **AC5:** The aircraft must be associated with exactly **one air transport company** (the collaborator's company).
- **AC6:** The aircraft is created with an initial **operational (maintenance) status**.
- **AC7:** At least one **crew element** must be associated with the aircraft.

---

### 1.3 Found Out Dependencies

| Dependency | Description |
|---|---|
| **US055** | An aircraft model must already exist before an aircraft can be created from it. |
| **US060** | The air transport company must already be registered in the system. |
| **US061** | The collaborator must already be registered and associated with the company. |

---

### 1.4 Input and Output Data

**Input:**
- Aircraft model (selected from existing models)
- Registration ID (unique identifier)
- Registration country
- Cabin configuration (number of seats per class)
- Number of crew elements

**Output:**
- Confirmation of successful aircraft registration with the aircraft details
- Error message if the registration ID already exists, the model does not exist, or the cabin configuration exceeds the model's capacity

---

### 1.5 System Sequence Diagram (SSD)

> See `SD_US070.puml` / `SD_US070.svg` in this folder.

---

### 1.6 Other Relevant Remarks

- The collaborator is automatically associated with their company — they do not manually select it.
- The `MaintenanceStatus` value object is set to an initial operational state upon creation.
- `CabinConfiguration` and `AircraftCountry` are value objects in the domain model that encapsulate their own validation.

---

## 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

The following concepts from the Domain Model are relevant to this US:

- `Aircraft` – the aggregate root being created, identified by `RegistrationID`
- `AircraftModel` – the model the aircraft is based on
- `AirTransportCompany` – the company the aircraft belongs to
- `CabinConfiguration` – value object for seat layout per class
- `AircraftCountry` – value object for the registration country
- `MaintenanceStatus` – value object for the aircraft's operational status
- `CrewElement` – entity representing each crew member slot associated with the aircraft

---

## 3. Design

### 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|---|---|---|---|
| 1 | Who initiates the use case? | `AirTransportCompanyCollaborator` via `AddAircraftUI` | Follows MVC; UI layer is responsible for interaction only. |
| 2 | How is the collaborator's company determined? | `CollaboratorRepositoryATCC.findBySystemUser` + `findCompanyByCollaborator` | Company comes from the logged-in ATCC profile, not from manual selection. |
| 3 | How are aircraft models listed? | `AircraftModelRepository.findAll()` | Repository pattern; presents all available models to the collaborator. |
| 4 | Where is uniqueness of registration ID enforced? | `AircraftRepository.existsByRegistrationID(registrationID)` | Persistence-level check ensures global uniqueness before creation. |
| 5 | Who creates the `Aircraft` aggregate? | `AircraftRepository` delegates to `Aircraft` constructor | The aggregate root enforces its own invariants on creation. |
| 6 | How is `CabinConfiguration` validated? | `CabinConfiguration` value object validates total seats against the model's capacity | Encapsulates validation within the value object; fails fast. |

---

### 3.2 Sequence Diagram

> See `SD_US070.puml` / `SD_US070.svg` in this folder.

---

### 3.3 Applied Design Patterns

- **MVC** – `AddAircraftUI` → `AddAircraftController` → Domain/Repository
- **Repository** – `AircraftRepository`, `AircraftModelRepository`, `AirTransportCompanyRepository`
- **Value Object** – `RegistrationID`, `CabinConfiguration`, `AircraftCountry`, `MaintenanceStatus`
- **Aggregate Root** – `Aircraft` enforces invariants (unique registration, valid configuration, mandatory company)

---

### 3.4 Tests

| Test ID | Test Description | Expected Result | Automated test |
|---|---|---|---|
| T1 | Add aircraft with all valid data | Aircraft created and persisted; success message shown. | `AddAircraftControllerTest.addAircraft_withValidData_persistsAircraft` |
| T2 | Add aircraft with duplicate registration ID | Registration rejected; informative error message. | `AddAircraftControllerTest.addAircraft_duplicateRegistration_throws` |
| T3 | Add aircraft with non-existent aircraft model | Registration rejected; model not found error. | Manual / UI selection |
| T4 | Add aircraft with cabin configuration exceeding model capacity | Registration rejected; capacity validation error. | `AddAircraftControllerTest.addAircraft_cabinExceedsCapacity_throws` |
| T5 | Add aircraft with invalid registration country | Registration rejected; country validation error. | Domain `AircraftCountry` (manual) |
| T6 | Add aircraft with zero crew elements | Registration rejected; crew validation error. | `AddAircraftControllerTest.addAircraft_zeroCrew_throws` |

Run: `mvn -pl alsafe.core test -Dtest=AddAircraftControllerTest`

---

## 4. Implementation

Key implementation notes:
- **ATCC console (US78 / fleet management):** `run-atcc.bat` → `AtccApp` → **Add Aircraft to Fleet** (`AddAircraftUI` / `AddAircraftAction` in `alsafe.app.common.console`). Not available in Backoffice for ATCC collaborators.
- **Controller:** `AddAircraftController` resolves company via `CollaboratorRepositoryATCC.findBySystemUser` + `findCompanyByCollaborator`.
- **Uniqueness:** `AircraftRepository.existsByRegistrationID` before `addAircraft`.
- **Validation:** `CabinConfiguration` enforces model capacity; at least one `CrewElement`; `MaintenanceStatus.OPERATIONAL` on `Aircraft.create`.

### 4.1 Design Changes

| Initial idea | What we implemented | Why |
|---|---|---|
| Menu in Backoffice for the ATCC collaborator | **`run-atcc.bat`** → `AtccMainMenu` (options Add / Decommission aircraft) | US078 expects fleet actions on the Air Transport Company app, not the backoffice operator console. |
| UI only in `user.console` | `AddAircraftUI` / `AddAircraftAction` in **`alsafe.app.common.console`** | Shared by ATCC app (and user console if needed) without duplicating screens. |
| Operator chooses the company | Company from authenticated `CollaboratorATCC` | AC5: exactly one company per collaborator; avoids registering aircraft on the wrong fleet. |
| Generic collaborator repository | `CollaboratorRepositoryATCC` + `AircraftRepository.addAircraft(...)` | Aligns with US061 ATCC model and keeps fleet persistence in the aircraft aggregate. |

---

## 5. Integration / Demonstration

To demonstrate the functionality:

1. Run `run-bootstrap.bat` (aircraft models and companies).
2. As **BackofficeOP** (`run-backoffice.bat`): **6. Collaborators > Add Collaborator** → option **2. Air Transport Company** → create ATCC user.
3. Run `run-atcc.bat` and log in with that collaborator email/password.
4. Select **Add Aircraft to Fleet** (option 2).
5. Choose model, registration ID, country, seat counts and crew count.
6. Confirm success; verify fleet later with US072 when implemented.

---

## 6. Observations

- With US071 (decommission) and US072 (list fleet), completes the basic aircraft lifecycle for a company.
- Future improvement: validate registration ID format (e.g. ICAO country prefix).