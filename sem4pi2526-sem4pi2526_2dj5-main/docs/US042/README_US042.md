# US042 – Import Bulk Weather Data

## 1. Requirements Engineering

### 1.1 User Story Description

> As a **Weather Person**, I want to import bulk weather data into the system.

Weather data might be from multiple external weather service providers, so the system can aggregate data from various sources for better accuracy. While a simple CSV file format will be available for initial system development, the system should be easy to expand to new weather data sources.

---

### 1.2 Acceptance Criteria

- **AC1:** The system must accept a CSV file as input and import all valid records contained in it.
- **AC2:** Each CSV line must contain the fields: `airControlAreaCode`, `date`, `windDirection`, `windSpeed`.
- **AC3:** Lines with invalid data (e.g. non-existent air control area, wind direction outside 0–360°, negative wind speed, future date) must be rejected with a detailed error report, while valid lines are still imported.
- **AC4:** Only authenticated users with the **Weather Person** role may execute the bulk import.
- **AC5:** The system must report to the user the number of successfully imported records and the lines that contained errors, with the reason for each failure.
- **AC6:** The import mechanism must be extensible to new data sources (e.g. JSON, XML, REST API) without modifying existing code — a **Strategy** pattern must be used.
- **AC7:** The functionality must be accessible remotely via the TCP client application (see US44).

---

### 1.3 Found Out Dependencies

| Dependency | Description |
|---|---|
| **US041** | Base domain model (`WeatherData`, `WindCondition`, `WeatherDate`) and repositories are reused. |
| **US030** | Authentication and authorisation must be in place — only a `WEATHER_PERSON` can perform this action. |
| **US050** | An air control area must already exist before weather data can be associated with it. |
| **US044** | Remote access for Weather Person — the bulk import must be available through the dedicated TCP client. |

---

### 1.4 Input and Output Data

**Input:**
- A CSV file path (provided by the user)
- The file content with lines following the format: `airControlAreaCode;date(YYYY-MM-DD);windDirection(0-360);windSpeed(m/s)`

**Output:**
- Summary of the import result:
  - Number of records successfully imported
  - List of rejected lines with the error category and reason for each failure
- Error message if the file cannot be read or the user is not authorised

---

### 1.5 System Sequence Diagram (SSD)

> _See `SSD_US042.puml` / `SSD_US042.svg` in this folder._

---

### 1.6 Other Relevant Remarks

- The bulk import operation processes each line independently, allowing partial imports (valid lines are saved even if some lines fail).
- Multiple providers can be registered (e.g. `CsvWeatherDataImportProvider`, `JsonWeatherDataImportProvider`), selected at runtime or via configuration.
- This US builds on the same domain model established in US041 and prepares the ground for US043 (consult weather data), which consumes the same persisted data.
- The file path may be local or remote depending on the deployment context (backoffice console or remote TCP client).
- Errors are categorised via a typed enum (`WeatherImportError.Category`), enabling the UI to group, filter, or internationalise error messages without parsing strings.

---

## 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

The following concepts from the Domain Model are relevant to this US:

- `WeatherData` – the aggregate being created, holding wind and date information for a given area
- `AirControlArea` – the area the weather data belongs to (exactly one per entry)
- `WindCondition` – value object encapsulating wind direction and wind speed
- `WeatherDate` – value object encapsulating the observation date

**New concepts introduced by this US:**

