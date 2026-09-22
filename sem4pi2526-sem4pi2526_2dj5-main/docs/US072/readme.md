# US072a – List Company Fleet by Aircraft Model

## 1. Requirements Engineering

### 1.1 User Story Description

> As an **Air Transport Company Collaborator**, I want to list the company fleet filtered by aircraft model.

---

## 1.2 Acceptance Criteria

- **AC1:** The system must allow the collaborator to select an aircraft model.
- **AC2:** Only aircraft belonging to the selected model should be presented.
- **AC3:** The system must display all aircraft associated with the selected model.

---

## 1.3 Found Out Dependencies

| Dependency | Description |
|------------|-------------|
| Aircraft Management | Aircraft must already exist in the company fleet. |
| Aircraft Model Management | Aircraft models must already exist. |
| Authentication & Authorization | Only authenticated collaborators can execute this use case. |

---

## 1.4 Input and Output Data

### Input
- Aircraft model

### Output
- List of aircraft belonging to the selected aircraft model
- Error message if no aircraft are found

---

## 1.5 Other Relevant Remarks

- This functionality depends on aircraft previously registered in the fleet.
- US072b, US072c and US072d reuse the same architectural structure, interaction flow and implementation approach described in this use case.
- The only difference between these user stories is the filtering criterion applied to the fleet:
    - US072b → aircraft maker;
    - US072c → aircraft capacity;
    - US072d → aircraft age.
- Due to this similarity, the same analysis, design rationale, sequence flow and architectural principles are reused, adapting only the repository filtering operation and corresponding input data.

---

# 2. Analysis

The objective of this use case is to allow collaborators to consult the company fleet filtered by aircraft model.

Important analysis decisions:

1. **Fleet Filtering**
    - Aircraft are filtered according to their associated aircraft model.

2. **Repository Access**
    - The filtering operation is delegated to the repository layer.

3. **Reuse of Existing Aggregates**
    - The functionality reuses:
        - `Aircraft`
        - `AircraftModel`

4. **Reusability Across Similar User Stories**
    - The same interaction and architectural approach is reused for US072b, US072c and US072d, changing only the filtering criterion used in the repository query.

---

# 3. Design

The implementation follows the Onion Architecture and DDD principles.

---

## 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|----------------|----------|--------|---------------|
| 1 | Who initiates the use case? | `AirTransportCompanyCollaborator` through `ListFleetUI` | Follows MVC separation of concerns. |
| 2 | How are aircraft models retrieved? | `ListFleetController` queries `AircraftModelRepository` | Repository abstracts persistence details. |
| 3 | How is fleet filtering performed? | `AircraftRepository.findByAircraftModel()` | Encapsulates filtering logic in persistence layer. |
| 4 | What is presented to the user? | The filtered fleet | Supports operational fleet consultation. |
| 5 | How are similar fleet filtering user stories supported? | Through reusable repository filtering methods | Promotes maintainability and avoids duplicated interaction flows. |

---

## 3.2 Sequence Diagram

### Fleet Listing by Aircraft Model
> See `SD_US072a.puml` / `SD_US072a.svg`

The same sequence structure is reused in:
- US072b – filtering by maker;
- US072c – filtering by capacity;
- US072d – filtering by aircraft age.

Only the filtering parameter and repository method differ.

---

## 3.3 Applied Design Patterns

- **MVC**
    - `ListFleetUI`
    - `ListFleetController`

- **Repository**
    - `AircraftRepository`
    - `AircraftModelRepository`

- **Aggregate Root**
    - `Aircraft`
    - `AircraftModel`

- **Reuse Through Similar Use Case Structure**
    - The same controller/UI interaction pattern is reused for US072b, US072c and US072d.

---

## 3.4 Tests

- Validate aircraft retrieval by aircraft model.
- Validate behavior when no aircraft exist for the selected model.
- Validate repository filtering operations.
- Validate reuse of the same filtering structure for equivalent fleet consultation use cases.

---

# 4. Implementation

## Main Implemented Classes

### Presentation Layer
- `ListFleetUI`

### Application Layer
- `ListFleetController`

### Domain Layer
- `Aircraft`
- `AircraftModel`

### Persistence Layer
- `AircraftRepository`
- `AircraftModelRepository`

The same implementation structure is reused in:
- US072b
- US072c
- US072d

with changes only in the repository filtering methods and input criteria.

---

# 5. Integration / Demonstration

To demonstrate the functionality:

1. Build the project.
2. Execute the bootstrap application.
3. Run the application.
4. Authenticate as a collaborator.
5. Navigate to Fleet Management.
6. Select an aircraft model.
7. Verify the filtered fleet listing.

The same execution flow applies to:
- US072b – filtering by maker;
- US072c – filtering by capacity;
- US072d – filtering by aircraft age.