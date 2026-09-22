# US091 – Remote Accesses Logging Visualization

## 1. Requirements Engineering

### 1.1 User Story Description

As an Administrator, I want to view the logs stored at the Remote Accesses Logging Server.

### 1.2 Acceptance Criteria

- The Remote Accesses Logging Server application contains an HTTP server to provide status pages to clients running a standard web browser.
- At least two pages must be available: one presenting the list of the last recorded events, and another presenting a list of the remote users currently active.
- Both pages must be kept updated to represent the current standing.
- AJAX must be used to update the presented data without the need of reloading the page.

### 1.3 Related User Stories

| User Story | Relation |
|---|---|
| US090 - External logging of remote accesses | US091 visualizes the events produced and stored by US090. The UDP event format, fields and transport are defined there. |
| US044 - Weather Person remote access | Remote TCP clients from US044 generate access events that must appear in the visualization. |
| US078 - Air Transport Company Collaborator remote access | Remote TCP clients from US078 generate access events that must appear in the visualization. |
| US086 - Pilot remote access | Remote TCP clients from US086 generate access events that must appear in the visualization. |
| US030 - Authentication and authorization | Authentication and authorization underpin the remote access events being logged and visualized. |

### 1.4 Current System Support

The repository already contains a partial logging infrastructure:

- `LoggingServer`, a UDP server that receives datagrams sent by the embedded TCP server and prints them to the console.
- `ClientHandler`, which already sends a simplified UDP authentication event for each login attempt.
- The existing UDP emission covers only authentication trial events and does not yet include all fields required by US090 (service identifier, event type, client port).
- No HTTP server or AJAX visualization layer exists in the repository yet.
- No in-memory event store or active-session tracking component exists yet.

### 1.5 Gaps Identified

- There is no HTTP server embedded in the logging server process.
- There are no HTML status pages for last events or active users.
- There are no JSON API endpoints for AJAX polling.
- The in-memory event store and active-session tracking are not yet implemented.
- Logout and disconnect events are not yet emitted by `ClientHandler`, so the active-users page cannot be fully demonstrated until US090 is complete.
- The UDP datagram format does not yet include all US090-required fields (service, event type, client port).

## 2. Analysis

### 2.1 Actor

The primary actor is the Administrator.

### 2.2 Visualization Scope

The intended US091 scope is to expose two live-updated views through a standard web browser:

- **Last recorded events** — a table of all remote access events received via UDP, ordered by timestamp, including event type, username, client IP, client port and service identifier.
- **Currently active users** — a table of authenticated remote sessions that have not yet logged out or disconnected, derived from the event stream.

### 2.3 Assumptions

- The HTTP server runs inside the same process as the UDP logging server, sharing the in-memory event store.
- No database is used by the logging server — all data is kept in memory during the server's lifetime.
- AJAX polling is used for live updates; WebSockets are out of scope.
- The Administrator accesses the pages using a standard web browser; no dedicated client application is required.
- The active-users page can only be fully reliable once logout and disconnect events are emitted by all remote TCP servers, which is a US090 dependency.

## 3. Design

### 3.1 Architecture

Logging server node:

- `UdpLogReceiver`
- `AccessLogStore`
- `HttpStatusServer`
- `LoggingVisualizationController`

Browser (Administrator):

- `/events` page with AJAX polling against `/api/events`.
- `/active-users` page with AJAX polling against `/api/active-users`.

Remote TCP server nodes (event producers):

- `ClientHandler` (US044, US078, US086) sending UDP datagrams on login, failed login, logout and disconnect.

### 3.2 Communication Protocol

The HTTP endpoints served by the logging server:

| Route | Type | Purpose |
|---|---|---|
| `GET /events` | HTML | Initial last-events page |
| `GET /api/events` | JSON | AJAX refresh of last events |
| `GET /active-users` | HTML | Initial active-users page |
| `GET /api/active-users` | JSON | AJAX refresh of active users |

Response format for AJAX endpoints should follow a consistent JSON convention:

| Field | Meaning |
|---|---|
| `timestamp` | Date and time of the event |
| `username` | Authenticated user identifier |
| `clientIp` | IP address of the remote client |
| `clientPort` | Port number of the remote client |
| `service` | `US44`, `US78` or `US86` |
| `eventType` | `LOGIN_SUCCESS`, `LOGIN_FAILURE`, `LOGOUT`, `DISCONNECT` |

### 3.3 Authentication and Authorization

- No authentication is required to access the HTTP visualization pages, as they run on the dedicated logging server node which is not exposed to end users.
- The Administrator is responsible for controlling access to the logging server node at the network level.
- The pages are read-only; no write operations are exposed through the HTTP server.

### 3.4 Remote Logging

For US090 compliance, US091 depends on the following UDP events being produced by all remote TCP servers:

- Successful login.
- Failed login.
- Logout.
- Abrupt disconnect.

Each event must include:

- Timestamp.
- Username.
- Client IP address.
- Client port number.
- Service identifier: `US44`, `US78` or `US86`.
- Event type.

The current implementation sends a simplified UDP message only for authentication attempts, so it must be extended before US091 can be fully demonstrated.

## 4. Implementation Plan

### 4.1 Logging Server Additions

New components should be added to the logging server process:

- `RemoteAccessLogEvent`
- `AccessLogStore`
- `UdpLogReceiver`
- `HttpStatusServer`
- `LoggingVisualizationController`

`LoggingServer` should be replaced or extended so that received datagrams are parsed into `RemoteAccessLogEvent` objects and stored in `AccessLogStore` instead of being printed to the console.

### 4.2 Browser-Side Additions

New pages to be served by `HttpStatusServer`:

- `/events` — HTML table populated on load; AJAX polls `/api/events` every _N_ seconds and replaces table rows without reloading the page.
- `/active-users` — HTML table populated on load; AJAX polls `/api/active-users` every _N_ seconds and replaces table rows without reloading the page.

## 5. Testing and Demonstration

### 5.1 Test Scenarios

- Administrator opens `/events` and sees the last recorded remote access events.
- Administrator opens `/active-users` and sees the currently active remote users.
- AJAX polling updates the events table without page reload.
- AJAX polling updates the active-users table without page reload.
- A successful login event received via UDP appears on the last-events page and adds the user to active users.
- A failed login event received via UDP appears on the last-events page but does not add the user to active users.
- A logout or disconnect event received via UDP removes the user from the active-users page.
- Events from US044, US078 and US086 are correctly identified by their service field.

### 5.2 Demonstration Setup

- Run the remote access TCP server (`RemoteServer`) on a server node.
- Run the logging server with `UdpLogReceiver` on a dedicated logging node.
- Run `HttpStatusServer` on the same logging node.
- Connect with remote clients (US044, US078 or US086) and perform login, logout and disconnect operations.
- Open a browser at `http://<logging-server-host>/events` and `http://<logging-server-host>/active-users`.
- Demonstrate that both pages update automatically through AJAX polling without reloading.

## 6. Open Issues

- Define the final UDP port and HTTP port for the deployment environment.
- Define the exact datagram format (delimiter, encoding) shared between `ClientHandler` and `UdpLogReceiver`.
- Decide the AJAX polling interval.
- Confirm whether logout and disconnect events will be fully implemented in US090 before the sprint review, as the active-users page depends on them.
- Decide whether event history should be bounded (e.g. last 100 events) or unlimited during the server's lifetime.