# US108 – Enforce Step-by-Step Simulation Synchronization

## 1. Requirements Engineering

### 1.1 User Story Description

> As a **PO**, I want the simulation engine to synchronize the simulation's step-by-step progression using **semaphores**, so that all flight processes and parent threads advance in lockstep through each simulation time step.

---

### 1.2 Acceptance Criteria

- **AC1:** Semaphores are used to control the progression of each simulation time step.

---

### 1.3 Found Out Dependencies

| Dependency | Description |
|---|---|
| **US100** | Each flight runs in an independent child process created with `fork()`. |
| **US101** | Pipes are used to exchange step commands and position updates between parent and children. |
| **US102** | Safety violation detection depends on comparing aircraft positions from the same simulation instant. |
| **US103** | Introduces the lockstep model: one `StepCommand` per active flight per time step. |
| **US105** | Allocates shared memory (`SharedSimulationArea`) and the process-shared semaphore `sem_mem`. |
| **US106** | Parent process runs dedicated threads (`step_engine_worker`, `monitor_flights_worker`, report thread). |
| **US107** | Condition variables notify the report thread when violations occur. |

---

### 1.4 Input and Output Data

**Input:**
- Active flight processes and their step pipes.
- Shared simulation state in `/shm_simulation`.
- Named semaphore `/sem_simulation_mem`.

**Output:**
- All active flights complete exactly one position update per authorized time step.
- Parent threads only validate safety after every active flight has `updated_in_step = 1`.
- Simulation advances to the next global step only after the current step barrier is satisfied.

---

### 1.5 System Sequence Diagram (SSD)

> See `SD_US108.puml` in this folder.

---

### 1.6 Other Relevant Remarks

- US108 does **not** replace the step pipes introduced in US103. The pipes still authorize each child to advance; the semaphore protects the shared step-completion state.
- The named semaphore `sem_mem` is created in `main.c` with `sem_open("/sem_simulation_mem", O_CREAT, 0644, 1)`.
- Every read/write to `SharedSimulationArea` — including `updated_in_step`, `last_pos`, `history` and `active` — occurs inside `sem_wait(sem_mem)` / `sem_post(sem_mem)` critical sections.
- The safety monitor thread (`monitor_flights_worker`) blocks on the `updated_in_step` flag for each active flight before running proximity checks, ensuring all flights have finished the current step.

---

## 2. Analysis

### 2.1 Problem

After US103, the parent process controls the simulation clock through `StepCommand`, but multiple parent threads and child processes still share the same `SharedSimulationArea`. Without semaphore protection:

- the step engine could reset `updated_in_step` while a child is still writing;
- the monitor thread could read partially updated flight data;
- safety validation could compare positions from different simulation instants.

US108 adds a semaphore-guarded step barrier on top of the existing lockstep protocol.

### 2.2 Relevant Data Structures

| Symbol | Role |
|---|---|
| `sem_mem` | Process-shared semaphore protecting shared memory |
| `SharedSimulationArea` | Shared flight statuses and position history |
| `FlightStatus.updated_in_step` | Per-flight flag indicating completion of the current step |
| `SimulationContext.global_step` | Current simulation time step |
| `StepCommand` | Authorization sent from parent to child through the step pipe |

---

## 3. Design

### 3.1 Step Barrier Protocol

For each simulation time step:

1. **Step engine thread** resets `updated_in_step = 0` for all flights inside a `sem_mem` critical section.
2. **Step engine thread** sends one `StepCommand` to each active child through its step pipe.
3. **Child process** reads the command, updates shared memory and sets `updated_in_step = 1` inside `sem_mem`.
4. **Monitor thread** waits until every active flight has `updated_in_step = 1`, polling under `sem_mem`.
5. **Monitor thread** performs safety checks only after the barrier is complete.
6. **Step engine thread** sleeps `TIME_STEP_SECONDS`, deactivates completed flights and increments `global_step`.

This guarantees that all parent threads and child processes observe a consistent snapshot for each step.

### 3.2 Sequence Diagram

> See `SD_US108.puml` in this folder.

---

### 3.3 Applied Design Patterns

- **Critical section** — `sem_wait` / `sem_post` around shared memory access.
- **Barrier synchronization** — monitor waits for all active flights to complete the current step.
- **Lockstep clock** — step engine is the only component allowed to advance `global_step`.

---

### 3.4 Tests

| Test ID | Test Description | Expected Result |
|---|---|---|
| T1 | Run simulation with multiple `.fp` plans | Each step prints one update per active flight |
| T2 | Compare positions used in safety checks | All compared flights share the same `time_step` |
| T3 | Terminate one child unexpectedly | Parent deactivates flight without corrupting shared memory |
| T4 | Inspect shared memory updates | No concurrent read/write outside `sem_mem` critical sections |

---

## 4. Implementation

### 4.1 Key source files

| File | Responsibility |
|---|---|
| `SCOMP/main.c` | Creates shared memory and `sem_mem`; forks child processes; starts parent threads |
| `SCOMP/flight_runner.c` | `run_flight()` child loop and `step_engine_worker()` step coordinator |
| `SCOMP/safety_monitor.c` | `monitor_flights_worker()` waits for step completion and checks violations |
| `SCOMP/headers/simulation_types.h` | `SimulationContext`, `FlightStatus`, `StepCommand` |
| `SCOMP/shared_state.c` | Flight deactivation helpers |

### 4.2 Semaphore usage map

| Operation | Location | Semaphore |
|---|---|---|
| Reset `updated_in_step` at step start | `step_engine_worker` | `sem_mem` |
| Child writes position/history | `run_flight` | `sem_mem` |
| Monitor waits for step completion | `monitor_flights_worker` | `sem_mem` |
| Monitor reads positions for safety checks | `monitor_flights_worker` | `sem_mem` |
| Deactivate completed/terminated flights | `step_engine_worker` / monitor | `sem_mem` |

---

## 5. Integration / Demonstration

1. Place valid `.fp` flight plans in `SCOMP/flight_plans/`.
2. From the `SCOMP/` directory run:

```bash
./run-simulation.sh
```

3. Observe console output:
   - `--- Time Step N ---` markers from the step engine;
   - one radar line per active flight per step from the monitor;
   - safety alerts only after all active flights reported the current step.
4. Confirm that the final report in `./reports/simulation_report.txt` contains coherent per-step violation data.

---

## 6. Observations

- US108 is tightly coupled with US103 and US105; the project documents those stories separately, but the semaphore barrier is the distinguishing contribution of US108.
- Parent-thread synchronization for report notification still uses `pthread_mutex_t` and `pthread_cond_t` (US107); `sem_mem` is reserved for **inter-process** shared memory protection and step progression.
- A comment in `safety_monitor.c` explicitly marks the `updated_in_step` waiting loop as the US108 synchronization responsibility.
