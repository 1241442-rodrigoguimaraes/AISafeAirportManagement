# US100 – Parallel Flight Simulation with Collision Detection

## 1. Requirements Engineering

### 1.1 User Story Description

> As a **Flight Control Operator**, I want to simulate flights in area. Simulations have many parameters, such as time range, geographic area, included flights, weather conditions, safety thresholds, performance settings.

---

### 1.2 Acceptance Criteria

- **AC1:** Each flight must run in its own **independent process** created via `fork()`.
- **AC2:** Telemetry data (position updates) must be sent from child processes to the parent controller via **Pipes**.
- **AC3:** The controller must detect collisions when two aircraft are within a threshold of $0.0001$ degrees (Lat/Lon) and $1.0$ unit of Altitude.
- **AC4:** Upon detecting a collision, the controller must terminate the involved flight process using a **SIGUSR1** signal.
- **AC5:** The system must use `waitpid` to ensure all child processes are cleaned up before the main program exits.

---

### 1.3 Input and Output Data

**Input:**
- Flight plan files (JSON) containing ID and segments with start/end coordinates.

**Output:**
- Console log showing real-time updates: `[Controller] Flight ID: Lat, Lon, Alt, Speed`.
- Emergency termination log: `[Flight ID] COLLISION AVOIDANCE: Terminating flight process immediately.`

---

## 2. Analysis (Data Structures)

### 2.1 Core

The following structures defined in `flight_simulation.h` represent the system's domain:

- **Position**: Represents the 3D coordinates (latitude, longitude, altitude).
- **FlightSegment**: Defines a path between two positions and the flight mode.
- **FlightPlan**: The aggregate containing all segments for a specific flight ID.
- **FlightStatus**: Internal controller state to track process PIDs and the most recent coordinates.

---

## 3. Design

### 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|---|---|---|---|
| 1 | Why use separate processes? | Execution of `run_flight` after a `fork()`. | Ensures that a crash or delay in one flight does not stop the entire simulation. |
| 2 | How is IPC handled? | Unidirectional pipes (`pipe(fd)`). | Provides a thread-safe way for children to report data to a single listener. |
| 3 | How is the signal handled? | `sigaction` with a custom handler. | Allows the flight process to perform an orderly emergency shutdown. |
| 4 | How is collision calculated? | `fabs` differences compared to a threshold. | Provides a simple but effective spatial proximity check. |

---

### 3.2 Applied Design Patterns and Technologies

- **Proximity Logic**: Uses absolute difference calculations for spatial detection.
- **Process Orchestration**: The parent process acts as a central "Controller" managing worker "Flight" processes.
- **Asynchronous Signaling**: Uses `kill()` to push emergency commands to child processes.

---

## 4. Implementation

### Key Implementation Notes:
- **File Discovery:** The system dynamically scans the `./flight_plans/` folder using `opendir` and `readdir`.
- **Data Integrity:** `strncpy` is used to safely copy mode strings to prevent buffer overflows.
- **Resource Management:** All dynamically allocated memory (for JSON strings and flight plans) is freed after the simulation ends.

---

## 5. Integration / Demonstration

To build and execute the simulation:

```bash
# Grant execution permission to the script
chmod +x run-simulation.sh

# Compile and run via the provided shell script
./run-simulation.sh