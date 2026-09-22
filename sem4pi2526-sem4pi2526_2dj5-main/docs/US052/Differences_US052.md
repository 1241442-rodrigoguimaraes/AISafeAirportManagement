# US052 – Differences Between Planned and Implemented

## 1. Naming

| Aspect | Planned | Implemented |
|---|---|---|
| Controller | `CreateAirportController` | `RegisterAirportController` |
| UI | `CreateAirportUI` | `RegisterAirportUI` |
| Method | `createAirport(...)` | `registerAirport(...)` |

## 2. Input Fields

| Field | Planned | Implemented |
|---|---|---|
| Elevation | Yes (standalone field) | Yes (as `altitudeMeters` inside `Node`) |

The implemented version uses only `airportName` (no town/country), and elevation is part of the `Node` entity.

## 3. Location Handling

| Aspect | Planned | Implemented |
|---|---|---|
| Type | `Coordinates` inline value object | `Node` entity (with `Coordinate` value object inside) |
| Creation | `new Coordinates(lat, lon)` directly | `NodeService.createNode(lat, lon, alt)` |
| Altitude | Separate field on `Airport` | Stored inside `Node` |

## 4. Air Control Area Lookup

| Aspect | Planned | Implemented |
|---|---|---|
| Method | `findByCode(areaCode)` (string) | `findById(airControlAreaId)` with `AirControlAreaID` |
| ID type | String code | `AirControlAreaID` value object |

## 5. Airport Construction

| Aspect | Planned | Implemented |
|---|---|---|
| Pattern | Direct `new Airport(...)` constructor call | **Builder pattern**: `AirportBuilder` implementing `DomainFactory<Airport>` |
| Fluent API | No | `with(icao, iata, name, location, area)` + individual `with*()` methods |
| Logging | No | Debug logging in `build()` |

## 6. Authorisation

| Aspect | Planned | Implemented |
|---|---|---|
| Role check | Not shown in SD | `AuthorizationService.ensureAuthenticatedUserHasAnyOf(BACKOFFICE_OPERATOR)` |
| Framework interaction | Absent from SD | Explicit `AuthzRegistry.authorizationService()` used |

## 7. Uniqueness Validation

| Aspect | Planned | Implemented |
|---|---|---|
| IATA/ICAO uniqueness | Checked inside `createAirport()` flow | Exposed as separate methods `icaoAlreadyExists()`, `iataAlreadyExists()`, `coordinatesAlreadyExist()` |
| Coordinate uniqueness | Not mentioned | Checked via `sameCoordinates()` |

## 8. Bootstrap

| Aspect | Planned | Implemented |
|---|---|---|
| Mechanism | `BootstrapService` (generic) | `AirportBootstrapper` calling `RegisterAirportController.registerAirport()` directly |

---

## Summary of Changes

1. **Replaced** inline `Coordinates` with `Node` entity + `NodeService`
2. **Changed** area lookup from string code to `AirControlAreaID` value object
3. **Added** `AuthorizationService` for role-based access control
4. **Added** `AirportBuilder` following the project's Builder pattern (implements `DomainFactory<Airport>`, fluent `with*()` methods, `build()` with logging)
5. **Exposed** uniqueness checks as public methods
