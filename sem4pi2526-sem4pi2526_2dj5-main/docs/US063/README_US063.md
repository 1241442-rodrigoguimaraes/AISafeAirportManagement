# US063 – Edit a Customer’s Collaborator

## 1. Requirements Engineering

### 1.1 User Story Description

> As a **Backoffice Operator**, I want to edit the information (email and phone number) of a customer’s collaborator. All other information cannot be changed.

---

### 1.2 Acceptance Criteria

* **AC1:** The system must only allow editing the **email** and the **phone number**.
* **AC2:** All other data (such as name, role, or associated customer) must remain immutable.
* **AC3:** The system must validate if the new email follows a valid format.
* **AC4:** The system must ensure the collaborator being edited exists and is currently associated with a customer.

---

### 1.3 Found Out Dependencies

| Dependency | Description |
|------------|-------------|
| **US060** | Customer (Air Transport Company or Air Control Area) must exist. |
| **US061** | Collaborator registered and linked to a customer. |
| **US062** | Listing collaborators helps locate the person to edit (separate menu action). |

---

### 1.4 Input and Output Data

**Input:**
- Customer type (Area or Company).
- Customer instance (selected from list).
- Collaborator (selected from customer’s collaborators).
- New email and new phone (both required on save).

**Output:**
- Updated `CollaboratorFCO` or `CollaboratorATCC` persisted.
- Error message on invalid email/phone or duplicate email.

---

### 1.6 Other Relevant Remarks

* Contact data lives on the **collaborator aggregate** (`CollaboratorEmail`, `CollaboratorPhone`), not on `AlSafeUser` directly in this flow.
* Two collaborator types: **FCO** (area) and **ATCC** (company).

---

## 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

* **`CollaboratorFCO`** — collaborator of an `AirControlArea`.
* **`CollaboratorATCC`** — collaborator of an `AirTransportCompany`.
* **`CollaboratorEmail`** / **`CollaboratorPhone`** — value objects with validation in `valueOf(...)`.
* **`EditCollaboratorController`** — orchestrates selection repositories and updates.

---

## 3. Design

### 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|----------------|----------|--------|---------------|
| 1 | Who initiates? | `BackofficeOperator` via `EditCollaboratorUI` | **Collaborators > Edit Collaborator**. |
| 2 | How is collaborator identified? | `SelectWidget` after choosing customer type and customer | No free-text email lookup. |
| 3 | How is consistency maintained? | `changeEmailAndPhone` on `CollaboratorFCO` / `CollaboratorATCC` | Only email and phone fields are mutable (AC1–AC2). |
| 4 | Duplicate email? | `findByEmail` on respective repository before save | Prevents two collaborators sharing the same email. |
| 5 | Authorization | `BACKOFFICE_OPERATOR`, `ADMIN` | Enforced on every controller method. |

---

### 3.2 Sequence Diagram

> See `SD_US063.puml` / `SD_US063.svg` in this folder.

---

### 3.3 Applied Design Patterns

* **MVC** — `EditCollaboratorUI` → `EditCollaboratorController` → domain/repositories.
* **Repository** — `CollaboratorRepositoryFCO`, `CollaboratorRepositoryATCC`, plus area/company repositories for selection.
* **Value Object** — `CollaboratorEmail`, `CollaboratorPhone` validate format on construction.

---

### 3.4 Tests

| Test ID | Test Description | Expected Result | Automated test |
|---------|------------------|-----------------|----------------|
| T1 | Valid email and phone update (FCO) | Saved collaborator with new contact data. | `EditCollaboratorControllerTest`, `CollaboratorFCOTest` |
| T2 | Valid update (ATCC) | Same for company collaborator. | `CollaboratorATCCTest` |
| T3 | Invalid email / phone | `IllegalArgumentException` from value objects. | Domain tests |
| T4 | Duplicate email | `IllegalArgumentException` from controller. | `EditCollaboratorControllerTest` |
| T5 | Unauthorized | `SecurityException`; no save. | `EditCollaboratorControllerTest` |

Run: `mvn -pl alsafe.core test -Dtest=EditCollaboratorControllerTest,CollaboratorFCOTest,CollaboratorATCCTest`

---

## 4. Implementation

Key implementation notes:

* **UI:** `EditCollaboratorUI` — branch `editFCO()` / `editATCC()` after customer-type selection (same order as Add/List/Disable collaborator menus).
* **FCO path:** `getAirControlAreas()` → `activeCollaboratorsOfArea(area)` → `updateEmailAndPhoneFCO`.
* **ATCC path:** `getAirTransportCompanies()` → `activeCollaboratorsOfCompany(company)` → `updateEmailAndPhoneATCC`.
* **Domain:** `changeEmailAndPhone` requires non-null email and phone; name, area/company link and `SystemUser` are unchanged.
* **Persistence:** `collaboratorRepositoryFCO.save` / `collaboratorRepositoryATCC.save`.

---

## 5. Integration / Demonstration

1. Run `run-backoffice.bat` as **BackofficeOP**.
2. **Collaborators > Edit Collaborator**.
3. Choose **Air Control Area** or **Air Transport Company**, then customer, then collaborator.
4. Enter new email and phone; confirm success message.
5. **Collaborators > List Customer's Collaborators** — verify updated contact data.

---

## 6. Observations

* `activeCollaboratorsOfCompany` currently uses `findAll()` on ATCC repository (not filtered by selected company in the query); UI still selects within the shown list.
* Optional fields from the US text are implemented as **both** email and phone required on each edit.

---

## 7. Initial plan vs actual implementation

| Topic | Initially planned | Actually implemented | Reason / note |
|-------|-------------------|----------------------|---------------|
| Aggregate updated | **`AlSafeUser`** with `updateContactInfo` | **`CollaboratorFCO`** / **`CollaboratorATCC`** with **`changeEmailAndPhone`** | US061 split collaborators by customer type; contact data is on the collaborator entity. |
| Lookup | `findByEmail` on user repository | **Select customer type → customer → collaborator** (`SelectWidget`) | Consistent with US062 list/add/disable flows; avoids mistyping email. |
| Single collaborator type | One generic `Collaborator` entity | **FCO** and **ATCC** repositories and update methods | Reflects domain model (area vs company customers). |
| Phone storage | On `AlSafeUser` aggregate | **`CollaboratorPhone`** embedded in collaborator | Matches current persistence model for collaborators. |
| Validation | Syntax check only | **`CollaboratorEmail.valueOf`**, **`CollaboratorPhone.valueOf`** + duplicate email check | AC3 + uniqueness across collaborators. |
| Immutable fields | Read-only in UI | Not sent to controller — only **`changeEmailAndPhone`** mutates state | AC2 enforced in domain method. |
