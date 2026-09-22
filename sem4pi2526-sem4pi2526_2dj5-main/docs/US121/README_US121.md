# US121 - Create a Flight Plan from a File

## Requirement

As a Pilot, I want to create a valid flight plan from a file.

### Acceptance Criteria

- The flight plan file must conform to the Flight DSL, as specified in US120.
- The file must be validated according to US120: lexical, syntactic and semantic analysis.
- Invalid files must produce meaningful error messages.
- Only valid flight plans may be imported and used by the system.

## Design Decisions

1. **US121 does not introduce a new menu option.**

   The existing flight plan lifecycle is preserved:
   `Create Flight Plan` creates a draft plan with DSL content loaded from a `.fp` file,
   `Submit Flight Plan` submits it, and `Validate Flight Plan` is the point where the file
   content is validated before the plan can be approved and used.

2. **US120 remains responsible for language validation.**

   US121 reuses `FlightDSLProcessor`, which runs the ANTLR lexer/parser, semantic visitor
   and builder visitor. This avoids duplicate parsing logic and keeps the DSL rules in one place.

3. **US121 validates consistency between the DSL and the operational FlightPlan.**

   After US120 accepts the DSL content, the system compares the parsed `FlightPlanDSL` with
   the persisted `FlightPlan`: flight id, route, aircraft, departure date/time, route endpoints
   and fuel.

4. **Only submitted plans can be validated.**

   This follows the existing domain lifecycle: `DRAFT -> SUBMITTED -> VALIDATED/REJECTED`.
   If DSL validation or consistency validation fails, the submitted plan is rejected.

## Processing Pipeline

```text
Pilot selects Validate Flight Plan
        |
        v
ValidateFlightPlanUI selects a SUBMITTED FlightPlan
        |
        v
FlightPlanController.validatePlan(plan)
        |
        v
FlightPlanService.validate(plan)
        |
        +-- operational checks: fuel range and pilot certification
        |
        +-- US120 pipeline: FlightDSLProcessor.process(plan.content)
        |       |
        |       +-- lexical/syntactic analysis
        |       +-- semantic analysis
        |       +-- internal representation: FlightPlanDSL
        |
        +-- US121 consistency checks
                |
                +-- matching flight id, route, aircraft and departure date/time
                +-- first DSL departure matches route origin
                +-- last DSL arrival matches route destination
                +-- DSL fuel matches FlightPlan fuel
```

## Consistency Rules

- The DSL must describe exactly one flight for the selected `FlightPlan`.
- The DSL flight id must match the `FlightPlanID`.
- The DSL route id must match the selected route.
- The DSL aircraft registration must match the selected aircraft.
- The DSL date/time must match the `FlightPlan` departure date/time.
- The first leg departure airport must match the route starting airport.
- The last leg arrival airport must match the route destination airport.
- Fuel declared in the DSL must match the `FlightPlan` fuel quantity.

### Fuel Conversion

The DSL supports kilograms and litres. The flight plan domain also supports pounds, but the
DSL does not currently use pounds.

For DSL litre/kilogram conversion, US121 uses the JET A-1 density:

```text
1 l = 0.804 kg
kg = l * 0.804
l = kg / 0.804
```

Multiple legs are supported by summing each leg's fuel before comparing it with the flight plan.

## Error Reporting

`FlightDSLProcessor.Result` exposes structured errors:

- `getSyntaxErrors()` for lexical/syntactic errors collected from ANTLR.
- `getErrors()` for semantic errors collected by `FlightSemanticAnalyzerVisitor`.
- `allErrors()` for UI/service reporting.

This improves the previous behaviour where syntax errors were printed to `System.err` but not
available to application services or tests.

## LPROG Rationale

This implementation follows good LPROG practice because:

- the grammar remains centralized in ANTLR files;
- lexical and syntactic validation are performed by generated ANTLR components;
- semantic validation remains in the semantic visitor from US120;
- US121 consumes the internal representation produced by the visitor instead of parsing strings manually;
- errors are structured and testable.

## Key Files

| File | Role |
|---|---|
| `FlightDSLProcessor.java` | Runs the US120 DSL pipeline and exposes syntax/semantic errors |
| `FlightPlanService.java` | Validates operational FlightPlan data against the parsed DSL |
| `ValidateFlightPlanUI.java` | Existing UI entry point for validating submitted plans |
| `SD_US121.puml` | Sequence diagram for the validation flow |

## Tests

Relevant automated tests are in `FlightPlanServiceTest` and cover:

- valid DSL content approving a submitted flight plan;
- syntax errors rejecting the plan with syntax details;
- semantic errors rejecting the plan with semantic details;
- mismatches in flight id, route, aircraft, date/time, airports and fuel;
- fuel conversion using JET A-1 density;
- multiple legs with summed fuel.
