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

- **Ensure** `employees.csv` exists in `/src/main/resources` and delete `/data` directory generated from last run.

- **Clean and re-build project:**
  ```sh
  mvn clean package
  ```
  
- **Run the jar file after deleting the `data` directory: The app run at port `4567`**
  ```sh
  rm -rf data && mvn clean package && java -jar target/cp2c-1.0-SNAPSHOT.jar
  ```
- **Check running process on port 4567:**
  ```sh
  lsof -i :4567
  ```
- **Stop the process by PID:**
  ```sh
  kill -9
  ```
- **Clear your local Maven repository cache for Javalin:**
  ```sh
  rm -rf ~/.m2/repository/io/javalin/
  ```
- **Force Maven to update dependencies:**
  ```sh
  mvn clean install -U
  ```
- **Clean NetBeans cache and reload project:**

  ```sh
  rm -rf ~/Library/Caches/NetBeans/
  ```

---

## Data Seeding and Storage Management with CSV Files

This section outlines the data seeding process for users and employees, along with how data is managed using CSV files within this application.

### Overview of Data Storage

This application uses CSV files for persistent data storage, primarily for users and employees. This approach offers a lightweight, human-readable way to manage core application data without requiring a full-fledged database.

The CSV files are handled in two distinct ways:

- **Resource CSVs (src/main/resources)**: These are static files bundled with the application's JAR. They serve as default templates or initial data.
- **Dynamic Data Directory (data/)**: This directory is located outside the application's build path (e.g., alongside the JAR file). Files within this directory are dynamic and mutable, meaning they can be read from and written to during runtime via API endpoints. This is where the current state of data resides.

#### Initial Data Seeding Process (Main.java)

The `Main.java` class is responsible for the application's bootstrap process, which includes the initial seeding of data:

#### Ensuring the `data/` Directory Exists

Upon startup, `Main.java` checks for the existence of the `data/` directory:

```java
Path dataDirPath = Paths.get("data");
if (!Files.exists(dataDirPath)) {
    Files.createDirectories(dataDirPath); // Creates the directory if it doesn't exist
    System.out.println("Created data directory: " + dataDirPath.toAbsolutePath());
}
```

This step ensures that there is a designated location for dynamic data files.

##### Initial `employees.csv` Copy

The `employees.csv` file is initially loaded from `src/main/resources`, but it is intended to be dynamic. `Main.java` performs the following copy operation to achieve this:

```java
Path targetEmployeeCsvPath = Paths.get(EMPLOYEES_CSV_FILE_PATH); // data/employees.csv
boolean targetEmployeeCsvExistsAndHasContent = Files.exists(targetEmployeeCsvPath)
        && (Files.size(targetEmployeeCsvPath) > 0);

if (!targetEmployeeCsvExistsAndHasContent) {
    // If data/employees.csv doesn't exist or is empty, copy from resources
    try (InputStream is = Main.class.getResourceAsStream(INITIAL_EMPLOYEES_CSV_RESOURCE)) {
        if (is != null) {
            Files.copy(is, targetEmployeeCsvPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Successfully copied initial employees.csv from resources to: " + targetEmployeeCsvPath.toAbsolutePath());
        }
    } catch (IOException e) {
        // Handle exception (log it, etc.)
    }
} else {
    System.out.println("Existing employees.csv found in data/. Skipping initial data copy from resources.");
}
```

##### Logic:

If `data/employees.csv` does not exist or is empty, the application copies the default `employees.csv` from the `src/main/resources` folder into the `data/` directory.

##### Purpose:

This ensures that on the first run, the application has a foundational set of employee data. Subsequent runs will use the existing `data/employees.csv`, preserving any changes made via API.

### User Seeding

After ensuring that `employees.csv` is available, `Main.java` proceeds to seed initial user accounts based on the loaded employee data:

```java
List<Employee> allEmployees = employeeService.getAllEmployees(); // Fetches employees from data/employees.csv
if (allEmployees.isEmpty()) {
    System.out.println("No employees found... Skipping user seeding.");
} else {
    for (Employee employee : allEmployees) {
        String employeeNumber = employee.getEmployeeNumber();
        String username = "user-" + employeeNumber; // Creates a username based on employee number
        if (userDao.findByUsername(username).isEmpty()) {
            authService.registerUser(username, SEED_USER_PASSWORD); // Registers the user
            System.out.println("Seeding user: " + username);
        } else {
            System.out.println("User '" + username + "' already exists. Skipping seeding for this user.");
        }
    }
}
```

