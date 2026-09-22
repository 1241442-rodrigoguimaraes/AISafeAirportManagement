# US120 – Flight DSL specification and validation

---

## As a Project Manager, I want the team to specify and implement the Flight Description DSL so that flight plans can be formally defined and validated.

### Acceptance criteria:

- The informal lexical and syntactic specification of the DSL is documented.
- A formal grammar is defined using ANTLR.
- The system performs lexical, syntactic and semantic validation.
- Use of listeners/visitors.
- An internal representation (AST or domain objects) is produced.
- Invalid inputs generate clear and informative error messages.

---

## DSL Design Decisions

1. **The Flight Plan file will support descriptions of one or more flights.**

- Since the project assignment is clear saying: "The flight identifier must be unique within the file", it implies that the file must support multiple flights.

2. **The Flight Designator Format**

- The Flight Designator identifies a flight uniquely. Its format is the concatenation of a two-character airline designator (xx), a numeric flight number of one to four digits (n(n)(n)(n)), and an optional one-letter operational suffix (a), resulting in: `xxn(n)(n)(n)(a)`.

- The Lexer file will be responsible for validating the Flight Designator format, creating a specific token for this attribute of a flight.

3. **The description of the Flight Profile will have to be part of the Flight Plan file.**

- This decision is based on the fact that, even though it promotes redundancy on the data between the various flights, in that way, each flight is described uniquely according to its profile, promoting more flexibility in its description.

4. **Description of the flight segments**

- The segments in the Flight Plan file will be described with the following attributes: start and end coordinates, altitude slots, width for each altitude slot, flight mode, wind direction and wind speed.

5. **The Airport Code in the Flight Plan file will be the IATA code.**

- The DSL will only accept the airport IATA code: 3-letter code.

6. **The unit for each numeric attribute in the Flight Plan file will have to be explicit.**

- This decision promotes more security and consistency in the data.

7. **The coordinates in the Flight Plan file will have to be written in a keyword block composed by: latitude, longitude and altitude.**

- The coordinates will have to be written in the following format: `latitude: <number> longitude: <number> altitude: <number>`. In this way, the coordinates will be more informative, readable and easier to understand.

8. **Date and time attributes will be two separate fields.**

- The date (`date : YYYY-MM-DD`) and the time (`time : HH:MM`) will be written separately in the file.

9. **Extensions of the Language Requirements**

- The Flight Plan file will support comments, in the following format: `// this is a comment`.
- The Flight Plan file will support a field for the aircraft: `aircraft: <aircraft_code>`.

---

## DSL Informal Specification

### Lexical Elements

The Flight DSL is composed of the following lexical elements:

**Keywords** — reserved words that define the structure of the language (case-insensitive):

`flight`, `leg`, `type`, `departure`, `arrival`, `route`, `segment`, `fuel`, `altitude`, `altitude_slots`, `profile`, `climb`, `cruise`, `descend`, `speed`, `rate_descent`, `mode`, `start`, `end`, `latitude`, `longitude`, `quantity`, `width`, `wind`, `unit`, `date`, `time`, `aircraft`, `regular`, `charter`

#### Identifiers And Codes

- Flight Designator — two letters followed by one to four digits and an optional letter suffix: `TP123`, `AA93B`
- Aircraft Registration — two uppercase letters, a hyphen, and two or more uppercase letters or digits: `CS-TUA`, `OE-LBT`
- IATA Airport Code — exactly three uppercase letters: `OPO`, `MAD`, `LIS`
- Route Identifier — same format as the Flight Designator

#### Numeric Literals

- Positive: `42`, `3.14`, `9249.5`
- Negative (used for rate of descent and coordinates): `-10`, `-8.68`

#### Date and Time Literals

- Date: `YYYY-MM-DD` — example: `2025-05-01`
- Time: `HH:MM` — example: `09:00`

#### Units

|  Unit   |                     Meaning                     |
|:-------:|:-----------------------------------------------:|
|  `kg`   |                Kilograms (Fuel)                 |
|   `l`   |                  Litres (Fuel)                  |
|   `m`   |            Metres (Altitude, Width)             |
|  `m/s`  | Metres per Second (Wind Speed, Rate of Descent) |
|  `deg`  |            Degrees (Wind Direction)             |
| `knots` |             Knots (Aircraft Speed)              |

**Separators and symbols:** `{ } [ ] : ,`

**Comments:** single-line comments starting with `//` are ignored by the parser.

**Whitespace:** spaces, tabs and line breaks are ignored and may be used freely to improve readability.

### Syntactic Structure

The DSL is hierarchical and block-based. A flight plan file contains one or more `flight` blocks. Each `flight` block contains one or more `leg` blocks. Each `leg` block contains a `fuel` block, a `profile` block and one or more `segment` blocks.

---

## Formal Grammar

[Flight Lexer Document](eapli.base/alsafe.core/src/main/antlr4/eapli/alsafe/dsl/FlightLexer.g4)

