# US044 – Weather Person remote access

## 1. Requirements Engineering

### 1.1 User Story Description

> As a **Weather Person**, I want to remotely access to the system in order to upload weather data.

---

### 1.2 Acceptance Criteria

- **AC1:** A specific TCP-based network client application is required to communicate with the server application embedded in the system.
- **AC2:** The client application interaction with the system must be limited to the TCP connection; remarkably, any direct interaction with the database is unacceptable.
- **AC3:** All the Weather Person user stories must be remotely available by using this client application.
- **AC4:** Authentication and authorization must be enforced.

---

### 1.3 Found Out Dependencies

| Dependency | Description |
|---|---|
| **US041** | Base domain model (`WeatherData`, `WindCondition`, `WeatherDate`) and repositories. |
| **US042** | Import Bulk Weather Data — the controller and service layers must be reusable by the remote handler without UI coupling. |
| **US043** | Consult Weather Data — the controller must be reusable by the remote handler. |
| **US030** | Authentication and authorisation must be in place — only a `WEATHER_PERSON` can perform these actions. |
| **US078** | TCP remote access infrastructure (Packet protocol, ClientHandler, RemoteServer, RemoteAccessDispatcher, AuthenticationRequestHandler) is reused as-is. |

---

### 1.4 Input and Output Data

**Authentication:**
- **Input:** Username and password.
- **Output:** Success or failure message.

**Import Bulk Weather Data:**
- **Input:** A local file path (the client reads the file and sends its content + filename over TCP).
- **Output:** Summary of the import result (number of records successfully imported, list of rejected lines with error category and reason).

**Register Weather Data:**
- **Input:** Air control area (selected from a list), date (yyyy-MM-dd), wind direction (0–360°), wind speed (m/s).
- **Output:** Success or error message with the registered data.

**Consult Weather Data:**
- **Input:** Air control area (selected from a list), start date, end date.
- **Output:** List of weather data records for the selected area and date range.

---

### 1.5 System Sequence Diagram (SSD)

> _See `SD_US044.puml` / `SD_US044.svg` in this folder._

---

### 1.6 Other Relevant Remarks

- The remote client application is a pure TCP client — it has no direct access to the database. All persistence is done through the application server.
- The existing binary application-layer protocol (Packet with OpCode, Length, Payload) is reused.
- Authentication must be performed before any Weather Person operation; the server enforces this via `ClientHandler.isAuthenticated`.
- Authorization is enforced server-side by the controllers (`authz.ensureAuthenticatedUserHasAnyOf(Roles.WEATHER_PERSON)`).
- For the bulk import, the file content is transmitted as the TCP payload (true upload). The server writes it to a temporary file, processes it with the existing providers, and cleans up.

---

## 2. OO Analysis

### 2.1 Relevant Domain Model Excerpt

The following concepts from the Domain Model are relevant to this US:

- `WeatherData` – the aggregate being created, holding wind and date information for a given area.
- `AirControlArea` – the area the weather data belongs to.
- `WindCondition` – value object encapsulating wind direction and wind speed.
- `WeatherDate` – value object encapsulating the observation date.

### 2.2 Relevant Infrastructure

| Concept | Description |
|---|---|
| `Packet` | Record with `opCode`, `length`, `payload` — standard unit of communication. |
| `TCPClient` | Wraps a TCP Socket; provides `sendPacket()` and `receivePacket()`. |
| `RemoteAccessDispatcher` | Routes incoming packets to registered handlers by opCode. |
| `ClientHandler` | Per-connection server thread; handles auth state and packet loop. |
| `RemoteServer` | Main server accepting TCP connections on port 2223. |
| `RemoteAccessRequestHandler` | Interface: `Packet handle(Packet packet)`. |

---

## 3. Design

### 3.1 Rationale

| Interaction ID | Question | Answer | Justification |
|---|---|---|---|
| 1 | How does the client connect? | The client opens a TCP Socket to the server's IP and port, then authenticates. | Reuses the existing `TCPClient` and authentication flow from US078. |
| 2 | How is the file uploaded for bulk import? | The client reads the file locally and sends `filename.ext\n<file_content>` as the TCP payload. The server writes it to a temporary file, invokes the existing controller, then deletes the temp file. | Avoids modifying existing providers; they continue to operate on file paths. True "upload" semantics. |
| 3 | How does the client know which areas exist? | A new handler (opCode=6) returns all `AirControlArea` records. The client shows them as a selectable list. | Follows the same pattern as the backoffice UI (`SelectWidget`). |
| 4 | How is authorization enforced? | The controllers already call `authz.ensureAuthenticatedUserHasAnyOf(Roles.WEATHER_PERSON)`. The handlers delegate directly to the controllers. | No additional authorization logic needed in the handler layer. |
| 5 | How are opCodes allocated? | New opCodes 4–7 are added for Weather Person operations. | Keeps the protocol extensible; no collision with existing opCodes (0–3). |

### 3.2 OpCode Table

