# US003 - Project Structure Setup

## 1. Context

This user story focuses on establishing the base project structure for the **AISafe** system. It is assigned to Sprint 1 and serves as the architectural foundation for all future development. The setup is heavily based on the `eapli.base` reference project provided by the course coordinators, ensuring that the team strictly follows the required **Onion Architecture** and a Maven multi-module configuration.

### 1.1 List of issues

**Analysis:**
- Study the `eapli.base` reference project to understand the existing module division (core, persistence, applications, bootstrappers, etc.).
- Identify the specific additions needed to accommodate the AISafe system requirements (ANTLR4 for LPROG, JaCoCo coverage for NFR03, and necessary application clients).

**Design:**
- Define the new package naming convention (`alsafe.*` replacing `exemplo.*`).
- Plan the integration of the new Air Transport Company Collaborator (ATCC) application module alongside the existing backoffice and user consoles.

**Implement:**
- Refactor the base project, renaming artifacts, group IDs, and folder structures.
- Inject the required plugins and dependencies (ANTLR4, JaCoCo) in the correct scopes.

**Test:**
- Ensure the parent Maven project compiles correctly (`mvn clean install`) and that plugins execute as expected without breaking the established architecture.

## 2. Requirements

The primary requirement is to set up our project structure by adapting `eapli.base` to our system (`alsafe`).

**Acceptance Criteria:**
- **US003.1:** Rename everything from `exemplo` to `alsafe` across all module names, package names, `pom.xml` files, and folder paths.
- **US003.2:** Maintain the existing structure and all base `pom.xml` configurations intact without changing the architecture.
- **US003.3:** Include the ANTLR4 Maven plugin and dependency in `alsafe.core` (pointing to `src/main/antlr4/`), required by US083 and NFR11 (LPROG).
- **US003.4:** Configure the JaCoCo plugin to enforce a 90% line coverage minimum specifically on the `domain` and `controllers` packages, as required by NFR03.
- **US003.5:** Create a new module `alsafe.app.atcc.console` mirroring the structure of the existing user console, meant for Air Transport Company Collaborators, Weather Persons, and Pilots (required by US044, US078, US086 - RCOMP).

## 3. Analysis

The team analyzed the provided `eapli.base` code. The project is natively structured to decouple business logic (`core`) from infrastructure (`persistence.impl`) and presentation (`app.*.console`).

To avoid over-engineering, the team decided to strictly adhere to the base configuration, applying only the modifications explicitly justified by the requirements document. The most significant architectural addition is the `alsafe.app.atcc.console`, which will act as a TCP-based client application in future USes.

## 4. Design & Implementation

The implementation consisted of a comprehensive refactoring of the initial POM structure and folder directories.

Major changes implemented:
1. **Parent POM (`alsafe/pom.xml`):** Updated the `<modules>` list to include all renamed `alsafe.*` modules and the newly created `<module>alsafe.app.atcc.console</module>`.
2. **Core Module (`alsafe.core/pom.xml`):** - Added the `org.antlr:antlr4-runtime` dependency.
    - Added the `antlr4-maven-plugin` configured to process grammars in `${basedir}/src/main/antlr4`.
    - Updated the `jacoco-maven-plugin` rule to filter specifically for `<include>**/domain/**</include>` and `<include>**/controllers/**</include>`, enforcing the `<minimum>0.90</minimum>` covered ratio limit.
3. **ATCC Module (`alsafe.app.atcc.console`):** Created a new Maven module depending on `alsafe.app.common.console`, `alsafe.core`, and `alsafe.persistence.impl` (runtime scope), identical to the standard user console, serving as the foundation for future network-based interactions.

## 5. Integration/Demonstration

Because this is a structural User Story, there is no standalone graphical interface or end-user functionality to demonstrate.

The success of this integration is verified via the CI pipeline or by running the following Maven command in the root directory:
```bash
mvnw clean verify