[Flight Parser Document](eapli.base/alsafe.core/src/main/antlr4/eapli/alsafe/dsl/FlightParser.g4)

---

## Semantic Rules

### Flight-level rules
- The flight designator must be unique within the file.
- The flight type must be `regular` or `charter`.

### Leg-level rules
- Each leg must have one departure and one arrival airport.
- Each leg must have at least one segment.
- Fuel quantity must be strictly positive.
- Departure and arrival airports must be different.

### Leg sequence coherence
- The arrival airport of leg N must match the departure airport of leg N+1.

### Route coherence
- The same airport cannot appear twice across all legs.

### Segment rules
- Start and end coordinates must be different.
- Altitude slots must be strictly positive.
- Altitude slots list must not be empty.
- Width must be strictly positive.
- Wind speed must be strictly positive.
- Wind direction must be between 0 and 360 degrees.
- Altitude values must be non-negative.

### Profile rules
- The rate of descent values must be negative.
- Speed values must be strictly positive.
- Altitude values in the climbing profile must be in ascending order.
- Altitude values in the descending profile must be in descending order.
- Altitude values in profile entries must be non-negative.

### Domain consistency
- IATA airport codes must exist in the system database (checked at business service level).
- The aircraft registration must exist in the system database (checked at business service level).
- The route identifier must exist in the system database (checked at business service level).
- Date and time values must represent valid calendar values.

---

## Internal Representation

After lexical and syntactic validation, the parse tree is converted into domain objects. This satisfies the acceptance criterion that requires an internal representation to be produced.

The following domain classes represent the AST produced by the DSL parser:

| Class             | Responsibility                                                                                          |
|-------------------|---------------------------------------------------------------------------------------------------------|
| `FlightPlanDSL`   | Represents a complete flight plan, including flight id, type, route, date, time, aircraft and legs.     |
| `Leg`             | Represents one flight leg, with departure airport, arrival airport, fuel information, profile and segments.|
| `FlightProfile`   | Represents the flight profile: climb entries, cruise speed (with unit), and descend entries.            |
| `ProfileEntry`    | Represents a single profile entry with altitude, speed, and optional rate of descent (descend only).    |
| `FuelInfo`        | Represents the fuel quantity and unit associated with a leg.                                            |
| `Segment`         | Represents a route segment, including mode, start/end coordinates, altitude slots, width and wind data. |
| `Coords3D`        | Represents a 3D coordinate with latitude, longitude and altitude.                                       |
| `SemanticError`   | Represents a semantic validation error found after parsing.                                             |

The internal representation is built by the `FlightPlanBuilderVisitor` after ANTLR parsing. Each `flight` block is converted into a `FlightPlanDSL`, each `leg` block into a `Leg` (including its `FlightProfile`), and each segment and coordinate block into the corresponding domain object.

---

## Processing Pipeline

The DSL file processing is orchestrated by `FlightDSLProcessor` and follows these stages:

1. **Lexical + syntactic analysis** — ANTLR lexer and parser with a custom `SyntaxErrorCollector` (extends `BaseErrorListener`) that captures all syntax errors with line/column information.
2. **Semantic analysis (parse-tree)** — `FlightSemanticAnalyzerVisitor` walks the parse tree, fills a `SymbolTable`, and validates cross-cutting rules (unique flight IDs, leg sequence coherence, repeated airports, profile ordering, coordinate equality, wind/width/speed bounds).
3. **Summary output** — `FlightPlanSummaryListener` prints a structured human-readable summary of the parsed file.
4. **Domain object construction** — `FlightPlanBuilderVisitor` converts the parse tree into domain objects (`FlightPlanDSL`, `Leg`, `FlightProfile`, `ProfileEntry`, `Segment`, `Coords3D`, `FuelInfo`).
5. **Post-construction validation** — `SemanticValidator` performs complementary validation on the built domain objects (time parsing, at least one leg/segment, non-empty altitude slots, null checks).
6. **JSON export** — If the input is fully valid, `FlightPlanJsonExporter` writes a JSON file with all flight plans and their structure, including profile data.

---

## Semantic Validation Implementation

Semantic validation is performed in two layers:

### Layer 1: Parse-tree analysis (`FlightSemanticAnalyzerVisitor`)

Directly over the ANTLR parse tree, before domain objects are built. Computes synthesized attributes from grammar nodes and fills a symbol table.

The symbol table stores entries such as:

- `FLIGHT`: flight identifier, type, route, date, time and aircraft attributes;
- `ROUTE`: route identifier applied in a flight block;
- `AIRCRAFT`: aircraft registration applied in a flight block;
- `AIRPORT`: airport code applied in leg departure/arrival fields;
- `LEG`: synthesized departure and arrival attributes;
- `SEGMENT`: synthesized mode, coordinates, altitude slots, width and wind attributes.

