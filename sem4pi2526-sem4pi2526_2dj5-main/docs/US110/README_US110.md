
# US110 - Integrate environmental influences into simulation

## 1. Requirements

As a PO, I want the simulation to incorporate environmental factors such as wind into the simulation, so that the flight paths become more realistic and adapt to dynamic conditions.

Acceptance criteria:

- The parent process spawns an additional environment thread at simulation start.
- This thread loads environmental configuration, wind speed and wind direction, from a weather service.
- Environment data is written into the shared memory segment at each time step.

## 2. Current context

The SCOMP simulation already has:

- one parent process controlling the simulation;
- one child process per flight;
- shared memory with `SharedSimulationArea`;
- a semaphore `sem_mem` to protect shared memory access;
- parent threads for step control, safety monitoring and report generation.

Relevant files:

- `SCOMP/main.c`
- `SCOMP/flight_runner.c`
- `SCOMP/safety_monitor.c`
- `SCOMP/report_writer.c`
- `SCOMP/weather.c`
- `SCOMP/parser.c`
- `SCOMP/headers/simulation_types.h`
- `SCOMP/headers/weather.h`

Some flight plan files already include wind data, for example:

```text
wind: 270 deg 15 m/s
```

US110 uses this data as the weather source for the C simulation.

## 3. Proposed approach

The idea is to add a weather/environment thread in the parent process.

This thread should update the current wind information in shared memory for each simulation step. The flight processes can then read that information and apply a small position adjustment before writing their updated position.

The shared memory can be extended with a structure similar to:

```c
typedef struct {
    double wind_direction_deg;
    double wind_speed_ms;
    int time_step;
    int available;
} EnvironmentState;
```

In the implementation this structure is named `WeatherStats`.

And `SharedSimulationArea` can include:

```c
WeatherStats weather;
```

All reads and writes to this data must be protected with `sem_wait(sem_mem)` and `sem_post(sem_mem)`.

## 4. Synchronization

The environment data must be updated before the child flight processes calculate the position for the current step.

Expected order per step:

1. Weather thread writes wind data for the current step.
2. Step engine sends the step command to active flights.
3. Each child reads the current environment data.
4. Each child writes its updated position to shared memory.
5. Safety monitor checks the updated positions.

This keeps the US aligned with the step-by-step synchronization already introduced in US108.

## 5. Implementation notes

Implemented changes:

- Add `weather.c` and `headers/weather.h`.
- Add `WeatherStats` to `simulation_types.h`.
- Start the new thread in `main.c` with `pthread_create()`.
- Join the thread before cleaning shared resources.
- Update `parser.c` to read the `wind` values already present in `.fp` files.
- Update `flight_runner.c` so each child reads wind data from shared memory before saving its position.

For now, the weather service is represented by the wind configuration loaded from the flight plan segments. Later this can be connected to the Java/weather side if needed.

## 6. Testing

Suggested checks:

- Run with wind speed `0` and confirm positions behave as before.
- Run with segment wind configured in `.fp` files and confirm the position changes slightly every step.
- Check that all shared memory access to environment data uses `sem_mem`.
- Check that the simulation still finishes and generates the final report.