#### Dependency:

User seeding is dependent on the data in `employees.csv`. For each employee, a corresponding user account is created with a default password (`userPassword`).

##### Preventing Duplicates:

The application checks if a user already exists to avoid re-seeding accounts on subsequent restarts.

#### `users.csv` Creation:

The `UserDao` manages the `users.csv` file within the `data/` directory. If `data/users.csv` does not exist, `UserDao` will create it with a header. User records are then appended to this file as needed.

### Data Storage Management (CSV Data Access Objects - DAOs)

The application uses dedicated Data Access Objects (DAOs) to interact with the CSV files.

#### `UserDao` (data/users.csv)

- **Purpose**: Manages user authentication data (username, hashed password, roles).
- **File Location**: Always reads from and writes to `data/users.csv`.
- **Lifecycle**:

  - Upon initialization, `UserDao` attempts to load existing users from `data/users.csv`.
  - If the file does not exist, it is automatically created with a header when the first user is registered.
  - User registrations and updates are written back to the file to ensure persistence.

#### `EmployeeDao` (data/employees.csv)

- **Purpose**: Manages detailed employee records.
- **File Location**: After the initial copy by `Main.java`, it reads from and writes to `data/employees.csv`.
- **Lifecycle**:

  - On initialization, `EmployeeDao` loads employee data from `data/employees.csv`.
  - Any `addEmployee`, `updateEmployee`, or `deleteEmployee` operation performed via the `EmployeeService` (and API endpoints) triggers a write operation to `data/employees.csv`, overwriting the entire file with the current in-memory state. This ensures that changes are persisted dynamically.

##### Parsing Logic:

Robust parsing logic (such as `parseBigDecimal`) is included to handle common CSV quirks, like commas and quotation marks within numeric fields, and convert them correctly to `BigDecimal`.

### `CSVUtils` Helper Class

Both `UserDao` and `EmployeeDao` internally use a shared `CSVUtils` class. This utility provides generic methods for:

- **`loadFromCsv()`**: Reads lines from a specified CSV file and applies a provided parser function to convert each line into an object.
- **`saveToCsv()`**: Writes a list of objects back to a specified CSV file, using a provided function to map each object back to a CSV line.

This centralizes CSV handling logic and promotes reusability.

---

## Salary Calculation Logic

This document outlines the logic used in the `SalaryCalculatorService` to calculate the monthly gross salary, deductions, and net salary for employees. The service is designed to be flexible, pulling rules and data from CSV and JSON files, allowing it to adapt to different compensation structures and regulatory changes.

### Data Sources

The salary calculation process relies on the following data files:

- **`data/employees.csv`**: Contains detailed information for each employee, including their basic salary and fixed allowances (Rice Subsidy, Phone Allowance, Clothing Allowance), along with other personal and professional details. This file is dynamically managed and can be updated through dedicated API endpoints.

- **`src/main/resources/attendance.csv`**: Contains daily login and logout times for employees. This is a static resource.

- **`src/main/resources/contributions.json`**: Defines the rules and rates for various government-mandated deductions (SSS, PhilHealth, Pag-IBIG) and withholding tax. This is a static resource.

#### Key Definitions

- **STANDARD_WORK_HOURS_PER_DAY**: Defined as 8 hours.
- **STANDARD_WORK_DAYS_PER_MONTH**: Defined as 22 days.
- **STANDARD_MONTHLY_HOURS**: Calculated as `STANDARD_WORK_HOURS_PER_DAY × STANDARD_WORK_DAYS_PER_MONTH`, which equals `8 hours/day × 22 days/month = 176 hours/month`. This represents the full expected working hours in a standard month for a full basic salary.

### Calculation Steps

The `calculateMonthlySalary` method follows a structured approach:

#### Monthly Worked Hours

- For a given `employeeNumber` and `yearMonth`, the service filters all relevant attendance records from `attendance.csv`.
- For each attendance record, the duration between `loginTime` and `logoutTime` is calculated.
- **Daily Hour Cap**: Each day's worked hours are capped at `STANDARD_WORK_HOURS_PER_DAY` (8 hours). This means that even if an employee works more than 8 hours in a day, only 8 hours will count toward the salary calculation.
- **Summation**: The total daily worked hours (capped) for the target `yearMonth` are summed up to get `totalActualWorkedHours`.

#### Prorated Basic Salary

The basic salary component is prorated based on the `totalActualWorkedHours` relative to the `STANDARD_MONTHLY_HOURS`:

