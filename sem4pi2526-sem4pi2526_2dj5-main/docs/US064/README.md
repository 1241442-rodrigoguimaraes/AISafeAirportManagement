# US064 – Disable a Customer's Collaborator

## 1. Requirements Engineering

### 1.1 User Story Description

> As a **Backoffice Operator**, I want to disable a customer's collaborator so that he may not use the system anymore.

---

### 1.2 Acceptance Criteria

- **AC1:** The collaborator must exist and be currently **active** in order to be disabled.
- **AC2:** After being disabled, the collaborator must **not** be able to authenticate or perform any action in the system.
- **AC3:** The collaborator's data must be **preserved** in the system (soft delete — not a permanent removal).
- **AC4:** Disabled collaborators must no longer appear in active collaborator listings (e.g. US062).

---

### 1.3 Found Out Dependencies

| Dependency | Description |
|---|---|
| **US061** | The collaborator must have been previously registered in the system. |
| **US062** | Listing collaborators is used to select the one to disable. |
| **US060** / **US050** | The customer (company or area) must already exist. |

---

### 1.4 Input and Output Data

**Input:**
- Customer identifier (selected from existing customers)
- Collaborator identifier (selected from the active collaborators of that customer)

**Output:**
- Confirmation that the collaborator has been successfully disabled
- Error message if the collaborator is already disabled or does not exist

---

### 1.5 System Sequence Diagram (SSD)

> See `SD_US064.puml` / `SD_US064.svg` in this folder.

---

### 1.6 Other Relevant Remarks

- Disabling a collaborator also disables their underlying `SystemUser`, preventing login.
- This operation is **reversible** — a disabled collaborator can be re-enabled in the future (e.g. via US032 or a future US).
- The operation must be restricted to Backoffice Operators and Administrators only.

---

## 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

The following concepts from the Domain Model are relevant to this US:

- `CollaboratorFCO` / `CollaboratorATCC` – disabled via the linked `SystemUser`, not removed from the database
- `SystemUser` – the underlying user entity whose active status is toggled
- `AirTransportCompany` / `AirControlArea` – the customers whose collaborator is being disabled

---

## 3. Design

### 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|---|---|---|---|
| 1 | Who initiates the use case? | `BackofficeOperator` via `DisableCollaboratorUI` | Follows MVC; UI layer is responsible for interaction only. |
| 2 | How is the collaborator selected? | UI lists active collaborators via `findActiveByCompany` or `findActiveByCustomerArea` | Same active-only queries as US062. |
| 3 | Who performs the disable operation? | `DisableCollaboratorController` delegates to `UserManagementService.deactivateUser()` | The framework service handles the deactivation of the underlying `SystemUser`. |
| 4 | Where is authorization enforced? | `DisableCollaboratorController` uses `AuthorizationService` | Only Backoffice Operators and Administrators may perform this action. |
| 5 | Is the collaborator permanently removed? | No — soft delete via status flag | Data integrity is preserved; the operation is auditable and reversible. |

---

### 3.2 Sequence Diagram

> See `SD_US064.puml` / `SD_US064.svg` in this folder.

---

### 3.3 Applied Design Patterns

- **MVC** – `DisableCollaboratorUI` → `DisableCollaboratorController` → Domain/Repository
- **Repository** – `CollaboratorRepository`, `AirTransportCompanyRepository`, `AirControlAreaRepository`
- **Service** – `UserManagementService` for deactivation, `AuthorizationService` for access control

---

### 3.4 Tests

| Test ID | Test Description | Expected Result | Automated test |
|---|---|---|---|
| T1 | Disable an active collaborator | Collaborator is marked as disabled; success message shown. | `DisableCollaboratorControllerTest.disableCollaboratorFCO_activeUser_deactivatesAndReturnsTrue`, `disableCollaboratorATCC_activeUser_deactivatesAndReturnsTrue` |
| T2 | Attempt to disable an already disabled collaborator | Operation rejected; informative error message shown. | `DisableCollaboratorControllerTest.disableCollaboratorFCO_alreadyInactive_returnsFalseWithoutDeactivate` |
| T3 | Disabled collaborator attempts to log in | Authentication fails; access denied. | Manual (framework auth) |
| T4 | List collaborators after disabling one (US062) | Disabled collaborator no longer appears in the list. | `getActiveCollaboratorsOfCompany_usesFindActiveByCompany` + US062 repository filters |
| T5 | Attempt to disable a collaborator without proper authorization | Access denied; error message shown. | `DisableCollaboratorControllerTest.disableCollaboratorFCO_whenUnauthorized_throws` |

Run: `mvn -pl alsafe.core test -Dtest=DisableCollaboratorControllerTest`

---

## 4. Implementation

Key implementation notes:
- **Backoffice UI:** `DisableCollaboratorUI` → `DisableCollaboratorController` (menu **Collaborators > Disable Customer's Collaborator**).
- **Deactivation:** `UserManagementService.deactivateUser(systemUser)` on the linked `SystemUser` (soft delete). The collaborator entity is **not** re-saved (avoids JPA rollback issues).
- **Selection list:** `getActiveCollaboratorsOfCompany` uses `findActiveByCompany`; `getActiveCollaboratorsOfArea` uses `findActiveByCustomerArea`.
- **FCO vs ATCC:** `disableCollaboratorFCO` / `disableCollaboratorATCC` according to customer type; both check `canBeDisabled()` before deactivating.

### 4.1 Design Changes

| Initial idea | What we implemented | Why |
|---|---|---|
| Disable by updating the collaborator row | `UserManagementService.deactivateUser(systemUser)` only | Soft delete is on the auth user; re-saving `Collaborator` in the same transaction caused JPA `RollbackException`. |
| List candidates with `findAll()` for companies | `findActiveByCompany(company)` | Operators must only pick active ATCCs; aligns with US062 and the acceptance criteria. |
| Same flow for FCO and ATCC | `disableCollaboratorFCO` / `disableCollaboratorATCC` | Mirrors the US061 split; each repository resolves by email. |
| — | `canBeDisabled()` on `CollaboratorATCC` (as on FCO) | Rejects a second disable without calling the user service again. |

---

## 5. Integration / Demonstration

To demonstrate the functionality:

1. Run `run-bootstrap.bat`.
2. Run `run-backoffice.bat` and log in as **BackofficeOP**.
3. **6. Collaborators >** **Disable Customer's Collaborator**.
4. Select customer type and customer, then select an active collaborator.
5. Confirm the operation.
6. Repeat **List Customer's Collaborators** (US062) — disabled collaborator must not appear.
7. Attempt login as the disabled collaborator (`run-atcc.bat` for ATCC, or backoffice) — access denied.

---

## 6. Observations

- Complements US062: together they define who appears in backoffice lists and who can still log in.
- Future improvement: audit log (who disabled whom and when).