- `WeatherDataImportProvider` – strategy interface for importing weather data from different sources
- `CsvWeatherDataImportProvider` – concrete strategy that parses CSV files into `ParsedWeatherDataLine` DTOs; accepts file path via constructor
- `ParseResult` – DTO containing the list of successfully parsed lines and any parse errors (replaces previous HashMap approach)
- `ParsedWeatherDataLine` – DTO representing a single parsed line from the input file (format-agnostic): code, date, windDirection, windSpeed, lineNumber
- `WeatherImportError` – value object representing a single import failure, containing a `Category` enum, line number, raw content, and a human-readable detail message
- `WeatherImportError.Category` – enum that classifies errors (`PARSE_ERROR`, `INVALID_WIND_DIRECTION`, `INVALID_WIND_SPEED`, `INVALID_DATE`, `FUTURE_DATE`, `AREA_NOT_FOUND`, `UNKNOWN_ERROR`)
- `WeatherImportException` – runtime exception thrown by the service's `processLine()` method when a single line fails validation, carrying the `Category` and detail; caught in the loop and converted to `WeatherImportError`
- `BulkImportResult` – DTO capturing the outcome of a bulk import (success count + list of `WeatherImportError`)
- `WeatherDataService` – application service that orchestrates domain logic: resolving the `AirControlArea`, validating each field, using `WeatherDataBuilder`, persisting, and catching per-line errors
- `WeatherDataBuilder` – builder pattern for step-by-step construction of the `WeatherData` aggregate
- `ImportBulkWeatherDataController` – `@UseCaseController` with authorisation, provider instantiation, service delegation
- `WeatherMenu` – submenu under the backoffice console for weather person actions

---

## 3. Design

### 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|---|---|---|---|
| 1 | Who initiates the use case? | `WeatherPerson` via `ImportBulkWeatherDataUI` or remote TCP client | Follows MVC; UI layer is responsible for interaction only. |
| 2 | How is authorisation enforced? | The controller verifies the authenticated user's role via `AuthorizationService` before proceeding | Only users with the `WEATHER_PERSON` role may access this functionality. |
| 3 | How is the import source handled? | `ImportBulkWeatherDataController` instantiates the appropriate `WeatherDataImportProvider` based on file type | Strategy pattern enables easy extension to new data sources without modifying the controller. |
| 4 | What does the Provider do? | `CsvWeatherDataImportProvider` receives the file path and returns a `ParseResult` with parsed lines + parse errors | The provider is **only** responsible for format parsing — no knowledge of repositories or domain logic. |
| 5 | Who resolves domain entities and validates the data? | `WeatherDataService` receives the `ParseResult`, queries `AirControlAreaRepository.findByCode()`, validates each field (area existence, wind direction 0-360, wind speed ≥ 0, date not future), uses `WeatherDataBuilder`, and persists via `WeatherDataRepository` | Application Service encapsulates all domain orchestration, keeping the provider pure. |
| 6 | Why a Builder for `WeatherData`? | `WeatherDataBuilder` allows step-by-step construction with internal creation of value objects (`WindCondition`, `WeatherDate`) | Builder pattern separates construction from representation; validates intermediate state. |
| 7 | How is per-line validation reported without aborting valid imports? | The service's `processLine()` throws `WeatherImportException` (with category) on any failure; the loop in `buildBulkWeatherData()` catches it and adds a `WeatherImportError` to the result, continuing to the next line | Enables partial import — one invalid line does not discard previously processed valid lines. |
| 8 | Why a typed `WeatherImportError` with a `Category` enum? | Each error carries a machine-readable category (e.g. `AREA_NOT_FOUND`, `FUTURE_DATE`) plus a human-readable detail | UI can group/filter/internationalise errors without parsing strings; tests assert on exact category. |
| 9 | Why add `findByCode()` to the repository? | The service needs to resolve an area by its formatted code string (`ACA-XXXX`); previously this was done via fragile string parsing in the service | Proper query method on the repository follows EAPLI pattern (matchOne JPQL), keeps domain logic in the right layer. |

---

### 3.2 Sequence Diagram

> See `SD_US042.puml` / `SD_US042.svg` in this folder.

---

### 3.3 Applied Design Patterns

- **MVC** – `ImportBulkWeatherDataUI` → `ImportBulkWeatherDataController` → `WeatherDataService` → Domain/Repository
- **Strategy** – `WeatherDataImportProvider` interface with concrete implementations (CSV, and future JSON/XML/REST)
- **Builder** – `WeatherDataBuilder` constructs `WeatherData` step by step, encapsulating `WindCondition` and `WeatherDate` creation
- **Application Service** – `WeatherDataService` coordinates domain logic (resolution, building, persistence) without exposing infrastructure to the provider
- **Repository** – `WeatherDataRepository`, `AirControlAreaRepository` (with `findByCode` query)
- **Value Object** – `WindCondition` (direction + speed), `WeatherDate`, `WeatherImportError` (line + category + detail)
- **Aggregate Root** – `WeatherData` enforces invariants (valid wind values, valid date, mandatory area)
- **DTO** – `ParsedWeatherDataLine`, `ParseResult`, `BulkImportResult`

