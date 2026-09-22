# US043 – Consult Weather Data

## 1. Requirements Engineering

### 1.1 User Story Description

> As a **Weather Person**, a **Pilot**, or a **Flight Control Operator**, I want to consult weather data in the system in a given day and in a specific air control area.

---

### 1.2 Acceptance Criteria

- **AC1:** The consultation must filter by **air control area** (selected from existing areas).
- **AC2:** The consultation must allow a **date range** (start date and end date) — all weather data entries within that range for the selected area are returned.
- **AC3:** Both filters (area and date range) are **mandatory**.
- **AC4:** Results must display **wind direction**, **wind speed**, and **date** for each weather data entry.
- **AC5:** If no data matches the criteria, an informative message must be shown.
- **AC6:** Only authenticated users with the **Weather Person**, **Pilot**, or **Flight Control Operator** role may consult weather data.

---

### 1.3 Found Out Dependencies

| Dependency | Description |
|---|---|
| **US041** | Base domain model (`WeatherData`, `WindCondition`, `WeatherDate`) and repositories are reused. |
| **US030** | Authentication and authorisation must be in place — only authenticated users with the appropriate role can perform this action. |
| **US050** | Air control areas must exist before weather data can be associated with them. |

---

### 1.4 Input and Output Data

**Input:**
- Air control area (selected from existing areas)
- Start date of the consultation range
- End date of the consultation range

**Output:**
- List of weather data entries matching the filters (area code, date, wind direction, wind speed)
- Informative message if no data is found for the given criteria
- Error message if the user is not authorised

---

### 1.5 System Sequence Diagram (SSD)

> See `SSD_US043.puml` / `SSD_US043.svg` in this folder.

---

### 1.6 Other Relevant Remarks

- This US consumes data persisted by US041 (single registration) and US042 (bulk import).
- The date range filter provides flexibility to observe weather patterns over multiple days.
- The three actor roles (Weather Person, Pilot, FCO) all have read-only access to weather data, reflecting their operational need for weather information.

---

## 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

The following concepts from the Domain Model are relevant to this US:

- `WeatherData` – the aggregate being queried, holding wind and date information for a given area
- `AirControlArea` – the area the weather data belongs to (exactly one per entry)
- `WindCondition` – value object encapsulating wind direction and wind speed
- `WeatherDate` – value object encapsulating the observation date

No new domain concepts are introduced by this US.

---

### 2.2 New/Modified Classes

| Class | Type | Description |
|---|---|---|
| `ConsultWeatherDataController` | Controller | Coordinates the consultation flow: authorisation check, repository query, result return |
| `WeatherDataRepository.findByAirControlAreaAndDateBetween()` | Repository method (new) | JPQL query filtering by area and date range |

---

## 3. Design

### 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|---|---|---|---|
| 1 | Who initiates the use case? | `WeatherPerson` / `Pilot` / `FCO` via `ConsultWeatherDataUI` | Follows MVC; UI layer is responsible for interaction only. |
| 2 | How are available air control areas presented? | `ConsultWeatherDataController` queries `AirControlAreaRepository.findAll()` | Same pattern as US041; user selects from a list. |
| 3 | How is the date range handled? | UI reads start and end dates via `Console.readCalendar()`; controller passes both to the repository | `BETWEEN` JPQL clause handles range filtering at the database level. |
| 4 | Where is the query executed? | `WeatherDataRepository.findByAirControlAreaAndDateBetween()` — new method in the repository | Keeps domain logic decoupled from persistence. |
| 5 | How is authorisation enforced? | The controller verifies the authenticated user's role via `AuthorizationService` before proceeding | Only users with `WEATHER_PERSON`, `PILOT`, or `FCO` roles may access this functionality. |
| 6 | How are results presented? | The UI iterates over the returned `Iterable<WeatherData>` and displays each entry | No DTO needed — `WeatherData` already exposes data via getters; `@ManyToOne` on `airControlArea` is EAGER by default, avoiding lazy loading issues. |

---

### 3.2 Sequence Diagram

> See `SD_US043.puml` / `SD_US043.svg` in this folder.

