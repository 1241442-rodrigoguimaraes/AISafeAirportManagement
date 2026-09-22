# US086 - Pilot Remote Access

## 1. Requirements Engineering

### 1.1 User Story Description

As a Pilot, I want to remotely access the system using the Air Transport Company App.

### 1.2 Acceptance Criteria

- A specific TCP-based network client application is required to communicate with the server application embedded in the system.
- The client application interaction with the system must be limited to the TCP connection.
- Any direct interaction between the client application and the database is unacceptable.
- All Pilot user stories must be remotely available by using the client application.
- Authentication and authorization must be enforced.

### 1.3 Related User Stories

| User Story                                       | Relation                                                                                                             |
|--------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| US030 - Authentication and authorization         | Required to authenticate the remote Pilot and enforce the `PILOT` role.                                              |
| US043 - Consult weather data                     | Pilot can consult weather data and this should be exposed remotely if included in the Pilot remote scope.            |
| US080 - Create a flight plan                     | Core Pilot use case to be exposed through the TCP client.                                                            |
| US081 / US121 - Create a flight plan from a file | The remote client may need to submit flight plan DSL content through TCP instead of accessing server files directly. |
| US082 - Insert weather data in a flight          | Pilot use case exposed remotely by selecting an existing weather data record and a Pilot-owned flight plan.          |
| US085 - Test/validate flight plan                | Pilot validation use case exposed remotely through the TCP client.                                                     |
| US090 - External logging of remote accesses      | Login, logout and disconnect events from US086 must be sent to the logging server using UDP.                         |
| US091 - Remote accesses logging visualization    | Depends on US090 logs being produced with complete event data.                                                       |

### 1.4 Current System Support

The repository already contains a partial remote access infrastructure:

- `alsafe.app.remoteaccess`, containing the remote client/server application module.
- `RemoteServer`, a TCP server that accepts client connections.
- `TCPClient`, a TCP client abstraction used by the remote console app.
- `Packet`, the application protocol frame containing `opCode`, `length` and `payload`.
- `RemoteAccessDispatcher`, which dispatches packets to request handlers.
- `RemoteAccessHandlerRegistry`, which registers the available operation handlers.
- `AuthenticationRequestHandler`, which authenticates credentials using the existing authentication service.
- `ClientHandler`, which manages a TCP connection with one client.
- `FlightPlanController` and `FlightPlanService`, which already centralize Pilot flight plan operations and can be reused by remote handlers.

The remote access implementation now includes Pilot-specific client actions and server request handlers for the supported US086 scope.

### 1.5 Gaps Identified

- The server stores an `isAuthenticated` flag but does not consistently reject non-authenticated business requests.
- The authenticated user/session is not passed as context to business request handlers.
- Authorization is not enforced at the remote protocol boundary for the `PILOT` role.
- Client and server ports are not aligned in code: the remote client points to `vsgate-s1.dei.isep.ipp.pt:10501`, while `RemoteServer` listens on port `2223`.
- UDP logging exists only as a basic authentication log and does not yet include all US090-required fields.
- Logout and disconnect events are not fully logged according to US090.
- The logging server currently prints UDP messages to the console and does not yet provide the HTTP/AJAX visualization required by US091.

## 2. Analysis

### 2.1 Actor

The primary actor is the Pilot.

### 2.2 Remote Access Scope

The intended US086 scope is to expose the Pilot's user stories through the TCP remote client. Based on the current codebase, the following operations are already implemented locally and should be exposed remotely:

- Create a flight plan.
- List the authenticated Pilot's flight plans.
- Submit a draft flight plan.
- Cancel a draft or submitted flight plan.
- Validate a submitted flight plan.
- Consult weather data.
- Create/import a flight plan from a DSL file.
- Insert weather data in a flight plan.

### 2.3 Assumptions

- The remote client must never access repositories, JPA, persistence units or the database directly.
- The TCP server is the only remote access component allowed to invoke application controllers and repositories.
- The Pilot authenticates remotely before invoking any Pilot operation.
- A non-Pilot authenticated user must not be allowed to use US086 operations.
- File-based flight plan creation must be adapted for remote use by sending the file content through TCP, because the server cannot assume access to the client's local filesystem.
- US090 logging is treated as a cross-cutting dependency of US086.

## 3. Design

### 3.1 Architecture

Client node:

- `ClientApp`
- `ClientMainMenu`
- Remote Pilot actions
- `TCPClient`

Server node:

- `RemoteServer`
- `ClientHandler`
- `RemoteAccessDispatcher`
- Pilot request handlers
- Existing application controllers and services

Database node:

- Remote RDBMS configured through JPA and application properties.

Logging node:

- Remote Accesses Logging Server receiving UDP datagrams.
- HTTP/AJAX visualization is covered by US091.

### 3.2 Communication Protocol