- If `totalActualWorkedHours` is greater than or equal to `STANDARD_MONTHLY_HOURS`, the `proratedBasicSalary` is equal to `employee.getBasicSalary()`.
- If `totalActualWorkedHours` is less than `STANDARD_MONTHLY_HOURS`, the `proratedBasicSalary` is calculated as:

  ```plaintext
  Prorated Basic Salary = Basic Salary × (Standard Monthly Hours / Total Actual Worked Hours)
  ```

#### Gross Monthly Salary

The `grossMonthlySalary` is the sum of the `proratedBasicSalary` and the fixed allowances:

```plaintext
Gross Monthly Salary = Prorated Basic Salary + Rice Subsidy + Phone Allowance + Clothing Allowance
```

**Note**: Fixed allowances (Rice Subsidy, Phone Allowance, Clothing Allowance) are currently always added to the gross salary, regardless of `totalActualWorkedHours`. If the business rule requires these allowances to be prorated or withheld for zero worked hours, the logic will need to be adjusted.

#### Deductions

Once the gross salary is calculated, various government-mandated deductions are applied, with rules defined in `contributions.json`.

##### SSS (Social Security System) Contribution

- The `monthlySssDeduction` is determined by finding the `SSSContributionRule` where the `grossMonthlySalary` is less than or equal to the salary cap defined in the rules.
- The corresponding contribution amount from that rule is applied. The rules in `contributions.json` are sorted by salary cap to ensure correct lookup.

##### PhilHealth Contribution

- The `monthlyPhilhealthDeduction` is determined by finding the `PhilHealthContributionRule` where the `grossMonthlySalary` falls within the `minSalary` and `maxSalary` range.
- If a fixed `EmployeeContribution` is defined for that rule, that amount is used.
- Otherwise, if a rate is defined, the employee's share is calculated as:

  ```plaintext
  Employee Share = grossMonthlySalary × (Rate / 2)
  ```

  (assuming a 50/50 employer/employee split for the total rate).

##### Pag-IBIG (Home Development Mutual Fund) Contribution

- The Pag-IBIG contribution is calculated based on the `PagIbigContributionRule` that matches the `grossMonthlySalary`.

- A `pagibigFundSalaryCreditCap` of P5000 is applied, meaning the calculation considers the `grossMonthlySalary` or P5000, whichever is lower.

- The contribution is calculated as:

  ```plaintext
  Pag-IBIG Contribution = applicableSalary × contributionRate
  ```

- A maximum employee contribution of P100.00 is enforced.

##### Withholding Tax

- **Taxable Income**: Before calculating the withholding tax, the `taxableIncome` is determined by subtracting the employee's share of SSS, PhilHealth, and Pag-IBIG contributions from the `grossMonthlySalary`:

  ```plaintext
  Taxable Income = Gross Monthly Salary - (Monthly SSS Deduction + Monthly PhilHealth Deduction + Monthly Pag-IBIG Deduction)
  ```

- The `monthlyWithholdingTax` is then calculated by finding the `WithholdingTaxRule` where the `taxableIncome` falls within the `minTaxableIncome` and `maxTaxableIncome` range. The tax is a combination of:

  - A `fixedTax` amount
  - A `percentageOverExcess` amount over the excess taxable income, as defined in the rule.

##### Total Deductions

The total deductions are the sum of all calculated deductions:

```plaintext
Total Deductions = Monthly SSS Deduction + Monthly PhilHealth Deduction + Monthly Pag-IBIG Deduction + Monthly Withholding Tax
```

##### Net Monthly Salary

The `netMonthlySalary` is calculated as:

```plaintext
Net Monthly Salary = Gross Monthly Salary - Total Deductions
```

### Handling Missing Attendance Data

To prevent inaccurate salary calculations for months with no attendance records, the `calculateMonthlySalary` method checks if `employeeMonthlyAttendance` is empty. If no attendance records are found for the specified `employeeNumber` and `yearMonth`, an `IllegalArgumentException` is thrown. This results in a 404 Not Found response from the API, indicating that the salary cannot be calculated for that period due to the lack of data.

### Fetching Valid Year-Months

To assist front-end applications and users in querying valid periods, the API provides the following endpoint:

**`GET /api/protected/monthly-cutoffs`**

This endpoint returns a list of `MonthlyCutoff` objects, each representing a unique year-month that has at least one attendance record in `attendance.csv`. This ensures that only periods with existing attendance data can be queried for salary calculations, improving data integrity and user experience.