---

### 3.4 Tests

| Test ID | Test Class | Test Description | Expected Result |
|---|---|---|---|
| T1 | `CsvWeatherDataImportProviderTest` | CSV with 2 valid lines | 2 parsed lines, 0 errors |
| T2 | `CsvWeatherDataImportProviderTest` | CSV with invalid numeric field (wind direction = abc) | 0 parsed lines, 1 `PARSE_ERROR` |
| T3 | `CsvWeatherDataImportProviderTest` | CSV with missing fields | 0 parsed lines, 1 `PARSE_ERROR` |
| T4 | `CsvWeatherDataImportProviderTest` | CSV with empty area code | 0 parsed lines, 1 `PARSE_ERROR` |
| T5 | `CsvWeatherDataImportProviderTest` | Non-existent file path | 0 parsed lines, 1 `PARSE_ERROR` |
| T6 | `CsvWeatherDataImportProviderTest` | Empty CSV (header only) | 0 parsed lines, 0 errors |
| T7 | `CsvWeatherDataImportProviderTest` | Mixed valid and invalid lines | 2 parsed lines, 1 `PARSE_ERROR` |
| T8 | `WeatherDataServiceTest` | All lines valid | 2 records imported, 0 errors |
| T9 | `WeatherDataServiceTest` | Non-existent area code | 0 imported, 1 `AREA_NOT_FOUND` |
| T10 | `WeatherDataServiceTest` | Invalid wind direction (400°) | 0 imported, 1 `INVALID_WIND_DIRECTION` |
| T11 | `WeatherDataServiceTest` | Negative wind speed | 0 imported, 1 `INVALID_WIND_SPEED` |
| T12 | `WeatherDataServiceTest` | Future date | 0 imported, 1 `FUTURE_DATE` |
| T13 | `WeatherDataServiceTest` | Invalid date format | 0 imported, 1 `INVALID_DATE` |
| T14 | `WeatherDataServiceTest` | Parse errors from provider preserved | 1 imported, 1 `PARSE_ERROR` preserved |
| T15 | `WeatherDataServiceTest` | Mixed valid and invalid lines | 2 imported, 1 `AREA_NOT_FOUND` |
| T16 | `WeatherDataServiceTest` | No parsed lines (empty input) | 0 imported, 1 `UNKNOWN_ERROR` |
| T17 | `ImportBulkWeatherDataControllerTest` | Valid import flow | `BulkImportResult` with successCount = 2 |
| T18 | `ImportBulkWeatherDataControllerTest` | Import with domain errors | `BulkImportResult` with errors list |
| T19 | `ImportBulkWeatherDataControllerTest` | Unauthorised user | `SecurityException` thrown |

---

## 4. Implementation

### Package structure

```
weather/
├── application/
│   ├── ImportBulkWeatherDataController.java    @UseCaseController
│   ├── WeatherDataService.java                 orquestração com try-catch por linha
│   └── WeatherImportException.java             exceção runtime categorizada
├── domain/
│   ├── WeatherData.java                        aggregate root (existente)
│   ├── WeatherDataBuilder.java                 builder (existente)
│   ├── WindCondition.java                      VO (existente)
│   ├── WeatherDate.java                        VO (existente)
│   └── WeatherParsing/
│       ├── ParseResult.java                    DTO: parsedLines + errors
│       ├── ParsedWeatherDataLine.java          DTO: linha parsed
│       ├── WeatherImportError.java             VO: category + raw + detail
│       └── BulkImportResult.java               DTO: successCount + errors

utils/provider/
├── interfaces/
│   └── WeatherDataImportProvider.java          interface: getWeatherData() → ParseResult
└── weatherParsers/
    └── CsvWeatherDataImportProvider.java       implementação CSV

airinfrastructure/repositories/
├── AirControlAreaRepository.java               + findByCode(String code)
└── JpaAirControlAreaRepository.java            matchOne("e.id.id=:code", ...)
```

