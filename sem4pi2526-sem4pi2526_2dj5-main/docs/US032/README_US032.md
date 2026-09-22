# US032 – Disable/Enable Users of the Backoffice

## 1. Requirements Engineering

### 1.1 User Story Description

> As an **Administrator**, I want to be able to disable/enable users of the backoffice.

---

### 1.2 Acceptance Criteria

* **AC1:** The Administrator must be able to change the status of any backoffice user (except, potentially, their own account to prevent lockout).
* **AC2:** A disabled user must be prevented from authenticating or performing any actions within the system.
* **AC3:** Enabling a previously disabled user must immediately restore their access rights.
* **AC4:** The system must record/update the current status of the user (Active/Disabled).

---

### 1.3 Found Out Dependencies

| Dependency | Description |
|------------|-------------|
| **US031** | Users must be registered in the system before they can be enabled or disabled. |
| **US033** | The operator needs to list users to see who is active or disabled. |
| **NFR09** | Authentication must respect the disabled flag on `SystemUser` during login. |

---

### 1.4 Input and Output Data

**Input:**
- Selection of a user from a numbered list (active users to deactivate, or deactivated users to activate).

**Output:**
- Updated `SystemUser` active flag (via framework `UserManagementService`).
- Console message on success or persistence error.

---

### 1.6 Other Relevant Remarks

* Status is stored on the EAPLI **`SystemUser`** aggregate (`isActive()`), not on a custom method inside `AlSafeUser`.
* Login blocking is handled by the framework authentication layer when `isActive` is false.

---

## 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

* **`SystemUser`** (framework aggregate) — holds the active/disabled flag used at login.
* **`AlSafeUser`** — extended profile (phone, security clearance, skills); listed in US033 but not mutated by US032.
* **`UserManagementService`** — framework service for `activeUsers()`, `deactivatedUsers()`, `activateUser()`, `deactivateUser()`.

---

## 3. Design

### 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|----------------|----------|--------|---------------|
| 1 | Who initiates the use case? | `BackofficeOperator` or `Admin` via `DeactivateUserUI` / `ActivateUserUI` | Implemented roles: `BACKOFFICE_OPERATOR`, `ADMIN`. |
| 2 | How is status changed? | `UserManagementService.deactivateUser` / `activateUser` on `SystemUser` | Reuses EAPLI authz infrastructure (AC2–AC4). |
| 3 | How is the user selected? | Numbered list from `activeUsers()` or `deactivatedUsers()` | Two separate menus instead of a single toggle screen. |
| 4 | Where is authorization enforced? | `ActivateUserController` / `DeactivateUserController` via `AuthorizationService` | Checked on every controller operation. |

---

### 3.2 Sequence Diagram

> See `SD_US032.puml` / `SD_US032.svg` in this folder.

---

### 3.3 Applied Design Patterns

* **MVC** — separate UI/Action pairs per operation; controllers delegate to framework services.
* **Application Service** — `UserManagementService` encapsulates persistence of `SystemUser` status.
* **Repository (framework)** — user data accessed through `AuthzRegistry.userService()`.

---

### 3.4 Tests

| Test ID | Test Description | Expected Result | Automated test |
|---------|------------------|-----------------|----------------|
| T1 | Deactivate an active user | User no longer in `activeUsers()`; `isActive()` false. | `DeactivateUserControllerTest` |
| T2 | Activate a deactivated user | User restored via `activateUser`. | `ActivateUserControllerTest` |
| T3 | Unauthorized caller | `SecurityException`; service not called. | Both controller test classes |
| T4 | List filtering | `deactivatedUsers()` / `activeUsers()` delegated to service. | Controller tests |

Run: `mvn -pl alsafe.core test -Dtest=ActivateUserControllerTest,DeactivateUserControllerTest`

---

## 4. Implementation

Key implementation notes:

* **Menu:** Backoffice → **Users >** → **Deactivate User** / **Activate User** (`UserMenuAction`).
* **Controllers:** `DeactivateUserController`, `ActivateUserController` in `eapli.alsafe.usermanagement.application`.
* **UI:** `DeactivateUserUI`, `ActivateUserUI` in `alsafe.app.backoffice.console.presentation.authz`.
* **Authorization:** `ensureAuthenticatedUserHasAnyOf(Roles.BACKOFFICE_OPERATOR, Roles.ADMIN)`.
* **No** `AlSafeUser.deactivate()` — status lives on `SystemUser` managed by the framework.

---

## 5. Integration / Demonstration

1. Run `run-backoffice.bat` and log in as **BackofficeOP** or **admin**.
2. Open **Users > List all Users** (US033) and note a user’s status.
3. **Users > Deactivate User** — pick an active user by number.
4. Try logging in with that user — access must be denied.
5. **Users > Activate User** — pick the same user from the deactivated list and confirm login works again.

---

## 6. Observations

* Two flows (activate / deactivate) improve clarity compared to a single “toggle” screen.
* A future improvement could block deactivating the currently logged-in administrator to satisfy AC1 explicitly.

---

## 7. Initial plan vs actual implementation

| Topic | Initially planned | Actually implemented | Reason / note |
|-------|-------------------|----------------------|---------------|
| UI | Single `EnableDisableUserUI` with email input and `toggleUserStatus` | **`DeactivateUserUI`** and **`ActivateUserUI`** as separate menu entries | Matches existing EAPLI backoffice patterns (`ActivateUser` / `DeactivateUser` samples). |
| Controller | One `EnableDisableUserController` | **`DeactivateUserController`** + **`ActivateUserController`** | Clear separation; each delegates to one service operation. |
| Domain operation | `AlSafeUser.deactivate()` / `activate()` | **`UserManagementService`** on **`SystemUser`** | Active flag is part of framework authz; login already honours `isActive()`. |
| User lookup | `AlSafeUserRepository.findByEmail(email)` | Lists from **`userSvc.activeUsers()`** / **`deactivatedUsers()`** | Selection by list index in console; no manual email typing. |
| Actor / role | Only **Administrator** | **`BACKOFFICE_OPERATOR`** and **`ADMIN`** | Aligns with other backoffice user-management stories (US033). |
| Confirmation | Explicit Y/N before toggle | Selection by number only (no second confirmation) | Same style as other backoffice list UIs in the project. |
