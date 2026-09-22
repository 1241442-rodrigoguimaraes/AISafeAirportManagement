# US111 - Generate a Simulation Report

## 1. Requirements Engineering

### 1.1 User Story

As a Flight Control Operator, I want to receive a summary of the simulation results so that I can determine if the programmed flights are safe to run.

### 1.2 Acceptance Criteria

- The system must generate a report and store it in a file.
- The report must include the total number of flights and their execution status.
- If safety violations occur, the report must list timestamps and positions.
- The report must indicate whether the scheduled flights plan passed or failed validation.

## 2. Implemented Approach

US109 already generates the raw final report in the SCOMP component:

```text
SCOMP/reports/simulation_report.txt
```

US111 adds the Java application use case that lets an authenticated Flight Control Operator generate an archived, human-readable summary from that latest SCOMP report. The Java layer does not execute SCOMP again and does not persist anything to the database.

The generated report is stored in the same reports folder with a timestamped name:

```text
SCOMP/reports/simulation_report_yyyyMMdd_HHmmss.txt
```

This avoids polluting the remote database and leaves US112 free to aggregate these timestamped report files later.

## 3. Design

### 3.1 Main Components

| Component | Responsibility |
|---|---|
| `GenerateSimulationReportUI` | Console interaction and success/error feedback |
| `GenerateSimulationReportController` | Authorizes the Flight Control Operator and starts the use case |
| `GenerateSimulationReportService` | Locates the SCOMP report and coordinates parsing/writing |
| `SimulationReportParser` | Reads the SCOMP final report format |
| `SimulationReportWriter` | Writes the timestamped Java summary report |
| `SimulationReport` and value objects | Immutable representation of the parsed result |

### 3.2 Authorization

The controller requires:

```java
Roles.FLIGHT_CONTROL_OPERATOR
```

The Tester app can also expose the option for easier local testing, but the controller remains protected by the same role check.

### 3.3 Report Contents

The generated report includes:

- source SCOMP report path;
- generation timestamp;
- total number of flights;
- each flight execution status;
- safety violation timestamps and positions;
- final validation result (`PASS` or `FAIL`).

## 4. UI Integration

The feature is available from:

- Collaborators app: `Simulation Reports > Generate Simulation Report` for Flight Control Operators.
- Tester app: option `14. Simulation Reports >`.

The Tester app keeps `Settings >` as option `15`.

## 5. Tests

Tests are placed according to the existing project convention:

```text
eapli.base/alsafe.core/src/test/controller/simulationreport/
eapli.base/alsafe.core/src/test/domain/simulationreport/
```

Covered scenarios:

- controller authorizes Flight Control Operators;
- unauthorized users are rejected;
- missing SCOMP report fails without creating an archive;
- parser reads the current SCOMP report format;
- parser rejects malformed input;
- domain objects reject invalid data;
- writer creates a timestamped report file with the required content.

Run:

```bash
mvn -pl alsafe.core test
```

## 6. Manual Demonstration

1. Run the SCOMP simulation so `SCOMP/reports/simulation_report.txt` exists.
2. Run the Tester app.
3. Log in as:

```text
Username: JoVi@gmail.com
Password: Password1
```

4. Open `14. Simulation Reports >`.
5. Select `Generate Simulation Report`.
6. Confirm a timestamped file was created in `SCOMP/reports/`.
