# US058 – Remove Engine Model from Aircraft Model

## 1. Requirements Engineering

### 1.1 User Story Description

> As a **Backoffice Operator**, I want to remove an engine model from an aircraft model’s list of certified engines.
>
> This is not possible if there are actual aircrafts using that model. Aircraft model must retain at least one certified engine model.

---

### 1.2 Acceptance Criteria

- **AC1:** The selected engine model must belong to the aircraft model’s certified engine list.
- **AC2:** The aircraft model must retain at least one certified engine after the removal.
- **AC3:** Only `Backoffice Operators` can execute this functionality.
- **AC4:** The aircraft model must be persisted after the engine removal.
- **AC5:** The removal must not be allowed if aircraft instances are using that aircraft model.

---

### 1.3 Found Out Dependencies

| Dependency                     | Description                                                                       |
|--------------------------------|-----------------------------------------------------------------------------------|
| Aircraft Model Management      | An `aircraftModel` must already exist.                                            |
| Engine Model Management        | The selected `engineModel` must exist and be certified in the aircraft model.     |
| Aircraft Management            | Required to fully verify whether aircraft instances are using the aircraft model. |
| Authentication & Authorization | Only `Backoffice Operators` can execute this functionality.                       |

---

### 1.4 Input and Output Data

**Input:**
- Aircraft model
- Engine model to remove

**Output:**
- Confirmation of successful removal
- Error message if:
    - the aircraft model does not exist;
    - the engine model is not certified in that aircraft model;
    - the aircraft model would be left without certified engines;
    - aircraft instances are using that aircraft model.


---

### 1.5 Other Relevant Remarks

- The invariant that an aircraft model must retain at least one certified engine is enforced inside the `aircraftModel` aggregate.
- The validation regarding existing aircraft instances depends on the future existence of the `Aircraft` aggregate/repository.

---

# 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

The following concepts are relevant to this US:

- `aircraftModel` – aggregate root whose certified engine list is modified.
- `engineModel` – engine model to be removed from the certified list.
- `aircraftModelRepository` – persists the updated aggregate.
- `RemoveEngineModelFromAircraftModelController` – coordinates the use case.

---

# 3. Design

## 3.1 Rationale

| Interaction ID | Question                                | Answer                                                              | Justification                               |
|----------------|-----------------------------------------|---------------------------------------------------------------------|---------------------------------------------|
| 1              | Who initiates the use case?             | `BackofficeOperator` through `RemoveEngineModelFromAircraftModelUI` | Follows MVC principles.                     |
| 2              | How are aircraft models obtained?       | Controller queries `aircraftModelRepository.findAll()`              | Repository abstracts persistence.           |
| 3              | Where is the removal rule enforced?     | `aircraftModel.removeCertifiedEngine()`                             | Aggregate protects its invariant.           |
| 4              | Who persists the update?                | `aircraftModelRepository.save()`                                    | Repository stores the changed aggregate.    |
| 5              | How is the aircraft usage rule handled? | Future validation through Aircraft repository                       | Depends on Aircraft aggregate availability. |

---

## 3.2 Sequence Diagram

> See `SD- Remove an engine model.puml` / `SD- Remove an engine model.svg`.

---

## 3.3 Applied Design Patterns

- **MVC**
- **Repository**
- **Aggregate Root**
- **Domain Invariant**

---

## 3.4 Tests

---

# 4. Implementation

> The implementation follows DDD and Onion Architecture principles.

Key implementation aspects:
- The controller coordinates the removal process.
- The aggregate method `removeCertifiedEngine()` enforces that at least one certified engine remains.
- The UI validates user selections before calling the controller.
- The repository persists the updated aircraft model.

---

# 5. Integration / Demonstration

> The functionality was successfully integrated into the Backoffice application and tested through:
>
> - aircraft model selection;
> - certified engine selection;
> - successful engine removal;
> - prevention of removing the last certified engine.

---

# 6. Observations

- The rule that prevents removal when actual aircraft instances are using the aircraft model could not be fully implemented yet because the `Aircraft` aggregate/repository is not currently available in the implemented domain.
- The implementation was prepared so that this validation can be added later in the controller before calling the aggregate removal method.