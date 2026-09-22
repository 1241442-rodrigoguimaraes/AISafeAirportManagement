# US061 – Differences Between Planned and Implemented

## 1. Entity Design

| Aspect | Planned | Implemented |
|---|---|---|
| Entity | Single `Collaborator` with customer type discriminator | Two separate entities: `CollaboratorATCC` and `CollaboratorFCO` |
| Customer link | Generic customer reference | `CollaboratorATCC` → `AirTransportCompany`; `CollaboratorFCO` → `AirControlArea` |
| Repository | Single `CollaboratorRepository` | Two repositories: `CollaboratorRepositoryATCC`, `CollaboratorRepositoryFCO` |

## 2. Customer Selection Flow

| Aspect | Planned | Implemented |
|---|---|---|
| Method | `getCustomerTypes()` → `getCustomers(customerType)` | Separate `getAirControlAreas()` and `getAirTransportCompanies()` |
| Flow | Select type, then select customer from filtered list | Both lists returned; UI presents two separate sections |

## 3. Input Fields

| Field | Planned | Implemented |
|---|---|---|
| Password | **No** | Yes (required for `SystemUser` creation) |
| Phone number | No (not in SD) | Yes (`CollaboratorPhone` value object) |

## 4. Email Uniqueness Check

| Aspect | Planned | Implemented |
|---|---|---|
| Explicit `existsByUsername()` | Yes (called before registration) | **No** (relies on `UserManagementService.registerNewUser()` throwing on duplicate) |

## 5. Collaborator Construction

| Aspect | Planned | Implemented |
|---|---|---|
| Pattern | Direct `new Collaborator(...)` constructor call | **Builder pattern**: `CollaboratorATCCBuilder` / `CollaboratorFCOBuilder` implementing `DomainFactory` |
| Fluent API | No | `.with(name, email, phone, systemUser, customer)` + individual `with*()` methods |
| Logging | No | Debug logging in `build()` |

## 6. Authorisation

| Aspect | Planned | Implemented |
|---|---|---|
| Role check | Not shown in SD | `AuthorizationService.ensureAuthenticatedUserHasAnyOf(BACKOFFICE_OPERATOR, ADMIN)` |
| Framework interaction | Absent from SD | Explicit `AuthzRegistry.authorizationService()` used |

## 7. Bootstrap

| Aspect | Planned | Implemented |
|---|---|---|
| Mechanism | Single bootstrap service | Two separate bootstrappers: `CollaboratorATCCBootstrapper`, `CollaboratorFCOBootstrapper` |

---

## Summary of Changes

1. **Split** single `Collaborator` entity into `CollaboratorATCC` and `CollaboratorFCO` with respective repositories
2. **Changed** customer selection from a two-step type-then-list flow to two separate method calls
3. **Added** `password` as input (needed for `SystemUser` creation)
4. **Added** phone number as a required field
5. **Removed** explicit email uniqueness check (relies on framework enforcement)
6. **Added** `AuthorizationService` for role-based access control
7. **Added** `CollaboratorATCCBuilder` and `CollaboratorFCOBuilder` following the project's Builder pattern
8. **Split** bootstrap into two dedicated bootstrappers
