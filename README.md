# cp2c

**CP2C** is the basic employee management application for the fictional organization MotorPH, built using [Javalin](https://javalin.io), a user-friendly Java framework designed for web or mobile applications and RESTful APIs. This system allows users to log in and perform CRUD (Create, Read, Update, Delete) operations on employee records. Using Javalin and a REST API architecture simplifies development and enhances scalability and management of employee data.

The projects aims to satisfy the requirements stated in the [MotorPH's Change Requests Form](https://docs.google.com/spreadsheets/d/1AHv2ht0gqcOINH_yn08s8NBn5DFM-7RIhZlnDWJyEpU/edit?usp=sharing):

- [MPHCR01-Feature 1](https://docs.google.com/spreadsheets/d/1AHv2ht0gqcOINH_yn08s8NBn5DFM-7RIhZlnDWJyEpU/edit?gid=475634283#gid=475634283)
- [MPHCR02-Feature 2](https://docs.google.com/spreadsheets/d/1AHv2ht0gqcOINH_yn08s8NBn5DFM-7RIhZlnDWJyEpU/edit?gid=1902740868#gid=1902740868)
- [MPHCR03-Feature 3](https://docs.google.com/spreadsheets/d/1AHv2ht0gqcOINH_yn08s8NBn5DFM-7RIhZlnDWJyEpU/edit?gid=28244578#gid=28244578)
- [MPHCR04-Feature 4](https://docs.google.com/spreadsheets/d/1AHv2ht0gqcOINH_yn08s8NBn5DFM-7RIhZlnDWJyEpU/edit?gid=299960867#gid=299960867)

**Rationale for Technology Choices**

Javalin was chosen for its ease of use and reduced configuration needs, enabling developers to focus on coding. Additionally, a JavaScript frontend framework replaces the original Java Swing, providing a dynamic and responsive user interface across devices.

## Project Background & Lineage

This project is a direct continuation, revision of a previous course project, [cp1](https://github.com/imperionite/cp1) and [cp2a](https://github.com/imperionite/cp2a), which served as the foundational phase for the MotorPH Employee Management System. The initial phase (cp1) focused on building a RESTful backend using Spring Boot and MySQL, implementing core payroll features such as:

- Employee information management
- Attendance tracking
- Automated payroll calculations
- Basic deductions and salary computation

The rationale, design decisions, and initial API documentation from **cp1** have been carried forward and expanded upon in this repository. Much of the documentation, architectural structure, and core modules in **CP2A** are derived from or inspired by the groundwork established in **cp1**. This ensures architectural consistency and provides a clear evolutionary path for the system.

**For detailed foundational documentation, refer to the [cp1 repository](https://github.com/imperionite/cp1).**  
All essential background, rationale, and initial API endpoint documentation are either included here or referenced directly from cp1 to avoid duplication and ensure clarity.

### What’s New in This Project

- **Frontend Integration:** A new React frontend (in the `GUI` folder) using Vite, running independently and communicating with the backend via REST API and CORS.
- **Improved Modularity:** Backend and frontend are decoupled for easier deployment and scalability.
- **Updated Documentation:** Enhanced and updated documentation, with references to class diagrams ([see here](https://github.com/imperionite/cp2a/blob/main/CLASS_DIAGRAM.md)) and new features added in this phase.

---

## CLI Commands

```sh
# ensure that employees.csv located in /src/main/resources exist
# and /data direcory at root of the project generated from last run is deleted
# clean & re-build project
mvn clean package
# clean, compile and execute one liner command
mvn clean compile exec:java
# check running process
lsof -i :4567
# stop the process
kill -9 <PID>

```