# US055 – Create Aircraft Model

## 1. Requirements Engineering

### 1.1 User Story Description

> As a **Backoffice Operator**, I want to register a new aircraft model with its maker, aircraft type, maximum range, physics data and certified engine models.
>
> An aircraft model must always contain at least one certified engine model.
>
> This must also be achievable through a bootstrap process.

---

### 1.2 Acceptance Criteria

- **AC1:** The combination of aircraft model name and maker must be unique.
- **AC2:** An aircraft model must always contain at least one certified engine model.
- **AC3:** Physics data values must be valid positive values.
- **AC4:** MTOW cannot be lower than:
    - empty weight;
    - MZFW.
- **AC5:** The aircraft model must always be associated with an existing maker.
- **AC6:** Certified engine models must already exist in the system.
- **AC7:** The functionality must also be supported through bootstrap initialization.

---

### 1.3 Found Out Dependencies

| Dependency                     | Description                                                     |
|--------------------------------|-----------------------------------------------------------------|
| Maker Management               | A `Maker` must already exist before creating an aircraft model. |
| Engine Model Management        | Certified `engineModel` instances must already exist.           |
| Authentication & Authorization | Only `Backoffice Operators` can execute this functionality.     |

---

### 1.4 Input and Output Data

**Input:**
- Aircraft model name
- Maker
- Aircraft type
- Maximum range
- Physics data
- Certified engine models

**Output:**
- Confirmation of successful aircraft model registration
- Error message if:
    - the aircraft model already exists;
    - invalid physics data is introduced;
    - no certified engines are selected;
    - referenced makers or engines do not exist.

---



### 1.5 Other Relevant Remarks

- The aircraft model identity is defined by:
    - aircraft model name;
    - maker.
- Physics-related validations are encapsulated in `aircraftModelPhysicsData`.
- The aggregate guarantees the existence of at least one certified engine model.
- Bootstrap initialization reuses the same domain validation rules.

---

# 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

The following concepts are relevant to this US:

- `aircraftModel` – aggregate root representing an aircraft model.
- `engineModel` – certified engines associated with the aircraft model.
- `Maker` – manufacturer associated with the aircraft model.
- `aircraftModelPhysicsData` – encapsulates aircraft physics-related data.
- `maximumRange` – value object representing aircraft operational range.

---

# 3. Design

## 3.1 Rationale

| Interaction ID | Question                                   | Answer                                               | Justification                            |
|----------------|--------------------------------------------|------------------------------------------------------|------------------------------------------|
| 1              | Who initiates the use case?                | `BackofficeOperator` through `CreateAircraftModelUI` | Follows MVC principles.                  |
| 2              | How are makers and engine models obtained? | `CreateAircraftModelController` queries repositories | Keeps persistence decoupled from the UI. |
| 3              | Where is uniqueness enforced?              | `aircraftModelRepository.ofIdentity()`               | Ensures aggregate uniqueness.            |
| 4              | Who validates business rules?              | `aircraftModel` aggregate and Value Objects          | Protects domain consistency.             |
| 5              | Where are physics validations enforced?    | `aircraftModelPhysicsData`                           | Encapsulates validation logic.           |
| 6              | How is bootstrap handled?                  | `AircraftModelBootstrapper`                          | Enables automatic initialization.        |

---

## 3.2 Sequence Diagram

> See `SD - Aircraft Configuration.puml` / `SD - Aircraft Configuration.svg`.

### Bootstrap Sequence Diagram

> See `SD- Aircraft Configuration bootstrap.puml` / `SD- Aircraft Configuration bootstrap.svg`.

---

## 3.3 Applied Design Patterns

- **MVC**
- **Repository**
- **Aggregate Root**
- **Value Object**
- **Bootstrap Pattern**

---

## 3.4 Tests

---

# 4. Implementation

> The implementation follows DDD and Onion Architecture principles.

Key implementation aspects:
- `aircraftModelId` ensures uniqueness through aircraft model name and maker.
- `aircraftModelPhysicsData` centralizes physics validations.
- The aggregate enforces the existence of at least one certified engine.
- Bootstrap registration uses the same aggregate validation rules.

---

# 5. Integration / Demonstration

> The functionality was successfully integrated into the Backoffice application and tested through:
>
> - interactive UI registration;
> - bootstrap initialization;
> - domain validation rules;
> - certified engine association validation.