package com.imperionite.cp2c.controller;

import com.imperionite.cp2c.dto.EmployeeDto;
import com.imperionite.cp2c.dto.MessageResponse;
import com.imperionite.cp2c.model.Employee;
import com.imperionite.cp2c.model.MonthlyCutoff;
import com.imperionite.cp2c.model.MonthlySalaryCalculationResult;
import com.imperionite.cp2c.service.EmployeeService;
import com.imperionite.cp2c.service.SalaryCalculatorService;
import io.javalin.Javalin;
import java.util.List;

/**
 * Controller for handling employee-related API endpoints.
 * All routes under `/api/protected/*` are protected and require authentication
 * (handled by AuthController's before filter).
 */
public class EmployeeController {

    /**
     * Registers all employee routes.
     *
     * @param app                     The Javalin app instance to register routes
     *                                with.
     * @param employeeService         The EmployeeService instance to use for
     *                                business logic.
     * @param salaryCalculatorService The SalaryCalculatorService instance for
     *                                salary computations.
     */
    public static void registerRoutes(Javalin app, EmployeeService employeeService,
            SalaryCalculatorService salaryCalculatorService) {

        // All routes for EmployeeController are typically under /api/protected/
        // The authentication filter for /api/protected/* is set up in AuthController.
        // It's crucial that AuthController.registerRoutes(app, authService) is called
        // *before* EmployeeController.registerRoutes(app, employeeService) in your
        // Main/App class setup.

        // GET /api/protected/employees - List all employees with summary attributes
        app.get("/api/protected/employees", ctx -> {
            System.out.println("EmployeeController: Fetching all employee summaries.");
            ctx.json(employeeService.getAllEmployeeSummaries());
        });

        // GET /api/protected/employees/:employeeNumber - Get full details of a single
        // employee
        app.get("/api/protected/employees/{employeeNumber}", ctx -> {
            String employeeNumber = ctx.pathParam("employeeNumber");
            System.out.println("EmployeeController: Fetching details for employee number: " + employeeNumber);

            Employee employee = employeeService.getEmployeeDetails(employeeNumber);

            if (employee != null) {
                ctx.status(200);
                ctx.json(new EmployeeDto(employee)); // Convert Employee model to DTO for response
            } else {
                ctx.status(404);
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
                ctx.status(201); 
                System.out.println("EmployeeController: Employee " + createdEmployee.getEmployeeNumber()
                        + " created successfully.");
                ctx.json(new EmployeeDto(createdEmployee)); 
            } catch (IllegalArgumentException e) {
                ctx.status(400); 
                System.err.println("EmployeeController: Failed to create employee: " + e.getMessage());
                ctx.json(new MessageResponse(e.getMessage()));
            } catch (Exception e) {
                ctx.status(500);
                System.err.println("EmployeeController: Error creating employee: " + e.getMessage());
                e.printStackTrace();
                ctx.json(new MessageResponse("Internal server error during employee creation."));
            }
        });

        // PATCH /api/protected/employees/:employeeNumber - Partially update an existing
        // employee (COMPLETED)
        app.patch("/api/protected/employees/{employeeNumber}", ctx -> {
            String employeeNumber = ctx.pathParam("employeeNumber");
            System.out.println("EmployeeController: Received PATCH employee request for: " + employeeNumber);

            // Convert request body DTO to Employee model (only for fields to update)
            // The DTO must have setters for all fields it might receive.
            EmployeeDto partialEmployeeDto = ctx.bodyAsClass(EmployeeDto.class);
            Employee partialEmployee = partialEmployeeDto.toEmployee(); // This creates a model with only set fields

            try {
                Employee updatedEmployee = employeeService.updateEmployee(employeeNumber, partialEmployee);
                if (updatedEmployee != null) {
                    ctx.status(200); // OK
                    System.out.println("EmployeeController: Employee " + updatedEmployee.getEmployeeNumber()
                            + " updated successfully.");
                    ctx.json(new EmployeeDto(updatedEmployee)); // Convert back to DTO for response
                } else {
                    ctx.status(404); // Not Found
                    ctx.json(new MessageResponse("Employee with number " + employeeNumber + " not found for update."));
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

        // DELETE /api/protected/employees/:employeeNumber - Delete an employee
        // (COMPLETED)
        app.delete("/api/protected/employees/{employeeNumber}", ctx -> {
            String employeeNumber = ctx.pathParam("employeeNumber");
            System.out.println("EmployeeController: Received DELETE employee request for: " + employeeNumber);

            try {
                boolean deleted = employeeService.deleteEmployee(employeeNumber);
                if (deleted) {
                    ctx.status(204); // No Content
                    System.out.println("EmployeeController: Employee " + employeeNumber + " deleted successfully.");
                } else {
                    ctx.status(404); // Not Found
                    ctx.json(
                            new MessageResponse("Employee with number " + employeeNumber + " not found for deletion."));
                }
            } catch (Exception e) {
                ctx.status(500); // Internal Server Error
                System.err.println("EmployeeController: Error deleting employee: " + e.getMessage());
                e.printStackTrace();
                ctx.json(new MessageResponse("Internal server error during employee deletion."));
            }
        });

        // NEW: GET /api/protected/monthly-cutoffs - Get list of available monthly
        // cutoffs
        app.get("/api/protected/monthly-cutoffs", ctx -> {
            System.out.println("EmployeeController: Fetching monthly cutoffs for salary calculation.");
            List<MonthlyCutoff> cutoffs = salaryCalculatorService.getMonthlyCutoffs();
            ctx.json(cutoffs);
            ctx.status(200);
        });

        // NEW: GET /api/protected/employees/:employeeNumber/salary - Calculate monthly
        // salary
        app.get("/api/protected/employees/{employeeNumber}/salary", ctx -> {
            String employeeNumber = ctx.pathParam("employeeNumber");
            String yearMonth = ctx.queryParam("yearMonth");

            System.out.println(
                    "EmployeeController: Calculating salary for employee " + employeeNumber + " for " + yearMonth);

            if (yearMonth == null || yearMonth.isEmpty()) {
                ctx.status(400);
                ctx.json(new MessageResponse("Missing 'yearMonth' query parameter."));
                return;
            }

            try {
                MonthlySalaryCalculationResult result = salaryCalculatorService.calculateMonthlySalary(employeeNumber,
                        yearMonth);
                ctx.json(result);
                ctx.status(200);
            } catch (IllegalArgumentException e) {
                ctx.status(404);
                ctx.json(new MessageResponse(e.getMessage()));
            } catch (Exception e) {
                System.err.println("EmployeeController: Error calculating salary for employee " + employeeNumber + ": "
                        + e.getMessage());
                e.printStackTrace();
                ctx.status(500);
                ctx.json(new MessageResponse("Error calculating salary: " + e.getMessage()));
            }
        });
    }
}