| OpCode | Direction | Handler | Payload (Client→Server) | Response |
|---|---|---|---|---|
| 1 | C→S | `AuthenticationRequestHandler` | `username;password` | 10 (success) / 11 (failure) |
| 4 | C→S | `ImportBulkWeatherDataRequestHandler` | `filename.ext\n<file_content>` | 10 (result summary) / 11 (error) |
| 5 | C→S | `RegisterWeatherDataRequestHandler` | `areaCode;date;windDir;windSpeed` | 10 (success + data) / 11 (error) |
| 6 | C→S | `ListAirControlAreasRequestHandler` | (empty) | 10 (pipe-delimited list) / 11 (error) |
| 7 | C→S | `ConsultWeatherDataRequestHandler` | `areaCode;startDate;endDate` | 10 (formatted results) / 11 (error) |
| 10 | S→C | Generic success | Response message/data | — |
| 11 | S→C | Generic failure | Error message | — |

### 3.3 Sequence Diagram

> _See `SD_US044.puml` / `SD_US044.svg` in this folder._

### 3.4 Applied Design Patterns

- **MVC** — Remote actions (client side) ↔ Handlers (server side) ↔ Controllers ↔ Domain/Repository.
- **Strategy** — `WeatherDataImportProvider` interface with concrete implementations (CSV, JSON, XML, XLSX) is reused without change.
- **Command** — Each `RemoteAccessRequestHandler` encapsulates a request; opCode selects the handler.
- **Dispatcher** — `RemoteAccessDispatcher` decouples packet reception from handler logic.
- **Repository** — All persistence is done through repository interfaces; client never accesses the database directly.
- **Application Service** — `WeatherDataService` orchestrates domain logic, reused by both console UI and remote handlers.

### 3.5 Tests

| Test ID | Test Class | Test Description | Expected Result |
|---|---|---|---|
| T1 | Manually | Client connects and authenticates with valid credentials | "Login successful" message |
| T2 | Manually | Client authenticates with invalid credentials | "Invalid credentials" message |
| T3 | Manually | Client imports bulk weather data (valid CSV) | Summary with success count |
| T4 | Manually | Client registers weather data | Success message with registered data |
| T5 | Manually | Client consults weather data | List of matching records |
| T6 | Manually | Client attempts operation without authentication | Error / handler rejects |
| T7 | Manually | Unauthenticated/disconnected client tries direct DB access | Not possible (TCP-only) |

---

## 4. Implementation

### Package structure

```
alsafe.core/src/main/java/eapli/alsafe/remoteaccess/server/requesthandlers/
├── ImportBulkWeatherDataRequestHandler.java    NEW  (opCode=4)
├── RegisterWeatherDataRequestHandler.java      NEW  (opCode=5)
├── ListAirControlAreasRequestHandler.java       NEW  (opCode=6)
└── ConsultWeatherDataRequestHandler.java        NEW  (opCode=7)

alsafe.app.remoteaccess/src/main/java/eapli/alsafe/app/remoteaccess/menus/actions/
├── ImportBulkWeatherDataActionRemote.java       NEW
├── RegisterWeatherDataActionRemote.java         NEW
└── ConsultWeatherDataActionRemote.java          NEW

Modified files:
├── RemoteAccessHandlerRegistry.java             ADD 4 new handler registrations
└── ClientMainMenu.java                          ADD 3 new menu items
```

### Handler layer (server-side)
- Each handler implements `RemoteAccessRequestHandler`.
- The handler receives a `Packet`, deserialises the payload, instantiates the corresponding controller, and returns a response packet.
- `ImportBulkWeatherDataRequestHandler` creates a temporary file from the payload content, calls `ImportBulkWeatherDataController`, and deletes the temp file in a `finally` block.

### Client actions (client-side)
- Each action implements `eapli.framework.actions.Action`.
- Actions use the shared `TCPClient` instance (created during authentication).
- For list selection (areas), the client fetches data via opCode=6 and presents a numbered menu.
- `ImportBulkWeatherDataActionRemote` reads a local file and sends its content as the TCP payload.

### Server registration
- `RemoteAccessHandlerRegistry.handlers()` is updated to include the four new handlers.
- The existing `RemoteServer` picks them up automatically.

---

## 5. Integration / Demonstration

The remote access will be demonstrated by:
1. Starting `RemoteServer` (port 2223).
2. Starting `ClientApp` on a separate machine/terminal.
3. Authenticating as a user with the `WEATHER_PERSON` role.
4. Selecting "Import Bulk Weather Data" and providing a CSV file path — the file content is uploaded and imported.
5. Selecting "Register Weather Data", choosing an area from the list, entering date/wind data.
6. Selecting "Consult Weather Data", choosing an area and date range.
7. Verifying that no direct database access is possible from the client node.

---

## 6. Observations

- The port number in `RemoteServer.java` (2223) differs from the one documented in US078 (9999) and the one in `ClientMainMenu.java` (10501). These should be aligned during integration.
- The existing `ClientHandler` does not currently enforce authentication for individual handlers beyond the initial authentication. The controllers themselves enforce role-based authorization, so this is acceptable — but adding a server-side check in `ClientHandler.processPacket()` for `isAuthenticated` before dispatching (except opCode=1) would be more robust.
- Data fragmentation (as documented in US078) is not implemented for weather data responses, which are expected to be small. If needed, the same fragmentation protocol can be added later.
