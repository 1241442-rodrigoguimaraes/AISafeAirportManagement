# US041 – Differences Between Planned and Implemented

## 1. Air Control Area Lookup

| Aspect | Planned | Implemented |
|---|---|---|
| Method | `findByCode(areaCode)` with string code | `findAll()` returns all areas; user selects from list |
| ID type | String code | No explicit lookup; full list is displayed |

## 2. Value Object Creation

| Aspect | Planned | Implemented |
|---|---|---|
| `WindCondition` | Created explicitly: `new WindCondition(windDir, windSpeed)` | Created inside `WeatherDataBuilder.with()` |
| `WeatherDate` | Created explicitly: `new WeatherDate(date)` | Created inside `WeatherDataBuilder.with()` |
| Location | In controller method | Inside builder (encapsulated) |

## 3. WeatherData Construction

| Aspect | Planned | Implemented |
|---|---|---|
| Pattern | Direct `new WeatherData(area, weatherDate, windCondition)` | **Builder pattern**: `WeatherDataBuilder` implementing `DomainFactory<WeatherData>` |
| Fluent API | No | `.with(area, date, windDirection, windSpeed)` + individual `with*()` methods |
| Logging | No | Debug logging in `build()` |

## 4. Authorisation

| Aspect | Planned | Implemented |
|---|---|---|
| Role check | Not shown in SD | `AuthorizationService.ensureAuthenticatedUserHasAnyOf(WEATHER_PERSON)` |
| Framework interaction | Absent from SD | Explicit `AuthzRegistry.authorizationService()` used |

## 5. Controller Interactions (SD Comparison)

| Step | Planned SD | Implemented SD |
|---|---|---|
| 1 | `findByCode(areaCode)` | `authorizationService.ensureAuthenticatedUserHasAnyOf(WEATHER_PERSON)` |
| 2 | `WindCondition**` creation | `WeatherDataBuilder.with(area, date, windDir, windSpeed)` |
| 3 | `WeatherDate**` creation | (inside builder) |
| 4 | `WeatherData**` creation | `WeatherDataBuilder.build()` → `new WeatherData(...)` |
| 5 | `save(area, weatherDate, windCondition)` | `save(weatherData)` |

---

## Summary of Changes

1. **Changed** area lookup from `findByCode` to `findAll()` (full list selection)
2. **Encapsulated** value object creation (`WindCondition`, `WeatherDate`) inside the builder
3. **Added** `WeatherDataBuilder` following the project's Builder pattern (implements `DomainFactory<WeatherData>`, fluent `with*()` methods, `build()` with logging)
4. **Added** `AuthorizationService` for role-based access control (`WEATHER_PERSON` role)
5. **Simplified** controller flow: authorisation check → builder → save