### 3.3 Applied Design Patterns

- **MVC** – `ConsultWeatherDataUI` → `ConsultWeatherDataController` → Repository/Domain
- **Repository** – `WeatherDataRepository.findByAirControlAreaAndDateBetween()`
- **Value Object** – `WindCondition` (direction + speed), `WeatherDate` (existing)
- **Aggregate Root** – `WeatherData` (existing)

### 3.4 Tests

| Test ID | Test Description | Expected Result |
|---|---|---|
| T1 | Consult weather data with valid area and date range that has matching entries | List of `WeatherData` entries returned |
| T2 | Consult weather data with valid area and date range with no matching entries | Empty list returned; informative message shown |
| T3 | Consult weather data with a null area | `IllegalArgumentException` thrown |
| T4 | Consult weather data with a null date range | `IllegalArgumentException` thrown |
| T5 | Consult weather data as a user without any of the required roles | `SecurityException` thrown |
| T6 | Consult weather data as a Weather Person (valid) | Results returned successfully |
| T7 | Consult weather data as a Pilot (valid) | Results returned successfully |
| T8 | Consult weather data as an FCO (valid) | Results returned successfully |

---

## 4. Implementation

### Package structure

```
weather/
├── application/
│   └── ConsultWeatherDataController.java    @UseCaseController
├── domain/
│   ├── WeatherData.java                    aggregate root (existente)
│   ├── WeatherDataBuilder.java             builder (existente)
│   ├── WindCondition.java                  VO (existente)
│   └── WeatherDate.java                    VO (existente)
└── repositories/
    └── WeatherDataRepository.java          + findByAirControlAreaAndDateBetween()
```

### Repository addition

- `WeatherDataRepository.findByAirControlAreaAndDateBetween(AirControlArea area, Calendar startDate, Calendar endDate)` — new query method.
- JPA implementation: `match("e.airControlArea = :area AND e.weatherDate.date BETWEEN :start AND :end", params)`.
- The `BETWEEN` clause ensures all entries within the date range are returned, regardless of the time component stored in `WeatherDate.date`.

### Controller

- `ConsultWeatherDataController` follows the same pattern as `RegisterWeatherDataController`:
  - Two constructors: no-arg (production, uses `PersistenceContext`) and parameterised (tests, receives fakes).
  - `consultWeatherData(AirControlArea area, Calendar startDate, Calendar endDate)` — validates params, checks authorisation, delegates to repository, returns results.
- Authorisation check: `authz.ensureAuthenticatedUserHasAnyOf(WEATHER_PERSON, PILOT, FCO)`.

### UI and Menu

- `ConsultWeatherDataUI` extends `AbstractUI`:
  1. Fetches available air control areas via controller
  2. Shows `SelectWidget<AirControlArea>` for area selection
  3. Reads start and end dates via `Console.readCalendar()`
  4. Calls `controller.consultWeatherData(area, startDate, endDate)`
  5. Iterates over results and displays each entry
- `ConsultWeatherDataAction` wraps the UI.
- `WeatherMenuAction` updated with option 3: "Consult Weather Data".

---

## 5. Integration / Demonstration

The consultation will be demonstrated by:
1. Logging in as a user with `WEATHER_PERSON`, `PILOT`, or `FCO` role.
2. Selecting "Weather > Consult Weather Data" from the menu.
3. Selecting an air control area and entering a date range.
4. Observing the list of matching weather data entries.
5. Verifying that no-data and unauthorised scenarios produce appropriate messages.

---

## 6. Observations

- The date range approach (`BETWEEN`) was chosen over a single-day filter to provide greater flexibility, as weather patterns are often analysed over multiple days.
- No new domain objects, value objects, or DTOs are introduced — the existing domain model is sufficient for read-only consultation.
- The query uses `e.weatherDate.date BETWEEN :start AND :end` to navigate into the embedded `WeatherDate` value object's `Calendar` field.
- All three roles (Weather Person, Pilot, FCO) have read-only access; no distinction is made between them at the controller level since the use case is identical for all.
