# US105 – Initialize hybrid simulation environment with shared memory

---

# Introduction

> As a Flight Control Operator, I want to start the simulation with a multithreaded parent process and multiple child flight processes communicating through a shared memory area, so that the system efficiently coordinates simulation data across processes.

**Acceptance criteria:**
- The parent process spawns dedicated threads for its functionalities.
- Each flight is launched as an independent process.
- A shared memory segment is allocated and properly initialized for inter-process communication.
- Flight processes are configured to use semaphores for synchronization.

---

# Development

1. **Threads Spawning**
- To encapsulate the simulation state and the synchronization tools of the Controller process into a single object, the `SimulationContext` struct was designed. This struct holds a pointer to the `SharedSimulationArea`, which contains the dynamic information of all flights in the simulation and the total flights count.
- The Controller process spawns two core threads: one responsible for dictating the simulation clock's rhythm (`step_thread`) and another dedicated to monitoring flights, updating their positions, and capturing potential violations (`monitor_thread`). The `ctx` instance of `SimulationContext` is passed as a parameter to both worker functions: `step_engine_worker` and `monitor_flights_worker`.

2. **IPC Shared Memory Segment**
- Before invoking `fork()`, the Controller process allocates a shared memory segment and truncates it to match the exact size of the `SharedSimulationArea` struct.
- **Architectural Evolution:** Unlike the previous sprint where the Controller communicated position updates and conclusion states with child processes via a pipe (`update_pipe`), this overhead has been eliminated. Child processes now write their telemetry data directly into the shared memory segment, drastically improving performance.

3. **Flight Synchronization & Monitoring**
- Synchronization is managed through one semaphore (`sem_mem`). The Controller initializes this semaphore and includes it in the `SimulationContext` so it can be safely accessed by both worker threads.
- Every time the `SharedSimulationArea` is read or written to, `sem_wait(sem_mem)` and `sem_post(sem_mem)` are used to bound the **critical zones**, effectively preventing race conditions.
- **US105 Execution:** The `monitor_flights_worker` safely polls each flight's `updated_in_step` flag inside these critical zones. Once a new position update is detected, the thread copies the telemetry data locally, prints the radar log to the console, and resets the flag to ensure data is processed exactly once per simulation step without busy waiting.