Validations performed:
- Duplicated flight identifiers in the same file;
- Invalid date values (parsed via `LocalDate.parse`);
- Repeated airport visits across legs;
- Non-positive fuel quantity;
- Inconsistent leg sequence (arrival N != departure N+1);
- Equal start and end coordinates in a segment;
- Invalid segment width (<= 0), wind direction (outside 0..360), wind speed (<= 0) or altitude slots (<= 0);
- Negative altitude in coordinates;
- Invalid flight profile speeds (<= 0);
- Invalid rate of descent sign (>= 0);
- Invalid climb/descend altitude ordering;
- Non-negative altitude in profile entries.

### Layer 2: Domain-object validation (`SemanticValidator`)

Applied after domain object construction, complementary to layer 1:

- Time validation (`LocalTime.parse`);
- At least one leg per flight;
- At least one segment per leg;
- Non-empty altitude slots list;
- Null safety checks (fuel information).

### Error Reporting

Both layers collect errors instead of stopping at the first one. Each error is represented by `SemanticError`, allowing the system to report all detected problems in a single execution.

Semantic errors are reported in the following format:
```text
[SEMANTIC ERROR] Flight '<flightId>': line X:Y: <message>
```

Syntax errors are captured by the ANTLR `BaseErrorListener` and reported as:
```text
[SYNTAX ERROR] line X:Y — <message>
```

---

## Tests

The test suite covers:

| Test class | Tests | Scope |
|---|---|---|
| `FlightDSLProcessorTest` | 16 | Full pipeline integration: valid files, syntax errors, semantic errors, JSON export, edge cases |
| `ImportFlightPlanDSLFromFileControllerTest` | 10 | Controller-level: file validation, persistence decision |
| `FlightPlanDSLMapperTest` | 1 | Entity mapping from DSL domain objects |

### Test resource files (`.fp`)

| File | Purpose |
|---|---|
| `valid_basic.fp` | Valid baseline — OPO->MAD, REGULAR, complete profile, 1 segment |
| `valid_multiple_flights.fp` | Two valid flights in the same file |
| `invalid_missing_type.fp` | Missing `type:` field (syntax error) |
| `invalid_bad_designator.fp` | Designator starting with digit (syntax error) |
| `invalid_bad_iata.fp` | 4-letter ICAO code instead of 3-letter IATA (syntax error) |
| `invalid_missing_segment.fp` | Leg with no segment block (syntax error) |
| `invalid_duplicate_flight_id.fp` | Two flights with same ID (semantic error) |
| `invalid_zero_fuel.fp` | Fuel quantity 0 (semantic error) |
| `invalid_leg_sequence.fp` | Leg 1 arrival != Leg 2 departure (semantic error) |
| `invalid_same_coords.fp` | Segment start == end coordinates (semantic error) |
| `invalid_wind_direction.fp` | Wind direction 361 (semantic error) |
| `invalid_profile_rules.fp` | Descending climb + positive rate of descent (semantic error) |
| `invalid_negative_altitude_profile.fp` | Negative altitude literal in profile (syntax error) |
| `invalid_bad_time.fp` | Invalid time `25:00` (syntax error) |

---

## Listeners and Visitors

The implementation uses both ANTLR listener and visitor patterns:

| Class | Type | Purpose |
|---|---|---|
| `FlightPlanSummaryListener` | Listener (extends `FlightParserBaseListener`) | Prints human-readable summary to stdout during parse-tree walk |
| `FlightPlanBuilderVisitor` | Visitor (extends `FlightParserBaseVisitor`) | Builds domain objects from the parse tree |
| `FlightSemanticAnalyzerVisitor` | Visitor (extends `FlightParserBaseVisitor`) | Computes symbol table and validates semantic rules over the parse tree |

---

## Key Files

| File | Role |
|---|---|
| `FlightLexer.g4` | ANTLR4 lexer grammar defining all tokens |
| `FlightParser.g4` | ANTLR4 parser grammar defining syntactic structure |
| `FlightDSLProcessor.java` | Orchestrates the full processing pipeline |
| `FlightPlanBuilderVisitor.java` | Visitor that builds domain objects from the parse tree |
| `FlightSemanticAnalyzerVisitor.java` | Parse-tree semantic analysis with symbol table |
| `SemanticValidator.java` | Domain-object post-construction validation |
| `FlightPlanSummaryListener.java` | Parse-tree listener for human-readable summary |
| `FlightPlanJsonExporter.java` | JSON serialization of domain objects |
| `FlightPlanDSL.java` | Aggregate root domain class |
| `Leg.java` | Leg domain class with profile reference |
| `FlightProfile.java` | Flight profile domain class (climb/cruise/descend entries) |
| `ProfileEntry.java` | Single profile entry (altitude, speed, rate of descent) |
| `Segment.java` | Segment domain class |
| `Coords3D.java` | 3D coordinate domain class |
| `FuelInfo.java` | Fuel information domain class |
| `SemanticError.java` | Semantic error value object |
| `FlightDSLProcessorUI.java` | CLI for importing .fp files |
| `ImportFlightPlanFromFileController.java` | Use-case controller for file import |
