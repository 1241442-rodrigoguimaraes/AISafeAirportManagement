# US062 – List Customer's Collaborators

## 1. Requirements Engineering

### 1.1 User Story Description

> As a **Backoffice Operator**, I want to list all collaborators of a given customer. It should not list disabled collaborators.

---

### 1.2 Acceptance Criteria

- **AC1:** The system must display only **active** collaborators (disabled collaborators must be excluded from the list).
- **AC2:** The operator must be able to identify the customer (air transport company or air control area) whose collaborators are being listed.
- **AC3:** The list must include relevant collaborator information (name, email, position/role).
- **AC4:** If the customer has no active collaborators, an appropriate message must be displayed.

---

### 1.3 Found Out Dependencies

| Dependency | Description |
|---|---|
| **US060** | An air transport company must already exist to list its collaborators. |
| **US050** | An air control area must already exist to list its collaborators. |
| **US061** | Collaborators must have been previously registered in the system. |
| **US064** | Collaborators may have been disabled; this US must filter them out. |

---

### 1.4 Input and Output Data

**Input:**
- Customer identifier (selected from a list of existing customers — air transport companies or air control areas)

**Output:**
- List of active collaborators for the selected customer, including:
    - Full name
    - Email address
    - Position / role in the system
- Informative message if no active collaborators exist

---

### 1.5 System Sequence Diagram (SSD)

> See `SD_US062.puml` / `SD_US062.svg` in this folder.

---

### 1.6 Other Relevant Remarks

- A "customer" in this context may be either an **air transport company** or an **air control area**, as defined in US061.
- Disabled collaborators must be completely hidden from the list — they should not appear even as inactive entries.

---

## 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

The following concepts from the Domain Model are relevant to this US:

- `CollaboratorFCO` / `CollaboratorATCC` – collaborators by customer type (area vs company), each linked to a `SystemUser`
- `AirTransportCompany` / `AirControlArea` – the customers whose collaborators are being listed
- `SystemUser` – the underlying user entity, which carries the active/disabled status

---

## 3. Design

### 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|---|---|---|---|
| 1 | Who initiates the use case? | `BackofficeOperator` via `ListCollaboratorsUI` | Follows MVC; UI layer is responsible for interaction only. |
| 2 | How are available customers presented? | `ListCollaboratorsController` queries both `AirTransportCompanyRepository` and `AirControlAreaRepository` | Customers can be of two types; the controller aggregates both. |
| 3 | How is the filtering of disabled collaborators done? | `findActiveByCompany` / `findActiveByCustomerArea` on ATCC/FCO repositories | Query-level (JPA) or in-memory filter on `systemUser.active`. |
| 4 | Where is authorization enforced? | `ListCollaboratorsController` uses `AuthorizationService` | Only Backoffice Operators may access this functionality. |

---

### 3.2 Sequence Diagram

> See `SD_US062.puml` / `SD_US062.svg` in this folder.

---

### 3.3 Applied Design Patterns

- **MVC** – `ListCollaboratorsUI` → `ListCollaboratorsController` → Domain/Repository
- **Repository** – `CollaboratorRepository`, `AirTransportCompanyRepository`, `AirControlAreaRepository`
- **Service** – `AuthorizationService` for access control

---

### 3.4 Tests

| Test ID | Test Description | Expected Result | Automated test |
|---|---|---|---|
| T1 | List collaborators of a customer with active collaborators | List is returned with all active collaborators and their details. | `ListCollaboratorsControllerTest.listActiveCollaboratorsOfCompany_returnsOnlyActiveAtccs` |
| T2 | List collaborators of a customer with no active collaborators | Informative message displayed; empty list returned. | Manual / UI (`ListCollaboratorsUI`) |
| T3 | List collaborators of a customer that has both active and disabled collaborators | Only active collaborators appear in the list. | Repository filters `systemUser.active` (JPA) / `user().isActive()` (in-memory) |
| T4 | Attempt to list collaborators without proper authorization | Access denied; error message shown. | `ListCollaboratorsControllerTest.listActiveCollaboratorsOfArea_whenUnauthorized_throws` |
| T5 | List collaborators of an air control area (not just a company) | Collaborators (FCOs) of the area are listed correctly. | `ListCollaboratorsControllerTest.listActiveCollaboratorsOfArea_returnsOnlyActiveFcos` |

Run: `mvn -pl alsafe.core test -Dtest=ListCollaboratorsControllerTest`

---

## 4. Implementation

Key implementation notes:
- **Backoffice UI:** `ListCollaboratorsUI` → `ListCollaboratorsController` (menu **Collaborators > List Customer's Collaborators**).
- **Customers:** air transport company → `CollaboratorRepositoryATCC.findActiveByCompany(company)`; air control area → `CollaboratorRepositoryFCO.findActiveByCustomerArea(area)`.
- **DTO mapping:** FCO collaborators are returned as `CollaboratorDTO`; company collaborators are returned as `CollaboratorATCC` entities (displayed in the UI).
- Disabled collaborators are excluded at repository level (`systemUser.active = true` in JPA; `user().isActive()` in in-memory).

### 4.1 Design Changes

| Initial idea | What we implemented | Why |
|---|---|---|
| Single `Collaborator` + `findActiveByCustomer()` | `CollaboratorRepositoryFCO` and `CollaboratorRepositoryATCC` with `findActiveByCustomerArea` / `findActiveByCompany` | US061 split FCO (area) and ATCC (company) into separate aggregates and roles. |
| Filter “disabled” on the collaborator entity | Filter on `SystemUser.active` in the query / in-memory | Disable (US064) only deactivates the framework user; there is no separate flag on the collaborator row. |
| One list type for all customers | FCO → `CollaboratorDTO`; company → `CollaboratorATCC` in the UI | Kept pragmatic for the sprint; both show name, email, phone and role. |
| Match customer by entity `equals` in JPQL | Company/area identity in repository + active filter | Avoided empty lists and wrong matches when the persistence context had detached instances. |

---

## 5. Integration / Demonstration

To demonstrate the functionality:

1. Run `run-bootstrap.bat` to ensure customers and collaborators exist.
2. Run `run-backoffice.bat` and log in as **BackofficeOP**.
3. **6. Collaborators >** **List Customer's Collaborators**.
4. Choose **Air Transport Company** or **Air Control Area**, then select the customer.
5. Verify that only active collaborators are shown.
6. Disable one collaborator (US064) and repeat — confirm they no longer appear.

---

## 6. Observations

- This US is closely related to US064 (disable collaborator); together they define the visible/active boundary of the collaborator list.
- Future improvements: unify list output as `CollaboratorDTO` for both customer types; sorting by name or role.