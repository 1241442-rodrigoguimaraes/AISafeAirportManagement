# US001

## 1. Context

In the context of the integrative project, the team must follow a set of technical constraints and concerns that guide the development of the system. These constraints ensure that the solution is built using the correct technologies, methodologies and architectural decisions, serving as the foundation for all subsequent development work.

### 1.1 List of issues

##### Analysis:

- Study of the technical constraints defined in section 5 of the requirements document: identify the mandatory technologies, programming languages, testing requirements and architectural decisions.

- Dependency Assessment: Identify the mandatory use of **Java** as the main programming language, **Maven** as the build automation tool, **Scrum** as the project management methodology, and **ANTLR** for DSL processing.

##### Design:

- Definition of the architectural decisions to be followed throughout the project, such as the use of **DDD** (Domain-Driven Design) and the layered architecture.

- Design of the persistence strategy: support for both **in-memory** and **relational database (RDBMS)** persistence.

##### Implement:

- Ensure the solution is implemented using **Java** as the main language, with other languages (e.g., **C**) used only where specifically required.

- Ensure that **authentication and authorization** are enforced for all users and functionalities.

- Ensure that code coverage of **domain and controller packages cannot fall below 90%**.

##### Test:

- Validate that the technical constraints are correctly applied across the project structure.

- Validate that the solution compiles and passes all tests in a clean environment.

## 2. Requirements

**US001 - As Project Manager, I want the team to follow the technical constraints and concerns of the project.**

**Acceptance Criteria:**

- US001.1 The solution must be implemented using **Java** as the main programming language.
- US001.2 **Maven** must be used as the build automation tool, with a **GitHub Actions** CI pipeline.
- US001.3 Code coverage of domain and controller packages must not fall below **90%** at any time.
- US001.4 The system must support data persistence either **in-memory** or via a **relational database (RDBMS)**.
- US001.5 The system must enforce **authentication and authorization** for all users and functionalities.
- US001.6 **Scrum** must be used as the project management methodology, with weekly meetings with the Scrum Master.
- US001.7 Documentation must follow **UML notation** and be maintained in the project repository in markdown format, using **PlantUML** whenever possible.

**Dependencies/References:**

This US is a prerequisite for all other user stories, as the technical constraints defined here apply to the entire project. It has a direct dependency on **US002** (project repository setup), **US003** (project structure) and **US004** (continuous integration server).

## 3. Realization

**Project Manager** → Defines and communicates the technical constraints to the team.

**Development Team** → Analyses the constraints and sets up the project structure accordingly.

**Java + Maven** → Used as the primary language and build tool throughout the project.

**ANTLR** → Used for lexical, syntactic and semantic processing of the Flight DSL.

**C Language** → Used specifically for the flight simulation component (US085, US100–US108).

**JaCoCo** → Enforces the 90% code coverage requirement on domain and controller packages.

**GitHub Actions** → Ensures continuous integration and validation of all technical constraints on every push.

**PlantUML** → Used to generate UML diagrams maintained in the repository's `docs` folder.

## 4. Acceptance Tests

**Test Case 1:** Verify that the project compiles successfully using Maven in a clean Unix-compatible environment, following the constraints defined in NFR10.

**Test Case 2:** Verify that the code coverage of domain and controller packages does not fall below 90%, as required by NFR03.

**Test Case 3:** Verify that the system supports both in-memory and relational database persistence, as required by NFR08.

**Test Case 4:** Verify that authentication and authorization are enforced for all users and functionalities, as required by NFR09.

## 5. Implementation

The technical constraints are enforced at multiple levels of the project:

The **programming language constraint** (NFR10) is enforced by the project structure itself, with Java as the primary language. C is used exclusively where required by specific user stories (simulation components).

The **testing constraint** (NFR03) is enforced via the JaCoCo plugin configured in the `pom.xml` files, ensuring that domain and controller code coverage does not fall below 90% at any time. Unit testing follows the **AAA (Arrange, Act, Assert)** convention.

The **persistence constraint** (NFR08) is enforced by supporting both an in-memory database solution (for development and testing) and a remote persistent relational database (for final deployment).

The **documentation constraint** (NFR02) is enforced by maintaining all documentation in the `docs` folder of the repository in markdown format, with UML diagrams generated using PlantUML.

The **Scrum methodology** (NFR01) is enforced by holding weekly meetings with the Scrum Master (LAPR4 PL teacher) and maintaining the project backlog and sprint planning in the GitHub project management tool.

## 6. Integration/Demonstration

This US does not produce a standalone deliverable. Its fulfilment is demonstrated through the correct application of the constraints across all other user stories and the overall project structure, validated at each sprint review.

## 7. Observations

The technical constraints defined in this US serve as the foundation for the entire project. Any deviation from these constraints must be discussed and validated with the course coordinators before implementation. Teams are encouraged to apply best practices acquired in previous semesters alongside those deepened throughout this semester.