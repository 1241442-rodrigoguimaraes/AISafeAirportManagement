# US053 – Create an Aircraft Engine Model

## 1. Requirements Engineering

### 1.1 User Story Description

> As a **Backoffice Operator**, I want to register a new engine model to be used by aircrafts.  
> The model’s name and manufacturer combination must be unique. Other important information regarding the type, power, fuel, and efficiency should be included.
>
> This must also be achievable by a bootstrap process.

---

## 1.2 Acceptance Criteria

- **AC1:** The combination of engine model name and maker must be unique.
- **AC2:** Every engine model must define:
    - engine type;
    - power;
    - fuel type;
    - efficiency.
- **AC3:** Engine type and fuel type must belong to predefined valid values.
- **AC4:** Numeric values such as power and efficiency must be valid positive values.
- **AC5:** The registration must be executable through both:
    - the Backoffice UI;
    - a bootstrap process.

---

## 1.3 Found Out Dependencies

| Dependency                     | Description                                                          |
|--------------------------------|----------------------------------------------------------------------|
| Maker Management               | A `Maker` must already exist before an engine model can be created.  |
| Authentication & Authorization | Only authenticated `Backoffice Operators` can execute this use case. |

---

## 1.4 Input and Output Data

### Input
- Engine model name
- Maker
- Engine type
- Engine power
- Fuel type
- Engine efficiency

### Output
- Confirmation message of successful engine model creation
- Error message if:
    - the engine model already exists;
    - invalid values are introduced;
    - the selected maker does not exist.



---

## 1.5 Other Relevant Remarks

- The uniqueness rule is enforced through the aggregate identity (`engineModelId`), composed of:
    - engine model name;
    - maker name.
- Enumerations were used for:
    - `engineType`
    - `engineModelFuel`
- Bootstrap initialization uses the same domain validation rules as the interactive process.

---

# 2. Analysis

The registration of an aircraft engine model introduces a new aggregate responsible for representing certified aircraft engines.

Important analysis decisions:

1. **Engine Model Identity**
    - The combination of model name and maker uniquely identifies an engine model.
    - This identity is represented by the `engineModelId` Value Object.

2. **Engine Characteristics**
    - Each engine model contains:
        - engine type;
        - power;
        - fuel type;
        - efficiency.

3. **Enumerated Types**
    - Engine type and fuel type were modeled using Java Enumerations to restrict values to valid domain concepts.

4. **Validation**
    - Power and efficiency must be positive values.
    - Domain validation is enforced directly inside the Value Objects and aggregate constructor.

5. **Bootstrap Support**
    - The system supports automatic initialization of engine models during startup through a dedicated bootstrapper.

---

# 3. Design

The implementation follows the Onion Architecture and DDD principles.

---

## 3.1 Rationale

| Interaction ID | Question                                  | Answer                                                   | Justification                                       |
|----------------|-------------------------------------------|----------------------------------------------------------|-----------------------------------------------------|
| 1              | Who initiates the use case?               | `BackofficeOperator` through `CreateEngineModelUI`       | Follows MVC separation of concerns.                 |
| 2              | How are makers retrieved?                 | `CreateEngineModelController` queries `MakersRepository` | Repository pattern abstracts persistence.           |
| 3              | Where is uniqueness enforced?             | `engineModelRepository.ofIdentity()`                     | Identity-based validation ensures uniqueness.       |
| 4              | Who validates domain invariants?          | `engineModel` aggregate and Value Objects                | Domain layer protects consistency.                  |
| 5              | How are valid engine/fuel types enforced? | Java Enumerations                                        | Restricts values to predefined valid domain values. |
| 6              | How is bootstrap supported?               | `EngineModelBootstrapper`                                | Enables automatic initialization at startup.        |

---

## 3.2 Sequence Diagram

### Manual Registration
> See `SD- Create engine model.puml` / `SD- Create engine model.svg`

### Bootstrap Registration
> See `SD- Create engine model bootsrap.puml` / `SD- Create engine model bootsrap.svg`

---

## 3.3 Applied Design Patterns

- **MVC**
    - `CreateEngineModelUI`
    - `CreateEngineModelController`

- **Repository**
    - `engineModelRepository`
    - `MakersRepository`

- **Aggregate Root**
    - `engineModel`

- **Value Objects**
    - `engineModelId`
    - `engineModelName`
    - `engineModelPower`
    - `engineModelEfficiency`

- **Enumeration**
    - `engineType`
    - `engineModelFuel`

- **Bootstrap Pattern**
    - `EngineModelBootstrapper`

---

## 3.4 Tests

---

# 4. Implementation

## Main Implemented Classes

### Presentation Layer
- `CreateEngineModelUI`
- `CreateEngineModelAction`

### Application Layer
- `CreateEngineModelController`

### Domain Layer
- `engineModel`
- `engineModelId`
- `engineModelName`
- `engineModelPower`
- `engineModelEfficiency`
- `engineType`
- `engineModelFuel`

### Persistence Layer
- `engineModelRepository`
- `JpaEngineModelRepository`
- `InMemoryEngineModelRepository`

### Infrastructure
- `EngineModelBootstrapper`

---

# 5. Integration / Demonstration

To demonstrate the functionality:

1. Build the project.
2. Execute the bootstrap application.
3. Run the Backoffice application.
4. Authenticate as a Backoffice Operator.
5. Navigate to Engine Model Management.
6. Register a new engine model.
7. Verify:
    - successful persistence;
    - duplicate prevention;
    - validation rules.

