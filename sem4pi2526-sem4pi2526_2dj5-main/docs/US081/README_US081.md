# US081 – Import Flight Plan from File

---

## As a Traffic Manager, I want to import a flight plan from a `.fp` file so that it can be validated and registered in the system.

### Acceptance Criteria

- The system rejects files that do not have a `.fp` extension.
- The system rejects files that do not exist or cannot be read.
- The system performs lexical and syntactic validation before accepting the file.
- The system performs semantic validation before accepting the file.
- Invalid files generate clear and informative error messages.
- Only fully valid flight plans are accepted into the system.

---

## Design Decisions

1. **File format pre-validation is performed before parsing.**

    - Before any ANTLR processing, the file is checked: it must exist, be a regular file, be readable, and have a `.fp` extension. This avoids unnecessary parsing of clearly invalid inputs.

2. **The full processing pipeline from US083 is reused.**

    - The `FlightPlanCompiler` internally reuses the same lexer, parser, visitor and semantic validator already defined in US083. There is no duplication of pipeline logic.

3. **Errors are printed to `System.err` and an empty list is returned on failure.**

    - No custom exceptions are introduced. Syntax and semantic errors are reported directly to `System.err`, consistent with the approach already used in `FlightDSLProcessor`.

4. **The `FlightPlanCli` class serves as the command-line entry point.**

    - It calls `FlightPlanCompiler.compileFromFile()` and handles the distinct failure scenarios with typed exit codes, allowing callers (scripts, CI pipelines) to distinguish error categories.

---

## Implementation

### `FlightPlanCompiler`

The central class of US081. Exposes the following methods:

| Method                                            | Description                                                                                                                  |
|---------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| `compileFromFile(Path file)`                      | US081 entry point. Validates the file, reads it, parses it and runs semantic validation. Returns the validated flight plans. |
| `compile(String source)`                          | Parses and semantically validates DSL source code given as a string.                                                         |
| `parse(String source)`                            | Parses DSL source code without semantic validation. Returns domain objects.                                                  |
| `walk(String source, ParseTreeListener listener)` | Parses DSL source code and walks the parse tree with a listener (preserves US083 listener behaviour).                        |

#### File Pre-Validation (inside `compileFromFile`)

Before parsing, the following checks are performed in order:

1. Path must not be `null`
2. File must exist
3. Path must point to a regular file
4. File must be readable
5. Filename must end with `.fp`

Any failure throws an `IllegalArgumentException` with a descriptive message.

#### Processing Pipeline

```
.fp file
   │
   ▼
File pre-validation (exists? readable? .fp extension?)
   │
   ▼
Lexical + Syntactic Analysis (ANTLR — FlightLexer / FlightParser)
   │
   ▼
Domain Object Construction (FlightPlanBuilderVisitor)
   │
   ▼
Semantic Validation (SemanticValidator)
   │
   ▼
List<FlightPlan>  ←  returned to the caller
```

---

### `FlightPlanCli`

Command-line entry point that calls `FlightPlanCompiler.compileFromFile()` and prints the result.

**Usage:**
```
java eapli.alsafe.antlr.FlightPlanCli <flight-plan-file.fp>
```

**Exit codes:**

| Code  | Meaning                                                       |
|-------|---------------------------------------------------------------|
| `0`   | File is valid and was imported successfully                   |
| `1`   | Wrong number of arguments                                     |
| `2`   | Lexical, syntactic or semantic validation failed              |
| `3`   | Invalid input file (wrong extension, not found, not readable) |
| `4`   | I/O error while reading the file                              |

---

## Relation to US083

US081 builds directly on top of US083. The grammar, lexer, parser, visitor and semantic validator defined in US083 are all reused without modification. US081 adds:

- file-level pre-validation (extension, existence, readability);
- a `FlightPlanCompiler` class as a clean entry point for file-based import;
- a `FlightPlanCli` class as the command-line interface.

The `FlightDSLProcessor` and `FlightDSLProcessorUI` from US083 remain unchanged.

---

## Error Reporting

### File pre-validation errors
```
Flight plan rejected: invalid input file.
  - invalid file format — expected a .fp file, got: plan.txt
```

### Syntax errors
```
Flight plan rejected: validation failed.
[SYNTAX ERROR] line 3:4 — missing ':' at 'LIS'
```

### Semantic errors
```
Flight plan rejected: validation failed.
[SEMANTIC ERROR] Flight 'TP123': Leg 1 arrival airport 'LIS' does not match Leg 2 departure airport 'OPO'.
```

### Success
```
Flight plan file is valid and was imported successfully.
  FlightPlan[id=TP123, type=REGULAR, route=TP123, date=2025-05-01 09:00, aircraft=CS-TUA, legs=2]
```