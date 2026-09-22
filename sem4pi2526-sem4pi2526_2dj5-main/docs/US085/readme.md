# US085 – Validate a Flight Plan

---

# User Story Statement / Requirements

> As a Pilot, I want to test/validate a flight I’ve made.

**Acceptance Criteria:**
- The validation must include validation of the flight plan described using the DSL and its test.
- The test component must be implemented in the C language.

The goal of this user story is to allow a pilot to validate one of his flight plans by the use of the DSL implemented in the project along with the simulation structure implemented in C language.

---

# Analysis

1. **DSL Validation:** the DSL validation of the flight plans developed by the pilot must be done by the language validation structure already implemented in the project.
2. **Flight Plans Testing:** the testing of flight plans has to be done by the simulation code structure developed in C language, in the context of the SCOMP course. The simulation checks if the flight plan describes a flight that presents safety violations, and if it does, it becomes rejected.
3. **Flights Approval:** as soon as the flight plan is validated by the DSL validation structure and by the C testing code, the flight plans get approved/validated, being ready to be put in practice by an actual flight.

---

# Design & Implementation

- Design of the interface `FlightPlanTestRunner`;
  1. This interface contains the method `run(String dslContent)`, responsible for running the simulation code implemented in the context of the SCOMP course, that will be the testing component of the flight plans developed by the pilot.
  2. Also, this interface has an inner record class `TestResult` responsible for containing the summary of the simulation results, either if it was successful or not.
- Design of the class `SCOMPFlightPlanTestRunner`, that implements the interface `FlightPlanTestRunner`;
  1. The reason for the interface not to be implemented directly as a class or as a private method in `FlightPlanService` is because of the "Dependency Inversion Principle" (DIP): the interface allows the test class `FlightPlanServiceTest` to not depend direclty on a compiled binary file (the simulation binary file `flight_simulation`) or a directory (the simulation directory `\SCOMP`), isolating this way the validation logic from those dependencies. The concrete implementation is then tested separately in `SCOMPFlightPlanTestRunnerTest`
- Changing of the class `FlightPlanService` for it to include the use of `FlightPlanTestRunner`.
  1. The validation via DSL validation structure was already being implemented, only missing the invocation of the C simulation test component. This invocation will be done by the call to the method `run(String dslContent)`, right after the DSL validation and before the actual approval of the flight plan.
  2. If the DSL validation and the C simulation test component are successful, the flight plan is approved. If any of those two steps fails, the flight plan gets rejected. After the simulation test, the boolean attribute `tested` from the class `FlightPlan`, initially as false, is changed for true.

---

# Acceptance Tests

**Unit Tests**
- **Action:** Test the domain and controller classes involved in the flight route creation process.
- **Expected Result:** All classes should pass their respective unit tests.

**Manual Test 1: Validate a Flight Plan**
- **Action:** Choose one of the existent flight plans to validate.
- **Expected Result:** The flight plan should be validated successfully or not, depending on the DSL validation and the C simulation test results.

---

## Integration/Demonstration

To demonstrate the functionality:

1. Build the project (`build.bat` or `build.sh`).
2. (Optional) Run the bootstrap process for initial data bootstrapping (usually via `run-bootstrap.bat` or `run-bootstrap.sh`).
3. Run the ATCC application (`run-collaborators.bat` or by actually running the `CollaboratorsApp` class).
4. Log in with Pilot credentials (or create a new one) and navigate to the flight plan creation page.
5. Choose the flight plan validation option and choose one of the existent ones to validate.
6. Read the result message: if the validation was successful, the flight plan will be approved. Otherwise, the flight plan will be rejected.

---

# Observations

None.
