package com.imperionite.cp2c.controller;

import com.imperionite.cp2c.dto.EmployeeDto;
import com.imperionite.cp2c.dto.MessageResponse;
import com.imperionite.cp2c.model.Employee;
import com.imperionite.cp2c.service.EmployeeService;
import io.javalin.Javalin;

/**
 * Controller for handling employee-related API endpoints.
 * All routes under `/api/protected/*` are protected and require authentication
 * (handled by AuthController's before filter).
 */
public class EmployeeController {

    /**
     * Registers all employee routes.
     *
     * @param app             The Javalin app instance to register routes with.
     * @param employeeService The EmployeeService instance to use for business logic.
     */
    public static void registerRoutes(Javalin app, EmployeeService employeeService) {

        // All routes for EmployeeController are typically under /api/protected/
        // The authentication filter for /api/protected/* is set up in AuthController.
        // It's crucial that AuthController.registerRoutes(app, authService) is called
        // *before* EmployeeController.registerRoutes(app, employeeService) in your Main/App class setup.

        // GET /api/protected/employees - List all employees with summary attributes
        app.get("/api/protected/employees", ctx -> {
            System.out.println("EmployeeController: Fetching all employee summaries.");
            ctx.json(employeeService.getAllEmployeeSummaries());
        });

        // GET /api/protected/employees/:employeeNumber - Get full details of a single employee
        app.get("/api/protected/employees/{employeeNumber}", ctx -> {
            String employeeNumber = ctx.pathParam("employeeNumber");
            System.out.println("EmployeeController: Fetching details for employee number: " + employeeNumber);

            Employee employee = employeeService.getEmployeeDetails(employeeNumber);

            if (employee != null) {
                ctx.status(200); // OK
                ctx.json(new EmployeeDto(employee)); // Convert Employee model to DTO for response
            } else {
                ctx.status(404); // Not Found
                ctx.json(new MessageResponse("Employee with number " + employeeNumber + " not found."));
            }
        });

        // POST /api/protected/employees - Create a new employee
        app.post("/api/protected/employees", ctx -> {
            System.out.println("EmployeeController: Received create employee request.");

            // Convert request body DTO to Employee model for service layer
            EmployeeDto newEmployeeDto = ctx.bodyAsClass(EmployeeDto.class);
            Employee newEmployee = newEmployeeDto.toEmployee();

            if (newEmployee.getEmployeeNumber() == null || newEmployee.getEmployeeNumber().isEmpty()) {
                ctx.status(400); // Bad Request
                ctx.json(new MessageResponse("Employee number is required to create a new employee."));
                return; // Stop further processing
            }

            try {
                Employee createdEmployee = employeeService.createEmployee(newEmployee);
                ctx.status(201); // Created
                System.out.println("EmployeeController: Employee " + createdEmployee.getEmployeeNumber() + " created successfully.");
                ctx.json(new EmployeeDto(createdEmployee)); // Convert back to DTO for response
            } catch (IllegalArgumentException e) {
                ctx.status(400); // Bad Request due to validation error
                System.err.println("EmployeeController: Failed to create employee: " + e.getMessage());
                ctx.json(new MessageResponse(e.getMessage()));
            } catch (Exception e) {
                ctx.status(500); // Internal Server Error
                System.err.println("EmployeeController: Error creating employee: " + e.getMessage());
                e.printStackTrace();
                ctx.json(new MessageResponse("Internal server error during employee creation."));
            }
        });

        // PATCH /api/protected/employees/:employeeNumber - Partially update an existing employee
        app.patch("/api/protected/employees/{employeeNumber}", ctx -> {
            String employeeNumber = ctx.pathParam("employeeNumber");
            System.out.println("EmployeeController: Received PATCH employee request for: " + employeeNumber);

            // Convert request body DTO to Employee model (only for fields to update)
            EmployeeDto partialEmployeeDto = ctx.bodyAsClass(EmployeeDto.class);
            Employee partialEmployee = partialEmployeeDto.toEmployee(); // This will create an Employee object, some fields may be null

            try {
                Employee resultEmployee = employeeService.updateEmployee(employeeNumber, partialEmployee);
                if (resultEmployee != null) {
                    ctx.status(200); // OK
                    System.out.println("EmployeeController: Employee " + employeeNumber + " updated successfully.");
                    ctx.json(new EmployeeDto(resultEmployee)); // Convert back to DTO for response
                } else {
                    ctx.status(404); // Not Found
                    System.out.println("EmployeeController: Employee " + employeeNumber + " not found for update.");
                    ctx.json(new MessageResponse("Employee with number " + employeeNumber + " not found."));
                }
            } catch (IllegalArgumentException e) {
                ctx.status(400); // Bad Request due to validation error
                System.err.println("EmployeeController: Failed to update employee: " + e.getMessage());
                ctx.json(new MessageResponse(e.getMessage()));
            } catch (Exception e) {
                ctx.status(500); // Internal Server Error
                System.err.println("EmployeeController: Error updating employee: " + e.getMessage());
                e.printStackTrace();
                ctx.json(new MessageResponse("Internal server error during employee update."));
            }
        });

        // DELETE /api/protected/employees/:employeeNumber - Delete an employee and associated user
        app.delete("/api/protected/employees/{employeeNumber}", ctx -> {
            String employeeNumber = ctx.pathParam("employeeNumber");
            System.out.println("EmployeeController: Received delete employee request for: " + employeeNumber);

            try {
                boolean deleted = employeeService.deleteEmployee(employeeNumber);
                if (deleted) {
                    ctx.status(204); // No Content - Javalin handles empty body for 204
                    System.out.println("EmployeeController: Employee " + employeeNumber + " and associated user deleted successfully.");
                } else {
                    ctx.status(404); // Not Found
                    System.out.println("EmployeeController: Employee " + employeeNumber + " not found for deletion.");
                    ctx.json(new MessageResponse("Employee with number " + employeeNumber + " not found.")); // Still send JSON for 404
                }
            } catch (Exception e) {
                ctx.status(500);
                System.err.println("EmployeeController: Error deleting employee and associated user: " + e.getMessage());
                e.printStackTrace();
                ctx.json(new MessageResponse("Internal server error during employee and user deletion."));
            }
        });
    }
}
