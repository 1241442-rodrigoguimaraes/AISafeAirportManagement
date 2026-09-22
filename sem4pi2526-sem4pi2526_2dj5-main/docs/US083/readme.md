# US083 – Flight DSL specification and validation

---

## As a Project Manager, I want the team to specify and implement the Flight Description DSL so that flight plans can be formally defined and validated.

### Acceptance criteria:

- The informal lexical and syntactic specification of the DSL is documented.
- A formal grammar is defined using ANTLR.
- The system performs lexical and syntactic validation.
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

- The date (`date : YYYY-MM-DD`) and the time (`time : NN:NN`) will be written separately in the file.

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

[Flight Lexer Document](eapli.base/alsafe.core/src/main/DSL/FlightLexer.g4)

[Flight Parser Document](eapli.base/alsafe.core/src/main/DSL/FlightParser.g4)

---

## Semantic Rules

### Flight-level rules
- The flight designator must be unique within the file.
- The flight type must be `regular` or `charter`.

### Leg-level rules
- Each leg must have one departure and one arrival airport.
- Each leg must have at least one segment.
- Fuel quantity must be strictly positive.

### Leg sequence coherence
- The arrival airport of leg N must match the departure airport of leg N+1.

### Route coherence
- The route origin must match the departure airport of the first leg.
- The route destination must match the arrival airport of the last leg.
- The same airport cannot appear twice across all legs.

### Segment rules
- Start and end coordinates must be different.
- Altitude slots must be strictly positive.
- Width must be strictly positive.
- Wind speed must be strictly positive.
- Wind direction must be between 0 and 360 degrees.

### Profile rules
- The rate of descent values must be negative.
- Speed values must be strictly positive.
- Altitude values in the climbing profile must be in ascending order.
- Altitude values in the descending profile must be in descending order.

### Domain consistency
- IATA airport codes must exist in the system database.
- The aircraft registration must exist in the system database.
- The route identifier must exist in the system database.
- Date and time values must represent valid calendar values.


---

## Internal Representation

After lexical and syntactic validation, the parse tree is converted into domain objects. This satisfies the acceptance criterion that requires an internal representation to be produced.

The latest implementation added the following domain classes:

| Class           | Responsibility                                                                                          |
|-----------------|---------------------------------------------------------------------------------------------------------|
| `FlightPlan`    | Represents a complete flight plan, including flight id, type, route, date, time, aircraft and legs.     |
| `Leg`           | Represents one flight leg, with departure airport, arrival airport, fuel information and segments.      |
| `FuelInfo`      | Represents the fuel quantity and unit associated with a leg.                                            |
| `Segment`       | Represents a route segment, including mode, start/end coordinates, altitude slots, width and wind data. |
| `Coords3D`      | Represents a 3D coordinate with latitude, longitude and altitude.                                       |
| `SemanticError` | Represents a semantic validation error found after parsing.                                             |

The internal representation is built by the visitor after ANTLR parsing. Each `flight` block is converted into a `FlightPlan`, each `leg` block into a `Leg`, and each segment and coordinate block into the corresponding domain object.

---

## Semantic Validation Implementation

Semantic validation is performed by `FlightSemanticAnalyzerVisitor` directly over the ANTLR parse tree, before the final domain objects are built and exported.

The analyzer computes semantic attributes from grammar nodes and fills a symbol table. This follows the syntax-directed approach used in the theoretical classes: defining occurrences are inserted in the table and applied occurrences/attributes are checked while visiting the corresponding parse-tree nodes.

The symbol table stores entries such as:

- `FLIGHT`: flight identifier, type, route, date, time and aircraft attributes;
- `ROUTE`: route identifier applied in a flight block;
- `AIRCRAFT`: aircraft registration applied in a flight block;
- `AIRPORT`: airport code applied in leg departure/arrival fields;
- `LEG`: synthesized departure and arrival attributes;
- `SEGMENT`: synthesized mode, coordinates, altitude slots, width and wind attributes.

The analyzer collects semantic errors instead of stopping at the first one. Each error is represented by `SemanticError`, allowing the system to report all detected problems in a single execution.

The implemented validations include:

- duplicated flight identifiers in the same file;
- invalid date or time values;
- repeated airport visits;
- non-positive fuel quantity;
- inconsistent leg sequence;
- equal start and end coordinates in a segment;
- invalid segment width, wind direction, wind speed or altitude slots;
- invalid flight profile speeds;
- invalid rate of descent sign;
- invalid climb/descend altitude ordering.

Rules already enforced by the lexer/parser, such as the flight type tokens, IATA format, mandatory fuel blocks and mandatory segment blocks, are not duplicated in the semantic analyzer.

Semantic errors are reported in the following format:

```text
[SEMANTIC ERROR] Flight '<flightId>': <message>
