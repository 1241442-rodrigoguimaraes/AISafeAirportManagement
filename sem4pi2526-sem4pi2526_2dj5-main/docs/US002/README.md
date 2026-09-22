# US002 – Project Repository Setup

## 1. Context

This user story is part of Sprint 1 and focuses on establishing the project repository and basic project management tooling using GitHub.

According to the project requirements, the team must use the provided GitHub repository and configure a project management tool to support development and tracking.

---

## 2. Objective

The objective of this user story is to create a structured and well-organized repository that:

* Supports collaborative development
* Enables proper task tracking
* Ensures traceability between work and user stories
* Follows best practices in version control

---

## 3. Implementation

### 3.1 Repository Setup

The team used the GitHub Classroom repository provided for the project.

The following structure was defined:

```
/docs
/tests
```

* `docs/` → project documentation (UML diagrams, reports, README files)
* `tests/` → automated tests

---

### 3.2 Branch Strategy

A simple and effective branching strategy was adopted:

* `main` → stable version of the system
*  branches → if used for development of each user story

Branch naming convention:

```
feature/USXXX-description
```

Example:

```
feature/US002-repository-setup
```

---

### 3.3 Commit Rules

Commits follow a standardized format to ensure traceability:

```
 Short description of the change (the respective # of the US)
```

Example:

```
Setup project structure and initial README (#2)
```

This allows easy identification of which user story each change belongs to.

---

### 3.4 GitHub Project Management

A GitHub Project board was created to manage the sprint workflow.

The board includes the following columns:

* Backlog
* To Do
* In Progress
* Done

Each User Story is:

* Created as a GitHub Issue
* Assigned to team members
* Tracked through the project board
* Linked to commits

---

### 3.5 Issues Management

Tasks are managed using GitHub Issues.

Each issue includes:

* Title (e.g., `US002 – Setup repository structure`)
* Labels (e.g., `US002`, `Sprint1`)
* Description with acceptance criteria
* Assignment to team members

---

## 4. Acceptance Criteria

* Repository is created and accessible
* Folder structure is defined
* GitHub Project board is configured
* Issues are created and linked to US002
* Team members actively use the repository

---

## 5. Validation

The repository is actively used by all team members:

* Regular commits are performed
* Issues reflect the work being developed
* The project board tracks progress clearly

This ensures alignment with project requirements and demonstrates continuous work throughout the sprint.

---

## 6. Conclusion

The repository setup provides a solid foundation for the project by ensuring:

* Efficient collaboration between team members
* Clear organization of code and documentation
* Proper tracking of tasks and user stories

This setup will support the development of all future user stories in a structured and scalable way.
