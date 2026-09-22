# US055 Update - Builder Pattern Application

## Overview

The aircraft model creation process was updated to use the Builder pattern, following the same approach already applied in US060.

This update centralizes the creation of `aircraftModel` objects in a dedicated builder class, making the construction process more consistent between manual creation and bootstrap creation.

## Updated Structure

A new domain class, `AircraftModelBuilder`, was added to the aircraft model domain package.

The current structure is:

- `AircraftModelBuilder`: responsible for preparing and building the `aircraftModel` aggregate.
- `CreateAircraftModelController`: coordinates the use case and delegates object construction to the builder.
- `AircraftModelBootstrapper`: uses the builder during bootstrap initialization.
- `aircraftModel`: remains the aggregate root and keeps the domain validation rules.
- `aircraftModelPhysicsData`: keeps the physics-related validation rules.
- `maximumRange`: represents the aircraft model maximum range.
- `engineModel`: represents the certified engines associated with the aircraft model.

## Builder Responsibility

The builder receives the data required to create an aircraft model and prepares the corresponding Value Objects when needed.

The aggregate is created only when the `build()` method is called.

This means that the controller and bootstrapper no longer instantiate `aircraftModel` directly. Instead, they configure the builder and then request the final aggregate.

## Builder Usage

The builder provides a complete `with(...)` method for creating an aircraft model in a single chained call:

```
new AircraftModelBuilder()
    .with(
        modelName,
        maker,
        type,
        motorization,
        maximumRangeKm,
        emptyWeight,
        mtow,
        mzfw,
        fuelCapacity,
        serviceCeiling,
        cruiseSpeed,
        wingArea,
        dragCoefficient,
        liftCoefficient,
        certifiedEngines
    )
    .build();
```

This method receives all the main data required for the aircraft model:

- model name;
- maker;
- aircraft type;
- motorization;
- maximum range;
- physics data values;
- certified engine models.

The builder also provides step-by-step methods

Each `withX(...)` method returns the builder itself, allowing method chaining.

## Current Creation Flow

The aircraft model creation flow is now:

1. The UI collects the aircraft model data.
2. The `CreateAircraftModelController` validates authorization.
3. The controller checks if an aircraft model with the same name and maker already exists.
4. The controller verifies that at least one certified engine was selected.
5. The controller passes the creation data to `AircraftModelBuilder`.
6. The builder creates the `aircraftModel` aggregate through `build()`.
7. The controller saves the aggregate in the repository.

## Bootstrap Creation

The `AircraftModelBootstrapper` was also updated to use `AircraftModelBuilder`.

This ensures that both manual creation and bootstrap creation follow the same construction process.

## Compatibility

The public method used by the UI remains unchanged.

This means that the use case behavior is preserved externally, while the internal object creation logic is now centralized in the builder.

## Tests

Unit tests were added for `AircraftModelBuilder`.

The tests cover:

- complete creation using `with(...)`;
- step-by-step creation using the individual `withX(...)` methods;
- certified engine association using `addCertifiedEngine(...)`;
- invalid data validation.

The controller tests were also updated to verify that a null builder dependency is rejected.
