# US102 – Detect aircraft safety violations in real time

---

# Introduction

### As a simulation system, I want to continuously monitor aircraft positions for overlaps so that I can identify and report safety violations.

**Acceptance Criteria:**
- The system must detect when two or more aircraft may eventually violate safety rules.
- Upon detecting a violation, the system should log the event and notify the involved aircraft via signals.
- Each flight process must handle the received signal and notify the system user with a message.
- When a flight process receives a SIGUSR1 (violation detected), it should block other signals while handling it.
- The system should allow early termination if safety violations exceed a predefined threshold by sending termination signals to aircraft.
- Flight processes properly handle termination signals and perform any necessary cleanup.

The goal is to implement a real-time monitoring system that can detect and report safety violations between concurrent flights. The flight processes are notified of violations via signals.

---

# Development

1. **Detection (Spatial Calculation)**

- The calculation is executed with the use of the function `calculate_distance(Position p1, Position p2)`, that computes the distance between two aircraft based on their coordinates/positions. This function implements the Equirectangular projection and applies a trigonometric correction to the difference in longitude to account for the Earth's curvature.
- It was considered the conversion of degrees to meters with the following rule: 1 degree = 111320.0 meters.

2. **Violation Checking**

- The Controller (parent process) is responsible for constantly checking for violations between the different active flights.
- The violation checking is made by the call of the function `check_violation(Position p1, Position p2)`, that calls the `calculate_distance()` function for the calculation of the horizontal distance (using latitude and longitude) and the vertical separation (based on the absolute altitude difference). The function then compares the distances to predefined limits to see if there's any safety violation.
- If a violation is detected, the function returns the violation type (`CRITICAL_COLLISION_VIOLATION`, `SAFETY_PROXIMITY_VIOLATION`, or `NO_VIOLATION`) and the parent process sends the SIGUSR1 to both the child processes involved.

3. **Signal Blocking**

- The `sigaction` struct is set with `sigfillset` to mask/block other signals while the signal handler is being executed.

4. **Early Termination**

- A limit number of violations that each flight can have is defined as `MAX_VIOLATION_LIMIT`, and it equals 10. When the number of detected violations exceeds the limit, the parent process sends the SIGUSR1 signal to the flight process and then gracefully shut it down for safety concerns.

5. **Signal Handling**

- When a child process receives the SIGUSR1 signal, it handles it as a non-fatal warning. It immediately prints an emergency message to _STDOUT_ using the async-signal-safe `write()` function to log the incident and safely returns control to its execution loop, allowing the flight simulation to proceed seamlessly.