### Provider layer
- `WeatherDataImportProvider` interface returns `ParseResult` (replaces previous `HashMap` approach).
- `CsvWeatherDataImportProvider` receives the file path via constructor; reads line by line, splits by `;`.
- Parse errors (missing fields, invalid numbers, empty code) produce `WeatherImportError` with `Category.PARSE_ERROR`; parsing continues to the next line.
- The file path comes from user input in the UI.

### Service layer
- `WeatherDataService.buildBulkWeatherData(ParseResult)`:
  1. Initialises `BulkImportResult` with any provider-level parse errors
  2. For each `ParsedWeatherDataLine`, calls `processLine()` inside a try-catch
  3. `processLine()`:
     - Resolves `AirControlArea` via `airControlAreaRepository.findByCode(code)` — delegates to the repository, no string parsing in the service
     - Parses date via `SimpleDateFormat("yyyy-MM-dd")` and checks if it's in the future
     - Validates wind direction (0-360) and wind speed (≥ 0) before passing to the builder
     - Uses `WeatherDataBuilder.with(area, date, windDirection, windSpeed)` to construct
     - Saves via `weatherDataRepository.save()`
  4. On any failure, `WeatherImportException` is thrown and caught, converted to `WeatherImportError` and accumulated

### Controller
- `ImportBulkWeatherDataController` is annotated with `@UseCaseController`.
- Has two constructors: no-arg (production, uses `PersistenceContext`) and parameterised (tests, receives fakes).
- Flow: `authz.ensureAuthenticatedUserHasAnyOf(WEATHER_PERSON)` → `new CsvWeatherDataImportProvider(filePath)` → `provider.getWeatherData()` → `service.buildBulkWeatherData(parseResult)`.

### UI and Menu
- `ImportBulkWeatherDataUI` extends `AbstractUI`; asks for CSV file path, displays `BulkImportResult.toString()`.
- `ImportBulkWeatherDataAction` wraps the UI.
- `WeatherMenu` (extends `Menu` from eapli framework) provides a submenu "Weather >" with "Register Weather Data" (option 1) and "Import Bulk Weather Data" (option 2), visible only for `WEATHER_PERSON` role.
- `MainMenu` references `WeatherMenu` via `addSubMenu(WEATHER_OPTION, new WeatherMenu())`.

### Repository addition
- `AirControlAreaRepository.findByCode(String code)` — new query method.
- JPA implementation: `matchOne("e.id.id=:code", Map.of("code", code))` — matches the string field inside the embedded `AirControlAreaID`.
- InMemory implementation: `matchOne(a -> a.identity().toString().equals(code))`.
- All existing `FakeAirControlAreaRepository` test classes updated to implement the new method.

---

## 5. Integration / Demonstration

> _To be completed after integration._

The bulk import will be demonstrated by:
1. Logging in as a user with the `WEATHER_PERSON` role.
2. Selecting "Weather > Import Bulk Weather Data" from the menu.
3. Providing the path to a CSV file with sample weather data (mix of valid and invalid lines).
4. Observing the import summary (X successes, Y errors grouped by category).
5. Verifying that the imported data can be consulted (US043) or reused in other weather-related features.

---

## 6. Observations

- The CSV provider uses a semicolon (`;`) as delimiter to avoid conflicts with comma-separated numeric values (e.g. wind speed with decimal separator).
- The extensibility requirement (AC6) is satisfied by the `WeatherDataImportProvider` interface. Future providers (JSON, XML, REST) only need to implement this interface and be registered in the provider resolution mechanism.
- The separation between **Provider** (format parsing only) and **Service** (domain orchestration) ensures that adding a new data source does not duplicate domain logic or couple parsing to infrastructure.
- The `WeatherDataBuilder` encapsulates value object creation, allowing the `WeatherData` aggregate to receive already-validated components without exposing the construction steps to the service.
- The typed `WeatherImportError.Category` enum makes the UI layer resilient to changes — it can group, filter, internationalise, or colour-code errors without relying on string matching.
- `AirControlArea.findByCode(String)` was added to the repository to avoid fragile string parsing in the service layer, following standard EAPLI repository query patterns.
- Alignment with US44 (remote access) must be confirmed during the RCOMP integration sprint; the controller and service layers are designed to be UI-agnostic and can be reused by both the console UI and the TCP server handler.