The current protocol uses one byte for the operation code, four bytes for payload length and the payload bytes.

US86 operation codes:

| OpCode | Operation               | Payload                                    |
|--------|-------------------------|--------------------------------------------|
| `0`    | Logout / disconnect     | Empty payload.                             |
| `1`    | Authenticate            | `username;password`.                       |
| `20`   | List Pilot routes       | Empty payload.                             |
| `21`   | List company aircraft   | Empty payload.                             |
| `22`   | Create flight plan      | Flight plan creation data and DSL content. |
| `23`   | List Pilot flight plans | Empty payload.                             |
| `24`   | Submit flight plan      | Flight plan identifier.                    |
| `25`   | Cancel flight plan      | Flight plan identifier.                    |
| `26`   | Validate flight plan    | Flight plan identifier.                    |
| `28`   | List Pilot air control areas | Empty payload.                         |
| `29`   | List available weather data | Empty payload.                          |
| `30`   | Insert weather data in flight plan | `flightPlanId;weatherDataId`.     |

Response operation codes should preserve the current convention:

| OpCode | Meaning                              |
|--------|--------------------------------------|
| `100`  | Success response.                    |
| `101`  | Error response.                      |
| `99`   | Unknown operation or protocol error. |

### 3.3 Authentication and Authorization

- Authentication is requested with `opCode 1`.
- The server validates credentials using the existing authentication service.
- The server must keep the authenticated user/session associated with the TCP connection.
- Before executing any US86 operation, the server must verify that the authenticated user has the `PILOT` role.
- Any unauthenticated request, except authentication and logout/disconnect, must be rejected.
- Any authenticated non-Pilot request to US86 operations must be rejected.

### 3.4 Remote Logging

For US090 compliance, US86 must produce UDP events for:

- Successful login.
- Failed login.
- Logout.
- Abrupt disconnect.

Each event must include:

- Timestamp.
- Username.
- Client IP address.
- Client port number.
- Service identifier: `US86`.
- Event type.

The current implementation sends a simplified UDP message only for authentication attempts, so it must be extended before US86 can be considered complete.

## 4. Implementation Plan

### 4.1 Client-Side Additions

Remote actions added under `alsafe.app.remoteaccess`:

- `ListPilotRoutesActionRemote`
- `ListPilotAircraftActionRemote`
- `CreateFlightPlanActionRemote`
- `ListFlightPlansActionRemote`
- `SubmitFlightPlanActionRemote`
- `CancelFlightPlanActionRemote`
- `ValidateFlightPlanActionRemote`
- `InsertWeatherDataInFlightActionRemote`
- `PilotConsultWeatherDataActionRemote`

`ClientMainMenu` should show Pilot options only after successful authentication as a Pilot.

### 4.2 Server-Side Additions

Request handlers added under `eapli.alsafe.remoteaccess.server.requesthandlers`:

- `ListPilotRoutesRequestHandler`
- `ListPilotAircraftRequestHandler`
- `CreateFlightPlanRequestHandler`
- `ListPilotFlightPlansRequestHandler`
- `SubmitFlightPlanRequestHandler`
- `CancelFlightPlanRequestHandler`
- `ValidateFlightPlanRequestHandler`
- `ListPilotAirControlAreasRequestHandler`
- `ListAvailableWeatherDataRequestHandler`
- `InsertWeatherDataInFlightRequestHandler`

These handlers should reuse existing controllers and services instead of duplicating business rules.

## 5. Testing and Demonstration

### 5.1 Test Scenarios

- Valid Pilot credentials authenticate successfully.
- Invalid credentials are rejected and logged.
- Valid non-Pilot credentials are rejected for US86 operations.
- A non-authenticated client cannot execute Pilot operations.
- A Pilot can list available routes remotely.
- A Pilot can list available aircraft remotely.
- A Pilot can create a flight plan remotely.
- A Pilot can list their flight plans remotely.
- A Pilot can submit a draft flight plan remotely.
- A Pilot can cancel a draft or submitted flight plan remotely.
- A Pilot can validate a submitted flight plan remotely.
- A Pilot can consult weather data remotely.
- A Pilot can insert existing weather data into one of their flight plans remotely.
- Login, failed login, logout and disconnect events are sent to the UDP logging server.
- The remote client does not access the database directly.

### 5.2 Demonstration Setup

- Run the TCP remote server outside the IDE on a server/cloud node.
- Run the remote client outside the IDE on another network node.
- Use a remote RDBMS persistence unit.
- Run the Remote Accesses Logging Server on a dedicated node.
- Demonstrate at least one complete Pilot flow through TCP only, for example: authenticate, create flight plan, list flight plans, insert weather data, submit, validate and logout.

## 6. Open Issues

- Decide the final TCP and UDP ports for the deployment environment.
- Define the final payload format for complex operations, especially flight plan creation with DSL content.
