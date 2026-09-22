# US052 – Create an Airport

## 1. Requirements Engineering

### 1.1 User Story Description

> As a **Backoffice Operator**, I want to register an airport in a given air control area. An airport must be associated with exactly one air control area. The airport has an ICAO and IATA code that must be unique worldwide. An airport has location coordinates that must be valid and an elevation in meters above sea level.
>
> This must also be achieved by a bootstrap process.

---

### 1.2 Acceptance Criteria

- **AC1:** The airport must be associated with **exactly one** air control area.
- **AC2:** The **ICAO code** (4 letters) must be unique worldwide.
- **AC3:** The **IATA code** (3 letters) must be unique worldwide.
- **AC4:** Location **coordinates** (latitude and longitude) must be valid (lat ∈ [-90, 90], lon ∈ [-180, 180]).
- **AC5:** **Elevation** must be expressed in metres above sea level and must be a non-negative value.
- **AC6:** The airport must also be registerable via a **bootstrap process** (e.g. data file import at startup).

---

### 1.3 Found Out Dependencies

| Dependency | Description |
|---|---|
| **US050** | An air control area must already exist before an airport can be associated with it. |
---

### 1.4 Input and Output Data

**Input:**
- Airport name
- Town / City
- Country
- IATA code (3-letter)
- ICAO code (4-letter)
- Latitude and longitude coordinates
- Elevation (metres above sea level)
- Air control area (selected from existing areas)

**Output:**
- Confirmation of successful registration with the airport details
- Error message if IATA/ICAO codes are not unique, coordinates are invalid, or the selected area does not exist

---

### 1.5 System Sequence Diagram (SSD)

> See `SSD_US052.puml` / `SSD_US052.svg` in this folder.

---

### 1.6 Other Relevant Remarks

- IATA codes follow the format `[A-Z]{3}` and ICAO codes follow `[A-Z]{4}`; format validation must be enforced.
- The bootstrap process should accept a structured file and apply the same validation rules as the interactive flow.
- Country names are subject to change (see section 3.1.2 of the requirements); the domain model should account for this.

---

## 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

The following concepts from the Domain Model are relevant to this US:

- `Airport` – the entity being created, with IATA/ICAO codes, coordinates and elevation
- `AirControlArea` – the area the airport belongs to (exactly one per airport)
- `Coordinates` – value object encapsulating latitude and longitude
---

## 3. Design

### 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|---|---|---|---|
| 1 | Who initiates the use case? | `BackofficeOperator` via `CreateAirportUI` | Follows MVC; UI layer is responsible for interaction only. |
| 2 | How are available air control areas presented? | `CreateAirportController` queries `AirControlAreaRepository` | Repository pattern; keeps domain logic decoupled from persistence. |
| 3 | Where is uniqueness of IATA/ICAO enforced? | `AirportRepository.existsByIataCode()` and `existsByIcaoCode()` | Persistence-level check ensures global uniqueness. |
| 4 | Who creates the `Airport` aggregate? | `AirportRepository` delegates to `Airport` constructor | The aggregate root enforces its own invariants on creation. |
| 5 | How is coordinate validity enforced? | `Coordinates` value object validates on construction | Encapsulates validation within the value object; fails fast. |
| 6 | How is the bootstrap process handled? | A `BootstrapService` reads a data file and calls the same controller | Reuses the same application-layer logic; avoids duplication. |

---

### 3.2 Sequence Diagram

> See `SD_US052.puml` / `SD_US052.svg` in this folder.

---

### 3.3 Applied Design Patterns

- **MVC** – `CreateAirportUI` → `CreateAirportController` → Domain/Repository
- **Repository** – `AirportRepository`, `AirControlAreaRepository`
- **Value Object** – `Coordinates` (latitude + longitude), `IataCode`, `IcaoCode`
- **Aggregate Root** – `Airport` enforces invariants (unique codes, valid coordinates, mandatory area)
- **Bootstrap** – reuses the controller layer for data initialisation at startup

---

### 3.4 Tests

| Test ID | Test Description | Expected Result |
|---|---|---|
| T1 | Register airport with all valid data | Airport created and persisted; success message shown. |
| T2 | Register airport with duplicate IATA code | Registration rejected; informative error message. |
| T3 | Register airport with duplicate ICAO code | Registration rejected; informative error message. |
| T4 | Register airport with invalid latitude (e.g. 95°) | Registration rejected; coordinates validation error. |
| T5 | Register airport with negative elevation | Registration rejected; elevation validation error. |
| T6 | Register airport with non-existent air control area | Registration rejected; area not found error. |
| T7 | Bootstrap process with valid data file | All airports in the file are created successfully. |
| T8 | Bootstrap process with one invalid entry | Invalid entry is skipped/reported; valid entries are still persisted. |

---

## 4. Implementation

> _To be completed during Sprint implementation._

Key implementation notes:
- `IataCode` and `IcaoCode` should be value objects with regex-based format validation.
- `Coordinates` value object should throw a domain exception if values are out of range.
- The `Airport` aggregate constructor should receive validated value objects, not raw primitives.
- Bootstrap uses `CreateAirportController` directly, bypassing the UI layer.

---

## 5. Integration / Demonstration

> _To be completed after integration._

---
