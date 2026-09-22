# US103 - Synchronize Flight Execution With a Time Step

## 1. Requirements Engineering

### 1.1 User Story Description

> As a simulation engine, I want to synchronize aircraft movements based on time steps so that I can accurately simulate real-world execution.

### 1.2 Acceptance Criteria

- **AC1:** The simulation must progress step by step.
- **AC2:** Each flight process should send position updates at defined intervals.
- **AC3:** The main process must ensure all updates for a given time step are processed before advancing to the next step.

### 1.3 Relation With Previous SCOMP User Stories

This user story builds directly on the previous SCOMP work:

- **US100:** keeps the model where each flight runs in an independent child process created with `fork()`.
- **US101:** keeps the use of pipes to send aircraft position updates from each flight process to the parent controller.
- **US102:** keeps the real-time safety violation detection and the use of `SIGUSR1` to notify involved aircraft.

US103 changes the execution model of those existing mechanisms. The child processes no longer decide their own rhythm with `sleep(1)`. The parent process now controls the simulation clock and only allows each aircraft to advance when the current time step is being processed.

## 2. Analysis

The previous SCOMP implementation already created one process per flight and used pipes to send position updates from each child process to the parent controller.

However, the child processes were still able to progress independently. This meant that a child could generate future position updates before the controller had finished processing the current time step.

US103 introduces a synchronization barrier: the parent process is now responsible for authorizing each time step, and each child process sends exactly one update only when that authorization is received.

This is necessary because safety validation must compare aircraft positions from the same simulation instant. If one flight is already at step `N` and another is still at step `N - 1`, the controller may report false violations or miss real ones.

## 3. Design

### 3.1 Process Synchronization

Two pipes are now used per flight:

- **Update pipe:** child process to parent process. Sends `FlightUpdate` data.
- **Step pipe:** parent process to child process. Sends a `StepCommand` that authorizes the next simulation step.

The parent process executes the simulation loop as follows:

1. Start a new global time step.
2. Send a `StepCommand` to every active flight process.
3. Wait until every active flight process sends one `FlightUpdate`.
4. Store all received updates in the position history.
5. Check safety violations using the complete set of updates for that step.
6. Terminate flights if needed.
7. Advance to the next step only after the current one is fully processed.

This creates a simple lockstep protocol:

```text
Parent -> Child: StepCommand(time_step)
Child  -> Parent: FlightUpdate(position for that time_step)
Parent: waits for every active child before validating and advancing
```

### 3.2 Data Structures

- `StepCommand`: contains the time step authorized by the parent.
- `FlightUpdate`: now includes a `completed` flag so that the child process can report its final update cleanly.
- `FlightStatus`: now tracks whether the flight has updated in the current step and whether it has completed its flight plan.

### 3.3 Flight Plan Input

The simulation reads the current project flight-plan DSL files from:

```text
SCOMP/flight_plans/*.fp
```

For each file, the parser extracts the flight designator and the `segment` blocks. Each segment contributes its `mode`, `start` position and `end` position to the simulation.

### 3.4 Safety and Resource Management Adjustments

Some corrections were also made to support the synchronized execution safely:

- The parent ignores `SIGPIPE`, preventing the controller from being killed if it writes to a step pipe whose child has already terminated.
- The `SIGUSR1` handler uses `_exit()` instead of `exit()`, which is safer inside signal handlers.
- Child processes close inherited pipe file descriptors that belong to previous flights, avoiding pipe leaks and possible deadlocks.
- Magic numbers were replaced by named constants such as `MAX_MODE_LENGTH`, `MAX_PATH_LENGTH`, `DEGREES_TO_METERS`, `MIN_COLLISION_DISTANCE` and `TIME_STEP_SECONDS`.

## 4. Implementation

### 4.1 Parent Controller

The controller no longer reads updates opportunistically. Instead, it acts as the time-step coordinator:

- It sends one command to each active child process.
- It reads one update from each active child process.
- It only performs safety validation after collecting all updates for the current step.
- It only advances the simulation after the current step is complete.

This guarantees that safety checks compare positions from the same simulation instant.

### 4.2 Flight Processes

Each flight process now waits for a `StepCommand` before sending its next position.

The child process does not control the simulation pace directly. The global interval is controlled by the parent through `TIME_STEP_SECONDS`, currently set to `1` second.

### 4.3 Completion Handling

When a child process sends the last position in its flight plan, the update is marked with `completed = 1`.

The parent still processes this final update in the current time step and only then deactivates the flight. This prevents the final position from being skipped in the history or in safety validation.

### 4.4 Safety Violation Logic

The safety violation check was refined to avoid false positives between aircraft that are horizontally far apart.

Previously, a vertical altitude violation could be reported only because two aircraft had similar altitude, even if they were separated by hundreds or thousands of kilometers. The current logic only reports a safety proximity violation when both conditions are unsafe:

- horizontal distance is below `HORIZONTAL_LIMIT`;
- vertical distance is below `VERTICAL_LIMIT`.

Critical collision detection remains stricter and uses `MIN_COLLISION_DISTANCE` for both horizontal and vertical separation.

## 5. Integration / Demonstration

To compile and run the simulation:

```bash
cd SCOMP
chmod +x run-simulation.sh
./run-simulation.sh
```

Expected behavior:

- The output is grouped by `--- Time Step N ---`.
- Each active flight reports at most one update per step.
- The controller processes the full batch of updates before moving to the next time step.
- The final history summary lists the positions stored for each flight.

## 6. Summary of Changes

- Added `StepCommand` and a second pipe per flight for parent-to-child synchronization.
- Changed the child flight loop so it waits for the parent before sending each update.
- Changed the controller loop into three phases: authorize step, collect all updates, validate safety.
- Added completion tracking for final flight updates.
- Improved pipe cleanup and signal handling.
- Improved safety violation detection to avoid altitude-only false positives.
- Added named constants for values that previously appeared directly in the code.
