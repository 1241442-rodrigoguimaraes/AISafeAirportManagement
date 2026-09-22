# US031 – Register Users

## 1. Context

As an Administrator, it's necessary to be able to register backoffice users, such as Backoffice Operators and other Administrators. They are the ones responsible 
for the System Users management.

### 1.1 List of issues

#### Analysis: 

- "Which roles are considered backoffice users?" (Administrator and Backoffice Operator.)
- "What data is required for a system user?" (Username, Password, First Name, Last Name, Email, Phone Number, Activated Security Clearance, Skills Assessment, and Role.)
- "How to ensure uniqueness of email?" (Enforced by the framework and database constraints.)
- "How to handle authentication and authorization?" (Using `AuthorizationService` to ensure only authorized users can register others.)

#### Design:

- Design of a UI component for manual registration: `AddUserUI`.
- Design of a Controller component that coordinates the application and ensures checks for appropriate permissions: `AddUserController`.
- Design of Bootstrap components for automatized registration: `MasterUsersBootstrap` and `BackofficeUsersBootstrapper`, both inheritors of `AbstractUserBootstrapper`.
- Use the framework's `UserManagementService` and `SystemUserBuilder` to handle the creation of a System User entity.
- Design of a domain object that is an extension of a `SystemUser` with the inclusion of additional attributes and uses its own email atribute as the primary key: `AlSafeUser`, with the Value Object `AlSafeUserEmail` as its primary key.
- Design of an entity for the valid email domains in the system: `EmailDomain`.
- Design of a repository and a Bootstrapper for `EmailDomain`: `EmailDomainRepository` and `EmailDomainBootstrapper`.
- Design of the necessary Value Objects for the `AlSafeUser`: `SecurityClearance`, `SkillsAssessment` and `AlSafeUserEmail`.
- Design of a Builder class responsible for creating AlSafe users: `AlSafeUserBuilder` 
- Design of a repository and its JPA and In-Memory implementations for AlSafe users persistence: `AlSafeUserRepository`, `JpaAlSafeUserRepository` and `InMemoryAlSafeUserRepository`.

#### Implement:

- `AddUserUI`: Presentation layer for gathering user data.
- `AddUserController`: Application layer to coordinate the registration process and enforce security.
- `AbstractUserBootstrapper` inheritors: Infrastructure layer for initial system setup (Bootstrap).
- `EmailDomain`: Domain entity for valid email domains in the system (by now, only `@alsafe.com` is certified and allowed). This entity is bootstrapped. 
- `UserManagementService`: Framework service for user operations.
- `SystemUserBuilder`: Framework builder for user creation.
- `AlSafeUserBuilder`: Builder for AlSafe users.
- `SystemUser`: Domain layer representing a user in the system.
- `AlSafeUser`, `AlSafeUserEmail`, `SecurityClearance` and `SkillsAssessment`: Domain layer representing an AlSafe user, which corresponds to a unique SystemUser but with additional attributes.
- `AlSafeUserRepository`: Infrastructure layer for AlSafeUser persistence.
- `Roles`: Domain layer defining the possible roles in the system.

#### Test:

- Unit tests for all the domain and controller classes involved in the user registration process.
- Manual verification of the UI and bootstrap process.

## 2. Requirements

**US031** As Administrator, I want to be able to register users of the backoffice. This must also be achieved by a bootstrap process.

**Acceptance Criteria:**

- Each user must be registered with a valid and unique email address.
- The backoffice users registration must be done manually (via UI) and via a bootstrap process.
- The users must be registered and persisted in the database.

**Dependencies/References:**

For the context of this user story, US031 depends on US030, that focuses on the authentication and authorization processes of each user. Each user registered in the backoffice system must be authenticated and authorized.

## 3. Analysis

The registration of backoffice users involves several key considerations:

1.  **User Roles:** Backoffice users include `Administrator` and `Backoffice Operator`. These roles are defined in the `Roles` class.
2.  **User Data:** A `SystemUser` requires a username, password, first name, last name, email address, and a role. An `AlSafeUser` would be an extension of a SystemUser that includes additional attributes, such as its own email address, phone number, skills assessment, and security clearance.
3.  **Uniqueness:** The email addresses must be unique within the system. `EmailAddress` is unique for system users and `AlSafeUserEmail` is the primary key of an `AlSafeUser`.
4.  **Validation of email domains:** The user must have a valid email domain. The list of valid email domains is bootstrapped in the system.
4.  **Security:** Access to register new users is restricted to users with `Administrator` or `Backoffice Operator` roles. This is enforced at the application layer in `AddUserController` by the user of the framework class `AuthorizationService`.
5.  **Persistence:** All users must be persisted in the database via `UserRepository` for `SystemUser` and `AlSafeUserRepository` for `AlSafeUser`.

The `eapli.framework` includes the `SystemUser`, `SystemUserBuilder` and `UserManagementService` classes to allow users to be created and persisted.

## 4. Design

