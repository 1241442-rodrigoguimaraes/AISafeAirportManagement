# US106 - Function-Specific Threads in the Parent Process

## 1. Requirements Engineering

### 1.1 User Story Description

> As a PO, I want the simulation controller parent process to have at least two dedicated threads, one for safety violation detection and one for report generation, so that each functionality operates concurrently and independently.

### 1.2 Acceptance Criteria

- **AC1:** The parent process creates a safety violation detection thread responsible for scanning the shared memory for aircraft flight conflicts.
- **AC2:** A report generation thread is created to compile simulation results and respond to safety violation events.
- **AC3:** Additional threads may be created when useful for the required simulation functionalities.
- **AC4:** Threads are managed using mutexes and condition variables for internal synchronization.

## 2. Theoretical Alignment

The implementation keeps the main concurrency model based on the SCOMP concepts covered in the theoretical classes:

- `fork()` creates one independent child process per flight.
- `waitpid()` is used by the parent process to collect child processes and avoid zombies.
- `pipe()`, `read()` and `write()` synchronize simulation time steps between the parent controller and each child flight process.
- `shm_open()`, `ftruncate()` and `mmap()` create the shared memory area used by parent and children.
- `sem_open()`, `sem_wait()` and `sem_post()` protect shared memory access between processes.
- `sigaction()` installs a `SIGUSR1` handler in each child process.
- `kill(pid, SIGUSR1)` notifies affected flight processes when a safety violation is detected.

The US106 requirement also explicitly asks for parent-process threads, mutexes and condition variables. These are therefore used only inside the parent process, where the user story requires them:

- `pthread_create()` creates the function-specific parent threads.
- `pthread_join()` waits for parent threads before resources are released.
- `pthread_mutex_t` protects parent-process internal data shared by threads.
- `pthread_cond_t` allows the safety thread to wake the report thread when new violation data is available.

## 3. Design

### 3.1 Runtime Responsibilities

| Component | Responsibility | Main Synchronization |
|---|---|---|
| Parent process | Initializes IPC resources, creates children and parent threads, coordinates shutdown. | `waitpid`, `pthread_join` |
| Child flight process | Receives time-step commands and writes its current position to shared memory. | Pipe + shared memory semaphore |
| Step engine thread | Sends time-step commands to active flights and deactivates completed flights. | Mutex for parent state + semaphore for shared memory |
| Safety monitor thread | Reads active flight positions, detects conflicts and records violation events. | Semaphore for shared memory + mutex/condition variable for report events |
| Report generation thread | Waits for violation events during simulation and writes the final report at shutdown. | Mutex + condition variable |

### 3.2 Data Flow

1. The parent process creates the shared memory segment and a named semaphore.
2. The parent process creates one pipe and one child process per flight plan.
3. Each child waits for a `StepCommand` from its pipe.
4. The step engine thread writes a command to each active child pipe for each simulation step.
5. Each child writes its new position into `SharedSimulationArea`, protected by `sem_mem`.
6. The safety monitor thread waits until active flights have updated their position for the current step.
7. The safety monitor compares aircraft positions and records a `SafetyViolationEvent` when limits are violated.
8. The safety monitor sends `SIGUSR1` to affected child processes.
9. The safety monitor signals `cond_violation` to wake the report generation thread.
10. The report generation thread processes queued events during the simulation.
11. At the end, the parent waits for children with `waitpid()` and asks the report thread to write the final report.
12. The parent joins all threads and releases shared memory, semaphores, pipes and allocated buffers.

## 4. Implementation Summary

### 4.1 Parent Threads

The parent process now creates three pthreads:

- `step_engine_worker`: additional control thread that advances simulation steps.
- `monitor_flights_worker`: required safety violation detection thread.
- `report_generation_worker`: required report generation thread.

The report thread is created before the simulation starts and remains alive until shutdown. It waits on `cond_violation` instead of polling continuously.

### 4.2 Shared Memory Protection

`SharedSimulationArea` is shared between the parent process and all child flight processes. For that reason, it continues to be protected with the POSIX semaphore `sem_mem`.

This follows the theoretical model where semaphores provide mutual exclusion over shared resources accessed by different processes.

### 4.3 Parent Internal Thread Synchronization

`SimulationContext` contains the mutex and condition variable used only by parent-process threads:

- `mutex` protects parent-only state such as `global_step`, `simulation_done`, `report_shutdown` and the violation event counter.
- `cond_violation` is signaled by the safety monitor after a violation is recorded.
- `report_shutdown` tells the report thread that no more events will arrive and that it should generate the final report.

This keeps process-level synchronization and thread-level synchronization separated.

### 4.4 Safety Detection Fix

The safety monitor no longer clears `updated_in_step` before checking conflicts. The previous flow could mark a flight as not updated before the pairwise safety check, preventing violations from being detected.

The monitor now processes each `global_step` once and clears `updated_in_step` only after logging and safety checks for that step are complete.

## 5. Acceptance Criteria Coverage

| Acceptance Criterion | Status | Evidence |
|---|---|---|
| AC1 | Implemented | `monitor_thread` runs `monitor_flights_worker` and scans `SharedSimulationArea`. |
| AC2 | Implemented | `report_thread` runs `report_generation_worker`, waits for violation events and writes the final report. |
| AC3 | Implemented | `step_thread` runs `step_engine_worker` as an additional useful parent thread. |
| AC4 | Implemented | `mutex` and `cond_violation` coordinate parent-thread state and report notifications. |

## 6. Verification Notes

The intended build command remains:

```bash
cd SCOMP
make clean
make
./flight_simulation
```

In the current Windows environment, `gcc` and `make` are not installed, so compilation could not be executed locally. Static verification was performed against the changed files.

Expected runtime evidence:

- time-step logs from the controller;
- child flight position updates in shared memory;
- safety warnings when aircraft violate separation limits;
- `[Report] Safety event queued` messages when the report thread receives events;
- final report saved to `SCOMP/reports/simulation_report.txt`.
