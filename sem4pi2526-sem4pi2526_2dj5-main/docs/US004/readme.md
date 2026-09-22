# US004

## 1. Context

In the context of the integrative project, it's of paramount importance for the team to work in the most effective, coherent, cautious and automatized way possible. That way, it's possible to avoid the integration of errors and to ensure the
quality of the software in general. Thus, to achieve this goal, the team must set up a continuous integration server, enjoying tools available in the GitHub platform, such as the Actions/Workflows.

### 1.1 List of issues

##### Analysis:

- Study of the requirements for the CI setup: analyze the restrictions for the implementation of the CI server, such as the use of **GitHub Actions/Workflows** and the not acceptance of Domain and Controller code tests with less than **90% of coverage**.

- Dependency Assessment: Identify the necessary versions of JDK and building tools shown in the `pom.xml` document (Java 17/Maven) to replicate the environment in the server.

##### Design:

- Design of the **YAML Workflow** to be used in the CI server: push trigger and list of jobs to be executed.

##### Implement:

- Creation of the **.YML** file in .github directory in the repository.

- Update the main pom.xml file to include the JaCoCo plugin for data collection and general reports generation.

- Update the pom.xml file in the 'exemplo.core' directory to include the JaCoCo plugin for checking the code tests coverage for 'domain' and 'controller' layers.

##### Test:

- Test if the CI server is correctly being actioned when a push is made to the repository.

- Validate if the server fails when the code coverage is below 90%.

## 2. Requirements

**US004 - As Project Manager, I want the team to set up a continuous integration server. GitHub Actions/Workflows should be used.**

**Acceptance Criteria:**

- US004.1 The setup of the continuous integration server must be done using GitHub Actions/Workflows.
- US004.2 Code coverage must be at least 90% on the tests for all classes and methods in the Domain and Controller layers.

**Dependencies/References:**

Aiming to cohere the work developed in this US and the next one, the build process evoked by the CI server has to use the script developed in US005: `build.sh`.

## 3. Realization

**Team Member** → Commits and pushes his/her changes to the remote repository.

**GitHub Actions** → Detects the push event and reserves a runner 'ubuntu-latest'.

**Runner** → Executes the code's checkout.

**Runner** → Configures JDK 17 and Maven.

**Runner** → Executes US005 script (`build.sh`), which triggers Maven.

**JaCoCo** → Collects the code coverage data.

**Maven Check** → Compares the code coverage with the minimum requirement established in the pom.xml file.

**GitHub** → Reports the status of the build process (Success/Fail).

## 4. Acceptance Tests

**Test Case 1:** Commit and push the implementation of the `ci.yml` and the addition of the JaCoCo plugins to both pom.xml files to the remote repository and check if the CI server is correctly being actioned and reporting the validation of the building process.

**Test Case 2:** Verify if the building process fails when the tests coverage is below 90%.

## 5. Implementation

The JaCoCo configuration is decentralized: the 'prepare-agent' is maintained in the Root POM (`eapli.base\pom.xml`) to ensure the collection of metrics in all modules, while the 'check' rule (90% of coverage) is applied specifically to the 'exemplo.core' module
(`eapli.base\exemplo.core\pom.xml`), fulfilling the quality requirement focused on domain logic and control.

The choosing of this decentralized structure allows the whole project (all of its modules) to be monitored by the JaCoCo agent. The isolation of the 90% rule of coverage in the 'exemplo.core' focuses the quality control in the business logic and control layer.

The `ci.yml` file contains the definition of the workflow, which is executed when a push is made to the repository and evokes the script `build.sh` to perform the build process.

The **GitHub Actions** implemented in the CI server are:
    
* `action/checkout@v4.0.0` : responsible for bringing the code to the Runner, so it can have access to the files and compile them.

* `actions/setup-java@v4.0.0` : responsible for installing the JDK 17, dealing with the Maven cache and configuring the necessary environment variables.

## 6. Integration/Demonstration

![Successful Workflow](/docs/US004/images/Successful.png)

## 7. Observations

None.
