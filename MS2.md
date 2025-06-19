# Milestone 2: MotorPH Employee Application with Add, Update, and Delete Features

## Table of Contents

1. [Project Overview](#project-overview)
2. [API Documentation](#api-documentation)
   - 2.1 [Manual API Tests](#manual-api-tests)
     - [Login](#login)
     - [Fetch All Employees](#fetch-all-employees)
     - [Fetch Employee by Employee Number](#fetch-employee-by-employee-number)
     - [Valid Monthly Salary Cut-Off](#valid-monthly-salary-cut-off)
     - [Compute Employee Salary & Deductions](#compute-employee-salary--deductions-based-employee-number-and-valid-cut-off-selected)
     - [Create New User](#create-new-user)
     - [Create New Employee](#create-new-employee)
     - [Update Employee](#update-employee-by-employee-number)
     - [Delete Employee](#delete-employee-by-employee-number)
   - 2.2 [API Endpoints Overview](#api-endpoints)
     - [View and Create Records](#api-documentation-view-and-create-records)
     - [View, Update, Delete Records](#api-documentation-view-update-delete-records)
3. [Notes](#notes)
4. [Video Demos](#video-demos)
5. [Client-Side GUI Screenshots](#client-side-gui-screenshots)
6. [Sample API Calls](#sample-api-calls)

## Project Overview

This milestone 2 project is designed to fulfill the requirements outlined in [MotorPH's Change Requests Form](https://docs.google.com/spreadsheets/d/1AHv2ht0gqcOINH_yn08s8NBn5DFM-7RIhZlnDWJyEpU/edit?usp=sharing), specifically:

- [MPHCR02-Feature 2](https://docs.google.com/spreadsheets/d/1AHv2ht0gqcOINH_yn08s8NBn5DFM-7RIhZlnDWJyEpU/edit?gid=1902740868#gid=1902740868)
- [MPHCR03-Feature 3](https://docs.google.com/spreadsheets/d/1AHv2ht0gqcOINH_yn08s8NBn5DFM-7RIhZlnDWJyEpU/edit?gid=28244578#gid=28244578)

**Note**:
In accordance with the course requirements and the mentor’s recommendation to use CSV files for data management, I have created a new project repository as a continuation of the [CP2A](https://github.com/imperionite/cp2a) project (Milestone 1), named **CP2C**. This new project differs in the technologies employed: it uses [Javalin](https://javalin.io), a Java-based microframework; CSV files instead of a database; and basic token authentication rather than `JWT`. The goal is to complete the project using these technologies through to the terminal assessment, prioritizing less framework's abstraction compared to the enterprise grade overhead of the previous poject done in MS 1, simplicity and modularity.

## API Documentation

All API endpoints in this project are protected routes except for the login endpoint. Role-based access control (RBAC) has not been applied, so both users and employees can perform actions as long as they are authenticated.

---

### Manual API Tests

#### Login

![Login](https://drive.google.com/uc?id=1_jmnNFwM-i-F9gY_idHDejNalcVKjZRl)

---

#### Fetch All Employees

![All Employees](https://drive.google.com/uc?id=1fVHl5c_0fc6ghhK1o8yIEd6t7zCxWhrc)

---

#### Fetch Employee by Employee Number

![Get Employee](https://drive.google.com/uc?id=1BrhK1GxThi2hk_yf2GivXfR_Iqmgi4Qg)

---

#### Valid Montly Salary Cut-Off

![Valid Monthly Cut Offs](https://drive.google.com/uc?id=12m5f3jAQBO40IPyc5Dqy1fTpLlmPScly)

---

#### Compute Employee Salary & Deductions Based Employee Number and Valid Cut Off Selected

![Compute Employee Salary and Deductions](https://drive.google.com/uc?id=10XwBFzFFVVGcHa6ubd_pkKnT5uPuoV6Z)

---

#### Create New User

![Create New User](https://drive.google.com/uc?id=1yzTLhkce4sdfE0_-jiQ2AEQJvn-B4imD)

---

#### Create New Employee

![Create New Employee](https://drive.google.com/uc?id=1BME2zsM5mmD4g3helCEosJGrjM_ww36E)

---

#### Update Employee by Employee Number

![Update Employee](https://drive.google.com/uc?id=1blFSZvSUlnSIZGLKKSbz6oDvKm-e_5dJ)

---

#### Delete Employee by Employee Number

![Delete Employee](https://drive.google.com/uc?id=136_uaC_uqb5biZyzF65M0v5MJw2dXM-n)

---

### API Endpoints

#### API Documentation: View and Create Records

| **Action**                    | **HTTP Method** | **Endpoint**                                | **Auth Required**  | **Request Body Example / Notes**                                                                                     |
| ----------------------------- | --------------- | ------------------------------------------- | ------------------ | -------------------------------------------------------------------------------------------------------------------- |
| **Login**                     | POST            | `/api/login`                                | No                 | `{ "username": "user-10034", "password": "userPassword" }`                                                           |
| **List all employees**        | GET             | `/api/protected/employees`                  | Yes (Bearer Token) | None                                                                                                                 |
| **Get full employee details** | GET             | `/api/protected/employees/{employeeNumber}` | Yes (Bearer Token) | Example: `/api/protected/employees/10010`                                                                            |
| **Register new user**         | POST            | `/api/register`                             | Yes (Bearer Token) | `{ "username": "user-10035", "password": "userPassword#" }`                                                          |
| **Create new employee**       | POST            | `/api/protected/employees`                  | Yes (Bearer Token) | Detailed employee JSON including employeeNumber, name, birthday, address, phone, IDs, status, position, salary, etc. |
| **Update employee by number** | PATCH           | `/api/protected/employees/{employeeNumber}` | Yes (Bearer Token) | Partial update JSON, e.g., `{ "firstName": "Juan", "lastName": "Dela Cruz" }`                                        |
| **Delete employee by number** | DELETE          | `/api/protected/employees/{employeeNumber}` | Yes (Bearer Token) | No body                                                                                                              |

---

#### API Documentation: View, Update, Delete Records

| **Action**                    | **HTTP Method** | **Endpoint**                                | **Auth Required**  | **Request Body Example / Notes**                                                                                     |
| ----------------------------- | --------------- | ------------------------------------------- | ------------------ | -------------------------------------------------------------------------------------------------------------------- |
| **Login**                     | POST            | `/api/login`                                | No                 | `{ "username": "user-10034", "password": "userPassword" }`                                                           |
| **List all employees**        | GET             | `/api/protected/employees`                  | Yes (Bearer Token) | None                                                                                                                 |
| **Get full employee details** | GET             | `/api/protected/employees/{employeeNumber}` | Yes (Bearer Token) | Example: `/api/protected/employees/10010`                                                                            |
| **Register new user**         | POST            | `/api/register`                             | Yes (Bearer Token) | `{ "username": "user-10035", "password": "userPassword#" }`                                                          |
| **Create new employee**       | POST            | `/api/protected/employees`                  | Yes (Bearer Token) | Detailed employee JSON including employeeNumber, name, birthday, address, phone, IDs, status, position, salary, etc. |
| **Update employee by number** | PATCH           | `/api/protected/employees/{employeeNumber}` | Yes (Bearer Token) | Partial update JSON, e.g., `{ "firstName": "Juan", "lastName": "Dela Cruz" }`                                        |
| **Delete employee by number** | DELETE          | `/api/protected/employees/{employeeNumber}` | Yes (Bearer Token) | No body                                                                                                              |

---

### Notes:

- Replace `{employeeNumber}` with actual employee number (e.g., 10010, 10035).
- Use the `Authorization: Bearer {{token}}` header for all protected endpoints.
- Default login credentials:
  - Admin: username `admin`, password `adminPassword`
  - User: username `user-{employeeNumber}`, password `userPassword`

---

### Video Demos

- [Manual API Tests](https://drive.google.com/file/d/1XRpQnfmdIi2cGKM86hdhWbnOZowG5QN0/view?usp=sharing)
- [Presentation on Client-Side GUI](https://drive.google.com/file/d/1OP4RsoPZEO3RLHX1VDMBomlUhFdAqS64/view?usp=sharing)

---

### Client-Side GUI Screenshots

**Login**

![Login](https://drive.google.com/uc?id=1ohgES3a6N0AZIaeIuxPXC5b2H82aoz0U)

**User Login Success**

![User Login Success](https://drive.google.com/uc?id=1Jcwtfic-fpPo4a0FXY6Xzs0QmQWXvcLh)

**Specific Employee Detail**

![Employee Detail](https://drive.google.com/uc?id=1tMgunhip_L6iUns_3rFHmYQsdjbsZKTs)

**View Employee Salary & Deductions**

![Employee Salary & Deductions](https://drive.google.com/uc?id=1zovo2NDst4qfwbqOClFlbvuuIReDRXQp)

**User Creation Success**

![User Creation Success](https://drive.google.com/uc?id=1yGPfyXT2LDzfZVWKJYWmH2mBn568mcnJ)

**User Creation Failed**

![User Creation Failed](https://drive.google.com/uc?id=1z0R5fujrD61bmL2htYC76u4RjXPtT75w)

**Employee Creation**

![Employee Creation](https://drive.google.com/uc?id=1su2q8-ROciQnPbV1rzIC7_prpbieL9Qr)

**Employee Creation Success**

![Employee Creation Success](https://drive.google.com/uc?id=1yuXJeG82-72IWQ30mR9FC3dfAnhonvI3)

**New Employee Created**

![New Employee Created](https://drive.google.com/uc?id=12nX5V16-gpN0n7c10pMO_xah6TM2Lo5k)

**Specific Employee Detail**

![Employee Detail](https://drive.google.com/uc?id=1tMgunhip_L6iUns_3rFHmYQsdjbsZKTs)

**Auth User Update Employee Information**

![Update Employee Detail](https://drive.google.com/uc?id=1WzhEUseji0LDSiXhtczAMIBtmeLS6V6l)

**Update Employee Information Success**

![Update Employee Success](https://drive.google.com/uc?id=1pC48Uct5FTiJmdDhqLE1OFuDBV3HXcIY)

**Deleting Employee/User**

![Delete Employee/User](https://drive.google.com/uc?id=11WcbNWP3aG6UB_bXL6cAdBpSrGUFVSDD)

---

### Sample API Calls

Please check this [link](https://github.com/imperionite/cp2c/blob/main/rest.http).
