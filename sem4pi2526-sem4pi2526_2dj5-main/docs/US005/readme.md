# US005 - Automated Deployment

## Description

**As Project Manager, I want the team to add to the project the necessary scripts, so that build/executions/deployments can be executed effortlessly in a Unix compatible machine. Include scripts for all the major tasks and execution of applications.**

---

## Prerequisites

- **Java** (JDK 11 or higher) installed and available in `PATH`
- **Maven** (`mvn`) installed and available in `PATH`
- All scripts must be executed from the `eapli.base/` directory

---

## Scripts

All scripts are located in the `eapli.base/` directory and must be given execute permission before first use:

```bash
chmod +x *.sh
```

---

### `build.sh` — Build the Project

Compiles the entire project, runs tests, generates reports (Surefire + Checkstyle), and copies all dependencies.

```bash
./build.sh
```

**What it does:**
- Verifies that `mvn` and `java` are installed
- Runs `mvn clean package dependency:copy-dependencies` with Surefire and Checkstyle reports
- Stops immediately on any error (`set -e`)

> Must be run before any `run-*.sh` script.

---

### `deploy.sh` — Package for Deployment

Builds the project and assembles a deployment package in the `dist/` directory.

```bash
./deploy.sh
```

**What it does:**
- Calls `build.sh` internally
- Creates the `dist/` directory
- Copies the application JAR and its dependencies into `dist/`

---

### `run.sh` — Generic Application Runner

Low-level script used by all `run-*.sh` scripts. Launches any application module given its folder name and main class.

```bash
./run.sh <module-folder> <main-class>
```

**Example:**
```bash
./run.sh alsafe.app.backoffice.console eapli.alsafe.app.backoffice.console.ExemploBackoffice
```

> Requires the project to have been built first (`build.sh`).

---

### `run-bootstrap.sh` — Run Bootstrap Application

Initialises the system with base/seed data. Should be run once before starting other applications.

```bash
./run-bootstrap.sh
```

**Main class:** `eapli.alsafe.app.bootstrap.ExemploBootstrap`

---

### `run-backoffice.sh` — Run Backoffice Application

Launches the backoffice console application for administrative users.

```bash
./run-backoffice.sh
```

**Main class:** `eapli.alsafe.app.backoffice.console.ExemploBackoffice`

---

### `run-user.sh` — Run User Application

Launches the end-user console application.

```bash
./run-user.sh
```

**Main class:** `eapli.alsafe.app.utente.console.UtenteApp`

---

### `run-other.sh` — Run Other Application

Launches an auxiliary/other console application.

```bash
./run-other.sh
```

**Main class:** `eapli.alsafe.app.other.console.OtherApp`

---

## Typical Workflow

```bash
# 1. Build the project
./build.sh

# 2. Seed the database with initial data (first time only)
./run-bootstrap.sh

# 3. Start the desired application
./run-backoffice.sh   # or ./run-user.sh / ./run-other.sh
```

---

## Notes

- All scripts use `#!/usr/bin/env bash` and are compatible with Unix/Linux/macOS.
- Windows is not directly supported; use WSL or Git Bash.
- `run.sh` will exit with an error if the JAR is not found — always run `build.sh` first.
