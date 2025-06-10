# Laboratory Work #5 & Homework #5: MotorPH Employee Application with Record Update and Delete Functionalities

This laboratory exercise and homework are designed to fulfill the requirements outlined in [MotorPH's Change Requests Form](https://docs.google.com/spreadsheets/d/1AHv2ht0gqcOINH_yn08s8NBn5DFM-7RIhZlnDWJyEpU/edit?usp=sharing), specifically:

- [MPHCR03-Feature 3](https://docs.google.com/spreadsheets/d/1AHv2ht0gqcOINH_yn08s8NBn5DFM-7RIhZlnDWJyEpU/edit?gid=28244578#gid=28244578)

**Note**:
In accordance with the course requirements and the mentor’s recommendation to use CSV files for data management, I have created a new project repository as a continuation of the `CP2A` project (Milestone 1), named **CP2C**. This new project differs in the technologies employed: it uses [Javalin](https://javalin.io), a Java-based microframework; CSV files instead of a database; and basic token authentication rather than JWT. The goal is to complete the project using these technologies through to the terminal assessment, prioritizing less framework's abstraction compared to the enterprise overhead of the previous poject done in MS 1, simplicity and modularity.

---

## Manual API Tests: View, Create, Update, Delete Records

- [Video Demo Link](https://drive.google.com/file/d/1XRpQnfmdIi2cGKM86hdhWbnOZowG5QN0/view?usp=sharing)

| **Action**                    | **HTTP Method** | **Endpoint**                                | **Auth Required**  | **Request Body Example / Notes**                                                                                     |
| ----------------------------- | --------------- | ------------------------------------------- | ------------------ | -------------------------------------------------------------------------------------------------------------------- |
| **Login**                     | POST            | `/api/login`                                | No                 | `{ "username": "user-10034", "password": "userPassword" }`                                                           |
| **List all employees**        | GET             | `/api/protected/employees`                  | Yes (Bearer Token) | None                                                                                                                 |
| **Get full employee details** | GET             | `/api/protected/employees/{employeeNumber}` | Yes (Bearer Token) | Example: `/api/protected/employees/10010`                                                                            |
| **Register new user**         | POST            | `/api/register`                             | Yes (Bearer Token) | `{ "username": "user-10035", "password": "userPassword#" }`                                                          |
| **Create new employee**       | POST            | `/api/protected/employees`                  | Yes (Bearer Token) | Detailed employee JSON including employeeNumber, name, birthday, address, phone, IDs, status, position, salary, etc. |
| **Update employee by number** | PATCH           | `/api/protected/employees/{employeeNumber}` | Yes (Bearer Token) | Partial update JSON, e.g., `{ "firstName": "Juan", "lastName": "Dela Cruz" }`                                        |
| **Delete employee by number** | DELETE          | `/api/protected/employees/{employeeNumber}` | Yes (Bearer Token) | No body                                                                                                              |

### Notes:

- Replace `{employeeNumber}` with actual employee number (e.g., 10010, 10035).
- Use the `Authorization: Bearer {{token}}` header for all protected endpoints.
- Default login credentials:
  - Admin: username `admin`, password `adminPassword`
  - User: username `user-{employeeNumber}`, password `userPassword`

## GUI: View, Update, Delete Records

- [Video Demo Link](https://drive.google.com/file/d/1OP4RsoPZEO3RLHX1VDMBomlUhFdAqS64/view?usp=sharing)
