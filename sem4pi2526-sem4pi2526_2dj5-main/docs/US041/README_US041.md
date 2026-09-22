# US041 – Register Weather Data

## 1. Requirements Engineering

### 1.1 User Story Description

> As a **Weather Person**, I want to register weather data in the system for a specific air control area.

---

### 1.2 Acceptance Criteria

- **AC1:** The weather data must be associated with **exactly one** air control area.
- **AC2:** The air control area selected must **already exist** in the system.
- **AC3:** The weather data must include a **valid date** (past or present; no future dates).
- **AC4:** **Wind direction** must be expressed as an angle in degrees relative to North (0–360°).
- **AC5:** **Wind speed** must be a non-negative value expressed in m/s.
- **AC6:** Only authenticated users with the **Weather Person** role may register weather data.

---

### 1.3 Found Out Dependencies

| Dependency | Description |
|---|---|
| **US030** | Authentication and authorisation must be in place — only a Weather Person can perform this action. |
| **US050** | An air control area must already exist before weather data can be associated with it. |

---

### 1.4 Input and Output Data

**Input:**
- Air control area (selected from existing areas)
- Date of the weather observation
- Wind direction (degrees relative to North, 0–360°)
- Wind speed (m/s)

**Output:**
- Confirmation of successful registration with the weather data details
- Error message if the selected air control area does not exist, or if any value is invalid

---

### 1.5 System Sequence Diagram (SSD)

> See `SSD_US041.puml` / `SSD_US041.svg` in this folder.

---

### 1.6 Other Relevant Remarks

- Weather data is scoped to an air control area and a specific date; multiple entries for the same area on different dates are allowed.
- Wind direction and speed are the primary weather attributes required for flight simulation (see section 3.2 of the requirements — segments include wind direction and speed).
- This US provides the foundation for US042 (bulk import) and US043 (consult), which reuse the same domain model.

---

## 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

The following concepts from the Domain Model are relevant to this US:

- `WeatherData` – the aggregate being created, holding wind and date information for a given area
- `AirControlArea` – the area the weather data belongs to (exactly one per entry)
- `WindCondition` – value object encapsulating wind direction and wind speed
- `WeatherDate` – value object encapsulating the observation date

---

## 3. Design

### 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|---|---|---|---|
| 1 | Who initiates the use case? | `WeatherPerson` via `RegisterWeatherDataUI` | Follows MVC; UI layer is responsible for interaction only. |
| 2 | How are available air control areas presented? | `RegisterWeatherDataController` queries `AirControlAreaRepository` | Repository pattern; keeps domain logic decoupled from persistence. |
| 3 | Where is domain validation enforced? | `WindCondition` and `WeatherDate` value objects validate on construction | Encapsulates validation within value objects; fails fast. |
| 4 | Who creates the `WeatherData` aggregate? | `WeatherDataRepository` delegates to `WeatherData` constructor | The aggregate root enforces its own invariants on creation. |
| 5 | How is authorisation enforced? | The UI/Controller layer checks the authenticated user's role before proceeding | Only users with the `WEATHER_PERSON` role may access this functionality. |

---

### 3.2 Sequence Diagram

> See `SD_US041.puml` / `SD_US041.svg` in this folder.

---

### 3.3 Applied Design Patterns

- **MVC** – `RegisterWeatherDataUI` → `RegisterWeatherDataController` → Domain/Repository
- **Repository** – `WeatherDataRepository`, `AirControlAreaRepository`
- **Value Object** – `WindCondition` (direction + speed), `WeatherDate`
- **Aggregate Root** – `WeatherData` enforces invariants (valid wind values, valid date, mandatory area)

---

### 3.4 Tests

| Test ID | Test Description | Expected Result |
|---|---|---|
| T1 | Register weather data with all valid inputs | WeatherData created and persisted; success message shown. |
| T2 | Register weather data for a non-existent air control area | Registration rejected; area not found error. |
| T3 | Register weather data with an invalid wind direction (e.g. 400°) | Registration rejected; wind direction validation error. |
| T4 | Register weather data with a negative wind speed | Registration rejected; wind speed validation error. |
| T5 | Register weather data with a future date | Registration rejected; date validation error. |
| T6 | Register weather data as a user without the Weather Person role | Registration rejected; authorisation error. |
| T7 | Register multiple weather entries for the same area on different dates | All entries created and persisted successfully. |

---

## 4. Implementation

> _To be completed during Sprint implementation._

Key implementation notes:
- `WindCondition` should be a value object validating direction ∈ [0, 360] and speed ≥ 0.
- `WeatherDate` should be a value object rejecting future dates.
- The `WeatherData` aggregate constructor must receive validated value objects, not raw primitives.
- The controller must verify the authenticated user holds the `WEATHER_PERSON` role before proceeding.

---

## 5. Integration / Demonstration

> _To be completed after integration._

---