The design separates concerns into different layers:

- **Presentation Layer:** `AddUserUI` handles user interaction and data input.
- **Application Layer:** `AddUserController` coordinates the use case, checking for authorization and delegating to the domain services.
- **Domain Layer:** `SystemUser`, `Roles`, `UserManagementService` (from the framework), `AlSafeUser` and its Value Objects represent the core logic and entities. The decision to create the entity `AlSafeUser` came from the fact that a `SystemUser` does not have the necessary attributes/VOs that an Alsafe user needs.
Also, the domain layer has the entity `EmailDomain`, responsible for creating valid email domains in the system. The list of those domains is bootstrapped in the system, and every time a user is being registered, the system validates its inputted email domain. If this validation fails, the user is not created.
- **Infrastructure Layer:** `UserRepository` (from the framework) and `AlSafeUserRepository` handles data persistence, and inheritors from `AbstractUserBootstrapper` provides automatic user registration during system startup.

### 4.1. Realization

**Manual Process**

![Sequence Diagram - Manual process](docs/US031/UI.svg)

**Bootstrap Process**

![Sequence Diagram - Bootstrap process](docs/US031/Bootstrap.svg)

### 4.2. Acceptance Tests

The validation of backoffice user registration is performed through both unit tests and manual verification.

**Manual Test 1: Register User via UI**
- **Action:** Log in as Administrator, navigate to user management, and register a new Backoffice Operator.
- **Expected Result:** Success message displayed, and the new user can log in to the system.

**Manual Test 2: Duplicate Email**
- **Action:** Attempt to register a user with an email that already exists.
- **Expected Result:** Error message indicating that the email is already in use.

**Unit Tests**
- **Action:** Test the domain and controller classes involved in the user registration process, such as `AlSafeUser` and its embedded Value Objects and `AddUserController`.
- **Expected Result:** All classes should pass their respective unit tests.

## 5. Implementation

The implementation leverages some classes from the `eapli.framework` for the development of the user management system.

### Key Classes

- `AddUserUI`: The presentation layer component that interacts with the administrator to gather user data (username, password, name, email, phone number, and roles) through the console.

- `AddUserController`: The application layer controller that coordinates the "Add User" use case. It ensures the performing user has the necessary permissions (Administrator or Backoffice Operator), registers the `SystemUser` via `UserManagementService`, and then creates and persists the corresponding `AlSafeUser`.

- `AbstractUserBootstrapper`: A base class for bootstrapping users into the system. It provides a common `registerUser` method that uses the `AddUserController` to ensure consistency between manual and automated registration.

- `MasterUsersBootstrapper`: A concrete bootstrapper that registers the initial system administrators and other essential users during the system's bootstrap process.

- `BackofficeUsersBootstrapper`: A concrete bootstrapper responsible for registering backoffice-specific users (e.g., Backoffice Operators) as part of the initial data setup.

- `SystemUserBuilder`: A builder class from the `eapli.framework` used to create `SystemUser` instances, ensuring that all mandatory framework-level attributes are correctly initialized.

- `UserManagementService`: A service from the `eapli.framework` that provides high-level operations for user management, such as registering new users, updating existing ones, and handling password policies.

- `UserRepository`: A repository from the `eapli.framework` that handles the persistence of `SystemUser` entities.

- `SystemUser`: A core domain entity from the `eapli.framework` representing a user with basic authentication and authorization details (username, password, roles, etc.).

- `AlSafeUserBuilder`: A custom domain builder responsible for creating `AlSafeUser` instances, simplifying the creation of this aggregate by handling its specific attributes and associated `SystemUser`.

- `AlSafeUser` and its Value Objects: `AlSafeUser` is the domain aggregate root that extends the framework's user concept with project-specific attributes. It uses Value Objects like `AlSafeUserEmail` (identity), `SecurityClearance`, and `SkillsAssessment` to encapsulate domain logic and validation. `phoneNumber` is a simple String attribute.

- `EmailDomain`, its repository and bootstrapper: An entity that represents a valid email domain in the system. The list of valid ones is kept in its repository and is created once the `Bootstrapper` runs.

- `Roles`: A class that defines the various roles available in the system (e.g., ADMIN, BACKOFFICE_OPERATOR), used for access control and user categorization.

## 6. Integration/Demonstration

To demonstrate the functionality:

1. Build the project (`build.bat` or `build.sh`)
2. Run the bootstrap process (usually via `run-bootstrap.bat` or `run-bootstrap.sh`).
3. Run the backoffice application (`run-backoffice.bat` or `run-backoffice.sh`).
4. Log in with the default administrator credentials created during bootstrap.
5. Use the "Add User" menu option to manually register a new user.
6. Verify the new user's existence in the database or by attempting to log in with their credentials.

## 7. Observations

- The use of `eapli.framework` greatly simplifies the implementation of user management, providing a robust builder and service classes for the creation of a `SystemUser`.
- Password policies and encoding are also managed by the framework, ensuring security best practices.
