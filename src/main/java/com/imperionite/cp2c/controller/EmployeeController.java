package com.imperionite.cp2c.controller; 

import com.imperionite.cp2c.dto.MessageResponse; 
import com.imperionite.cp2c.model.Employee;     
import com.imperionite.cp2c.service.EmployeeService; 
import io.javalin.Javalin; 

/**
 * Controller for handling employee-related API endpoints.
 * All routes are protected and require authentication (handled by AuthController's before filter).
 */
public class EmployeeController {

    // private static final Gson gson = new Gson();

    /**
     * Registers all employee routes.
     * @param app The Javalin app instance to register routes with.
     * @param employeeService The EmployeeService instance to use for business logic.
     */
    public static void registerRoutes(Javalin app, EmployeeService employeeService) {

        // All routes for EmployeeController are typically under /api/protected/
        // The authentication filter for /api/protected/* is set up in AuthController.

        // GET /api/protected/employees - List all employees with summary attributes
        app.get("/api/protected/employees", ctx -> {
            // ctx.type("application/json") is handled by ctx.json()
            System.out.println("EmployeeController: Fetching all employee summaries.");
            ctx.json(employeeService.getAllEmployeeSummaries());
        });

        // GET /api/protected/employees/:employeeNumber - Get full details of a single employee
        app.get("/api/protected/employees/{employeeNumber}", ctx -> { // Changed path parameter syntax
            String employeeNumber = ctx.pathParam("employeeNumber"); // Use ctx.pathParam()
            System.out.println("EmployeeController: Fetching details for employee number: " + employeeNumber);

            Employee employee = employeeService.getEmployeeDetails(employeeNumber);

            if (employee != null) {
                ctx.status(200); // OK
                ctx.json(employee);
            } else {
                ctx.status(404); // Not Found
                ctx.json(new MessageResponse("Employee with number " + employeeNumber + " not found."));
            }
        });

        // POST /api/protected/employees - Create a new employee
        app.post("/api/protected/employees", ctx -> {
            System.out.println("EmployeeController: Received create employee request.");

            Employee newEmployee = ctx.bodyAsClass(Employee.class); // Use ctx.bodyAsClass()

            if (newEmployee == null || newEmployee.getEmployeeNumber() == null || newEmployee.getEmployeeNumber().isEmpty()) {
                ctx.status(400); // Bad Request
                ctx.json(new MessageResponse("Employee number is required to create a new employee."));
                return; // Stop further processing
            }

            try {
                Employee createdEmployee = employeeService.createEmployee(newEmployee);
                ctx.status(201); // Created
                System.out.println("EmployeeController: Employee " + createdEmployee.getEmployeeNumber() + " created successfully.");
                ctx.json(createdEmployee);
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
        app.patch("/api/protected/employees/{employeeNumber}", ctx -> { // Changed path parameter syntax
            String employeeNumber = ctx.pathParam("employeeNumber"); // Use ctx.pathParam()
            System.out.println("EmployeeController: Received PATCH employee request for: " + employeeNumber);

            Employee partialEmployee = ctx.bodyAsClass(Employee.class); // Use ctx.bodyAsClass()

            if (partialEmployee == null) {
                ctx.status(400); // Bad Request
                ctx.json(new MessageResponse("Request body cannot be empty for employee update."));
                return;
            }
            // The employeeNumber in the path is the primary identifier.
            // We don't strictly enforce a match with partialEmployee.getEmployeeNumber() here,
            // as the service layer will handle fetching the correct employee by the path parameter.

            try {
                Employee resultEmployee = employeeService.updateEmployee(employeeNumber, partialEmployee);
                if (resultEmployee != null) {
                    ctx.status(200); // OK
                    System.out.println("EmployeeController: Employee " + employeeNumber + " updated successfully.");
                    ctx.json(resultEmployee);
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
        app.delete("/api/protected/employees/{employeeNumber}", ctx -> { // Changed path parameter syntax
            String employeeNumber = ctx.pathParam("employeeNumber"); // Use ctx.pathParam()
            System.out.println("EmployeeController: Received delete employee request for: " + employeeNumber);

            try {
                boolean deleted = employeeService.deleteEmployee(employeeNumber);
                if (deleted) {
                    ctx.status(204); // No Content - Javalin handles empty body for 204
                    System.out.println("EmployeeController: Employee " + employeeNumber + " and associated user deleted successfully.");
                } else {
                    ctx.status(404); // Not Found
                    System.out.println("EmployeeController: Employee " + employeeNumber + " not found for deletion.");
                    ctx.json(new MessageResponse("Employee with number " + employeeNumber + " not found."));
                }
            } catch (Exception e) {
                ctx.status(500); // Internal Server Error
                System.err.println("EmployeeController: Error deleting employee and associated user: " + e.getMessage());
                e.printStackTrace();
                ctx.json(new MessageResponse("Internal server error during employee and user deletion."));
            }
        });
    }

    // Keep MessageResponse here if it's only used internally by this controller
    // or move it to com.imperionite.cp2c.dto package for broader use.
    // Assuming it's in its own file now based on AuthController fix.
    // private static class MessageResponse {
    //     private String message;
    //     public MessageResponse(String message) {
    //         this.message = message;
    //     }
    //     public String getMessage() { return message; }
    //     public void setMessage(String message) { this.message = message; }
    // }
}