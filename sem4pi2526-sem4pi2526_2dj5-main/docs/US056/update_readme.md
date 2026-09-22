# US056 Update - Builder Pattern Application

## Overview

The engine model creation process was updated to use the Builder pattern, following the same approach already applied in US060.

This update centralizes the creation of `engineModel` objects in a dedicated builder class, making the construction process more consistent between manual creation and bootstrap creation.

## Updated Structure

A new domain class, `EngineModelBuilder`, was added to the engine model domain package.

The current structure is:

- `EngineModelBuilder`: responsible for preparing and building the `engineModel` aggregate.
- `CreateEngineModelController`: coordinates the use case and delegates object construction to the builder.
- `EngineModelBootstrapper`: uses the builder during bootstrap initialization.
- `engineModel`: remains the aggregate root and keeps the domain validation rules.
- `engineModelName`, `engineModelPower`, and `engineModelEfficiency`: remain Value Objects used by the aggregate.

## Builder Responsibility

The builder receives the data required to create an engine model and prepares the corresponding Value Objects when needed.

The aggregate is created only when the `build()` method is called.

This means that the controller and bootstrapper no longer instantiate `engineModel` directly. Instead, they configure the builder and then request the final aggregate.

## Builder Usage

The builder provides a complete `with(...)` method for creating an engine model in a single chained call:

```
new EngineModelBuilder()
    .with(modelName, maker, type, power, fuel, efficiency)
    .build();
```

This method receives the main data required for the engine model:

- model name;
- maker;
- engine type;
- power;
- fuel type;
- efficiency.

The builder also provides step-by-step methods

Each `withX(...)` method returns the builder itself, allowing method chaining.

## Current Creation Flow

The engine model creation flow is now:

1. The UI collects the engine model data.
2. The `CreateEngineModelController` validates authorization.
3. The controller checks if an engine model with the same name and maker already exists.
4. The controller passes the creation data to `EngineModelBuilder`.
5. The builder creates the `engineModel` aggregate through `build()`.
6. The controller saves the aggregate in the repository.

## Bootstrap Creation

The `EngineModelBootstrapper` was also updated to use `EngineModelBuilder`.

This ensures that both manual creation and bootstrap creation follow the same construction process.

## Compatibility

The public method used by the UI remains unchanged.

This means that the use case behavior is preserved externally, while the internal object creation logic is now centralized in the builder.

## Tests

Unit tests were added for `EngineModelBuilder`.

The tests cover:

- complete creation using `with(...)`;
- step-by-step creation using the individual `withX(...)` methods;
- invalid data validation.

The controller tests were also updated to verify that a null builder dependency is rejected.
