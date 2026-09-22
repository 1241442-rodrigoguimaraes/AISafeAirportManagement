# AlSafe — Air Traffic Control & Flight Simulation System

An integrated air traffic control back-office and flight simulation system developed as a **fourth-semester project** at **ISEP**, spanning Java enterprise application development, a domain-specific language for flight plans, and low-level C systems programming with POSIX shared memory and semaphores.

## Overview

This system supports **AlSafe**, a fictional startup modernizing air traffic control, across two integrated fronts:

- **Java** — A layered back-office and remote-access application for managing airports, air transport companies, fleets, collaborators, weather data, and flight plans
- **C (SCOMP)** — A parallel flight simulation engine using shared memory, semaphores, and multithreading to simulate flight execution, detect safety violations, and generate reports in real time

## Features

### Back-Office Management
- User registration, roles, and account management (Backoffice, ATCC/FCO collaborators, pilots)
- Air control area, airport, and flight route registration
- Aircraft model, engine model, and maker catalog management
- Air transport company registration with fleet and collaborator management
- Weather data registration and bulk import

### Flight Planning
- Flight plan creation and import from a file
- Custom **Flight DSL** (ANTLR-based) for specifying and validating flight plans
- Weather data association with flights

### Remote Access
- Dedicated remote-access channels for weather personnel, pilots, and air transport company collaborators
- Remote access logging and visualization

### Flight Simulation (SCOMP)
- Hybrid simulation environment initialized with POSIX shared memory
- Function-specific threads in the parent process, synchronized via condition variables
- Step-by-step simulation synchronization with a defined time step
- Real-time detection of aircraft safety violations
- Environmental (weather) influence integrated into simulation behavior
- Parallel flight simulation with collision detection
- Final simulation report generation and monthly statistics reporting

## Tech Stack

|    Category    |                  Technology                  |
|:--------------:|:--------------------------------------------:|
|  **Language**  |         Java 17 · C (POSIX) · ANTLR4         |
|   **Build**    |                 Apache Maven                 |
| **Framework**  | EAPLI Framework (DDD-oriented layered arch.) |
|  **Database**  |       H2 (JPA / Hibernate persistence)       |
|  **Testing**   |        JUnit, Mockito, JaCoCo (Java)         |
|     **CI**     |                GitHub Actions                |
| **Simulation** | C, POSIX shared memory, semaphores, pthreads |
|    **Docs**    |     PlantUML diagrams (sequence, UC, DM)     |

## Architecture

The Java application follows a **Domain-Driven, layered architecture** on top of the EAPLI teaching framework, split into multiple Maven modules:

```
UI (Console Menus) → Controllers → Application Services → Domain Model → Repositories → H2 (JPA)
```

- **Multi-app structure** — separate console applications for backoffice, collaborators, users, testers, and remote access
- **Bootstrap modules** — seed data and system initialization on startup
- **ANTLR-generated parser** — validates and interprets the custom Flight DSL
- **SCOMP simulation engine** — a fully separate C process communicating through shared memory, decoupled from the Java application

## Project Structure

```
├── eapli.base/                        # Java application (Maven multi-module)
│   ├── alsafe.core/                   # Domain model, services, DSL, tests
│   ├── alsafe.persistence.impl/       # JPA/Hibernate persistence
│   ├── alsafe.infrastructure.application/
│   ├── alsafe.app.backoffice.console/ # Backoffice UI
│   ├── alsafe.app.collaborators.console/
│   ├── alsafe.app.user.console/
│   ├── alsafe.app.tester.console/
│   ├── alsafe.app.remoteaccess/       # Remote access channels
│   ├── alsafe.app.bootstrap/          # App entry point / bootstrap
│   └── alsafe.bootstrappers/          # Seed data bootstrappers
├── SCOMP/                             # C flight simulation engine
│   ├── main.c, flight_runner.c, safety_monitor.c
│   ├── shared_state.c, report_writer.c, weather.c
│   ├── headers/                       # Module headers
│   ├── tests/                         # Unit tests (custom test framework)
│   ├── flight_plans/                  # Sample .fp flight plan files
│   └── reports/                       # Generated simulation reports
├── docs/                              # Per-user-story documentation (US001–US121)
│   └── Global/                        # Domain model, use cases, glossary
├── flightPlanJson/                    # Sample flight plan JSON data
├── weatherDataImportFiles/            # Sample weather import data
└── generate-plantuml-diagrams.sh      # Regenerates diagrams from .puml sources
```

## How to Run

### Java Application (eapli.base)

**Prerequisites**
- JDK 17
- Apache Maven 3.x

```bash
cd eapli.base
./build.sh          # Build and run tests (build.bat on Windows)
./run.sh             # Run the application (run.bat on Windows)
./deploy.sh          # Install/deploy to another machine (deploy.bat on Windows)
```

Each console app can also be launched individually, e.g. `./run-backoffice.sh`, `./run-user.sh`, `./run-tester.sh`.

### Flight Simulation (SCOMP)

**Prerequisites**
- GCC with POSIX threads / shared memory / real-time extensions (`-lpthread -lrt -lm`)

```bash
cd SCOMP
./run-simulation.sh   # make clean && make && ./flight_simulation
```

Run the simulation's own tests with `make test` from inside `SCOMP/`.

### Diagrams

```bash
./generate-plantuml-diagrams.sh
```

Regenerates all SVG diagrams from the `.puml` sources under `docs/`. Downloads the PlantUML jar automatically on the first run.

## Testing

- Java unit tests cover the domain, services, and DSL parsing, with JaCoCo coverage reports
- SCOMP has its own lightweight C test framework (`tests/test_framework.h`) covering the parser, safety monitor, shared state, report writer, and utilities

## Academic Context

|                    |                                                                                     |
|:------------------:|:-----------------------------------------------------------------------------------:|
|     **Course**     |                       Integrative Project — 4th Semester (PI)                       |
|  **Institution**   |                  ISEP — Instituto Superior de Engenharia do Porto                   |
|     **Degree**     |                            B.Sc. in Computer Engineering                            |
| **Academic Year**  |                                      2025/2026                                      |
|      **Team**      | Henri Fontes, Miguel Ribeiro, Gabriel Ferreira, Rodrigo Guimarães and Beatriz Pinto |