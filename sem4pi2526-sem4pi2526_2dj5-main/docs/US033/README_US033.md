# US033 – List Users

## 1. Requirements Engineering

### 1.1 User Story Description

> As an **Administrator**, I want to be able to list the users of the backoffice, including their status (enabled/disabled).

---

### 1.2 Acceptance Criteria

* **AC1:** The list must display all users registered in the backoffice.
* **AC2:** The system must clearly indicate the status (Active/Disabled) of each user.
* **AC3:** Each entry in the list should include identifying information (Email, Name, Phone Number, and Role).
* **AC4:** The list should include security-related information, such as the security clearance expiration date and the last skills assessment date.

---

### 1.3 Found Out Dependencies

| Dependency | Description |
|------------|-------------|
| **US030** | Authentication and authorization framework in place. |
| **US031** | Users bootstrapped / registered in the system. |
| **US032** | Activate/deactivate changes the status shown in this list. |

---

### 1.4 Input and Output Data

**Input:**
- None (operator opens the list from the menu).

**Output:**
- Console table: email, name, phone, role, status (`Active` / `Disabled`), security clearance expiration, last skills assessment date.

---

### 1.6 Other Relevant Remarks

* Each backoffice user has an **`AlSafeUser`** profile linked to a **`SystemUser`**.
* Status comes from **`SystemUser.isActive()`**; compliance dates from **`AlSafeUser`**.

---

## 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

* **`AlSafeUser`** — aggregate with phone, `SecurityClearance`, `SkillsAssessment`; links to `SystemUser`.
* **`SystemUser`** — email, name, roles, active flag.
* **`UserDTO`** — read model record built in the controller for the UI table.

---

## 3. Design

### 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|----------------|----------|--------|---------------|
| 1 | Who initiates? | Backoffice operator via `ListUsersUI` | Menu: **Users > List all Users**. |
| 2 | Where is listing logic? | `ListUsersController.getUsers()` | Maps domain entities to `UserDTO`. |
| 3 | How are users retrieved? | `AlSafeUserRepository.findAll()` | One row per AlSafe profile in persistence. |
| 4 | Authorization? | `BACKOFFICE_OPERATOR`, `ADMIN` in controller (and UI pre-check) | Consistent with US032. |

---

### 3.2 Sequence Diagram

> See `SD_US033.puml` / `SD_US033.svg` in this folder.

---

### 3.3 Applied Design Patterns

* **MVC** — `ListUsersUI` → `ListUsersController` → repository.
* **DTO** — `UserDTO` avoids exposing aggregates to the console layer.
* **Repository** — `AlSafeUserRepository` for `findAll()`.

---

### 3.4 Tests

| Test ID | Test Description | Expected Result | Automated test |
|---------|------------------|-----------------|----------------|
| T1 | Multiple users | Full list as DTOs with status and dates. | `ListUsersControllerTest` |
| T2 | Empty repository | Empty list returned. | `ListUsersControllerTest` |
| T3 | Status mapping | `isActive()` → `"Active"` / `"Disabled"`. | `ListUsersControllerTest` |
| T4 | Unauthorized | `SecurityException`; no repository call. | `ListUsersControllerTest` |

Run: `mvn -pl alsafe.core test -Dtest=ListUsersControllerTest`

---

## 4. Implementation

Key implementation notes:

* **Controller:** `ListUsersController` — constructor injection for tests; production uses `AuthzRegistry` + `PersistenceContext.repositories().alSafeUsers()`.
* **DTO mapping:** `toDto(AlSafeUser)` reads `SystemUser` for email, name, role (first role label), status; `AlSafeUser` for phone and calendar-based clearance/skills dates converted to `LocalDate`.
* **UI:** `ListUsersUI` prints a fixed-width table; shows authorization message if roles are missing.
* **Extra methods:** `allUsers()` / `find(Username)` on controller delegate to `UserManagementService` (used elsewhere, not by the list UI).

---

## 5. Integration / Demonstration

1. Run `run-backoffice.bat` as **BackofficeOP** or **admin**.
2. **Users > List all Users**.
3. Verify columns: email, name, phone, role, status, clearance expiry, skills assessment date.
4. Deactivate a user (US032) and list again — status must show **Disabled**.

---

## 6. Observations

* Only the **first role** of each user is shown in the role column (single-role assumption in UI).
* Long text is truncated in the table for console width.

---

## 7. Initial plan vs actual implementation

| Topic | Initially planned | Actually implemented | Reason / note |
|-------|-------------------|----------------------|---------------|
| Repository | Generic `UserRepository.findAll()` | **`AlSafeUserRepository.findAll()`** | Backoffice users are `AlSafeUser` aggregates in this project. |
| Status source | `AlSafeUser.isActive()` in a loop | **`SystemUser.isActive()`** mapped to `"Active"` / `"Disabled"` | Active flag belongs to framework `SystemUser`. |
| Return type to UI | Collection of domain entities | **`List<UserDTO>`** | Stable read model for the console table (AC3–AC4). |
| Authorization | Administrator only | **`BACKOFFICE_OPERATOR`** + **`ADMIN`** | Same as US032; backoffice operators manage users daily. |
| UI auth check | Only in controller | Controller **and** `ListUsersUI` pre-check | Fails fast with a clear message before building the table. |
| Security / skills data | Pulled via repeated getter calls on user | **`SecurityClearance`** / **`SkillsAssessment`** on `AlSafeUser`, converted to **`LocalDate`** in DTO | Matches domain model; satisfies AC4. |
