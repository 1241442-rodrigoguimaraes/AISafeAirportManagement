# US061 – Add a Customer's Collaborator

## 1. Requirements Engineering

### 1.1 User Story Description

> As a **Backoffice Operator**, I want to register a customer's collaborator.
>
> A customer may be an air transport company or an air control area. The collaborator will also be a user of the system. Thus, each collaborator will be a distinct system user. There is no need to verify that the collaborator's email is in the customer's domain.
>
> This must also be achievable by a bootstrap process.

---

### 1.2 Acceptance Criteria

- **AC1:** The collaborator must be associated with **exactly one** customer (either an air transport company or an air control area).
- **AC2:** Each collaborator must be a **distinct system user** — a new `SystemUser` is created for each collaborator.
- **AC3:** The collaborator's **email does not need to belong** to the customer's domain.
- **AC4:** The collaborator's **email must be unique** in the system (enforced at `SystemUser` level).
- **AC5:** The collaborator must be assigned a **role** appropriate to the customer type:
  - Air Transport Company → role `ATCC` (Air Transport Company Collaborator)
  - Air Control Area → role `FCO` (Flight Control Operator)
- **AC6:** The registration must also be achievable via a **bootstrap process**.

---

### 1.3 Found Out Dependencies

| Dependency | Description |
|---|---|
| **US030** | Authentication and authorisation must be in place — only a Backoffice Operator can perform this action. |
| **US050** | An air control area must already exist before a collaborator can be associated with it. |
| **US060** | An air transport company must already exist before a collaborator can be associated with it. |

---

### 1.4 Input and Output Data

**Input:**
- Customer type (Air Transport Company or Air Control Area)
- Customer (selected from existing customers of the chosen type)
- Collaborator name
- Collaborator email
- Collaborator phone number
- Collaborator position/role description

**Output:**
- Confirmation of successful registration with collaborator details
- Error message if email already exists in the system, or the selected customer does not exist

---

### 1.5 System Sequence Diagram (SSD)

> See `SSD_US061.puml` / `SSD_US061.svg` in this folder.

---

### 1.6 Other Relevant Remarks

- The collaborator's email is their system username and must be globally unique.
- The same person cannot be a collaborator of two different customers simultaneously (one user = one collaborator entry).
- The bootstrap process should accept a structured file and apply the same validation rules as the interactive flow.
- Customer type determines which role is assigned to the new system user.

---

## 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

The following concepts from the Domain Model are relevant to this US:

- `Collaborator` – the entity being created, associated with a customer and a system user
- `AirTransportCompany` – one possible customer type
- `AirControlArea` – the other possible customer type
- `SystemUser` – the system user created for the collaborator (from eapli framework)

---

## 3. Design

### 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|---|---|---|---|
| 1 | Who initiates the use case? | `BackofficeOperator` via `AddCollaboratorUI` | Follows MVC; UI layer is responsible for interaction only. |
| 2 | How are customers presented to the operator? | Controller queries both `AirTransportCompanyRepository` and `AirControlAreaRepository` | Both customer types must be available for selection. |
| 3 | How is email uniqueness enforced? | eapli's `UserManagementService` or `SystemUserRepository` checks for existing username | Email is the username; uniqueness is enforced at system user level. |
| 4 | Who creates the `SystemUser`? | `UserBuilderHelper` / `UserManagementService` from eapli framework | Reuses existing user management infrastructure. |
| 5 | Who creates the `Collaborator`? | `AddCollaboratorController` delegates to `Collaborator` constructor | The aggregate root enforces its own invariants on creation. |
| 6 | How is the role determined? | Controller maps customer type to the corresponding system role | `ATCC` for air transport company, `FCO` for air control area. |
| 7 | How is the bootstrap process handled? | A `BootstrapService` reads a data file and calls the same controller | Reuses the same application-layer logic; avoids duplication. |

---

### 3.2 Sequence Diagram

> See `SD_US061.puml` / `SD_US061.svg` in this folder.

---

### 3.3 Applied Design Patterns

- **MVC** – `AddCollaboratorUI` → `AddCollaboratorController` → Domain/Repository
- **Repository** – `CollaboratorRepository`, `AirTransportCompanyRepository`, `AirControlAreaRepository`
- **Value Object** – `EmailAddress`, `PhoneNumber`, `CollaboratorName`
- **Aggregate Root** – `Collaborator` enforces invariants (mandatory customer, mandatory system user)
- **Factory / Builder** – eapli's `UserBuilderHelper` for creating `SystemUser`
- **Bootstrap** – reuses the controller layer for data initialisation at startup

---

### 3.4 Tests

| Test ID | Test Description | Expected Result |
|---|---|---|
| T1 | Register collaborator for an air transport company with all valid data | Collaborator and system user created; success message shown. |
| T2 | Register collaborator for an air control area with all valid data | Collaborator and system user created with FCO role; success message shown. |
| T3 | Register collaborator with an email already in use | Registration rejected; duplicate email error message. |
| T4 | Register collaborator with a non-existent customer | Registration rejected; customer not found error. |
| T5 | Register collaborator with an invalid email format | Registration rejected; email format validation error. |
| T6 | Register collaborator with a null or blank name | Registration rejected; name validation error. |
| T7 | Bootstrap process with valid data file | All collaborators in the file are created successfully. |
| T8 | Bootstrap process with one invalid entry (duplicate email) | Invalid entry is skipped/reported; valid entries are still persisted. |

---

## 4. Implementation

> _To be completed during Sprint implementation._

Key implementation notes:
- `EmailAddress` and `PhoneNumber` should be value objects with format validation.
- The `Collaborator` aggregate constructor must receive a valid `SystemUser` and a customer reference.
- The customer reference should be stored as a typed association (not a generic string), so that the domain relationship is explicit.
- The role assigned to the `SystemUser` must reflect the customer type (`ATCC` or `FCO`).
- Bootstrap uses `AddCollaboratorController` directly, bypassing the UI layer.

---

## 5. Integration / Demonstration

> _To be completed after integration._

---
