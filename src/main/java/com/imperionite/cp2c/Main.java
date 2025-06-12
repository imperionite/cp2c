package com.imperionite.cp2c;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.imperionite.cp2c.config.BigDecimalDeserializer;
import com.imperionite.cp2c.controller.AuthController;
import com.imperionite.cp2c.controller.EmployeeController;
import com.imperionite.cp2c.dao.EmployeeDao;
import com.imperionite.cp2c.dao.UserDao;
import com.imperionite.cp2c.dto.MessageResponse;
import com.imperionite.cp2c.model.Employee;
import com.imperionite.cp2c.service.AuthService;
import com.imperionite.cp2c.service.EmployeeService;
import com.imperionite.cp2c.service.SalaryCalculatorService;

import io.javalin.Javalin;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.json.JavalinJackson;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class Main {

    private static final String USERS_CSV_FILE_PATH = "data/users.csv";
    private static final String EMPLOYEES_CSV_FILE_PATH = "data/employees.csv"; // Path for dynamic employee data

    // This resource is used to *initially* copy employee data to the data/ directory if it doesn't exist
    private static final String INITIAL_EMPLOYEES_CSV_RESOURCE = "/employees.csv";

    private static final String SEED_USER_PASSWORD = "userPassword";

    private static SalaryCalculatorService salaryCalculatorService;
    private static UserDao userDao;
    private static EmployeeDao employeeDao;
    private static AuthService authService;
    private static EmployeeService employeeService;

    public static void main(String[] args) throws IOException {
        Path dataDirPath = Paths.get("data");
        if (!Files.exists(dataDirPath)) {
            try {
                Files.createDirectories(dataDirPath);
                System.out.println("Created data directory: " + dataDirPath.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error creating data directory: " + e.getMessage());
                System.exit(1);
            }
        }

        Path targetEmployeeCsvPath = Paths.get(EMPLOYEES_CSV_FILE_PATH);
        boolean targetEmployeeCsvExistsAndHasContent = Files.exists(targetEmployeeCsvPath)
                && (Files.size(targetEmployeeCsvPath) > 0);

        if (!targetEmployeeCsvExistsAndHasContent) {
            System.out.println(
                    "No existing employees.csv found in data/ or it's empty. Attempting to copy initial data from resources...");
            try (InputStream is = Main.class.getResourceAsStream(INITIAL_EMPLOYEES_CSV_RESOURCE)) {
                if (is != null) {
                    Files.createDirectories(targetEmployeeCsvPath.getParent());
                    Files.copy(is, targetEmployeeCsvPath, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Successfully copied initial employees.csv from resources to: "
                            + targetEmployeeCsvPath.toAbsolutePath());
                } else {
                    System.err.println(
                            "Warning: Initial employees.csv resource not found at " + INITIAL_EMPLOYEES_CSV_RESOURCE);
                }
            } catch (IOException e) {
                System.err.println("Error copying initial employees.csv from resources: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Existing employees.csv found in data/. Skipping initial data copy from resources.");
        }

        userDao = new UserDao(USERS_CSV_FILE_PATH);
        employeeDao = new EmployeeDao(EMPLOYEES_CSV_FILE_PATH);

        authService = new AuthService(userDao);
        employeeService = new EmployeeService(employeeDao, userDao, authService);
        // FIX: Pass the dynamic employee CSV file path to SalaryCalculatorService
        salaryCalculatorService = new SalaryCalculatorService(EMPLOYEES_CSV_FILE_PATH);

        System.out.println("\n--- Starting User Data Seeding ---");
        List<Employee> allEmployees = employeeService.getAllEmployees();
        if (allEmployees.isEmpty()) {
            System.out.println("No employees found in " + EMPLOYEES_CSV_FILE_PATH + ". Skipping user seeding.");
            System.out.println("To seed users, ensure " + EMPLOYEES_CSV_FILE_PATH + " contains employee data.");
        } else {
            System.out.println("Found " + allEmployees.size() + " employees. Attempting to seed users...");
            for (Employee employee : allEmployees) {
                String employeeNumber = employee.getEmployeeNumber();
                String username = "user-" + employeeNumber;
                if (userDao.findByUsername(username).isEmpty()) {
                    System.out.println("Seeding user: " + username);
                    authService.registerUser(username, SEED_USER_PASSWORD);
                } else {
                    System.out.println("User '" + username + "' already exists. Skipping seeding for this user.");
                }
            }
            System.out.println("--- User Data Seeding Complete ---\n");
        }

        Javalin app = Javalin.create(config -> {
            // Create a new ObjectMapper instance and configure it for JSON mapping
            ObjectMapper customObjectMapper = new ObjectMapper();
            customObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            customObjectMapper.registerModule(new JavaTimeModule());
            customObjectMapper.registerModule(new SimpleModule().addDeserializer(BigDecimal.class, new BigDecimalDeserializer()));
            // Pass the custom ObjectMapper to JavalinJackson and set it as the JSON mapper
            config.jsonMapper(new JavalinJackson(customObjectMapper));

            config.requestLogger.http((ctx, time) -> {
                System.out.println(String.format("Request: %s %s - Status: %d - Time: %.2f ms",
                        ctx.req().getMethod(), ctx.req().getRequestURI(), ctx.res().getStatus(), time));
            });
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    it.allowHost("http://localhost:5173", "http://127.0.0.1:5173");
                    it.allowCredentials = true;
                    it.exposeHeader("Authorization");
                });
            });

        }).start(4567);

        app.exception(UnauthorizedResponse.class, (e, ctx) -> {
            ctx.status(e.getStatus()).json(new MessageResponse(e.getMessage()));
            System.err.println("Authentication Error (401): " + e.getMessage() + " for path: " + ctx.path());
        });

        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(400).json(new MessageResponse(e.getMessage()));
            System.err.println("Bad Request (400): " + e.getMessage());
        });

        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500).json(new MessageResponse("Internal Server Error: " + e.getMessage()));
            System.err.println("Internal Server Error (500): " + e.getMessage());
            e.printStackTrace();
        });

        AuthController.registerRoutes(app, authService);
        EmployeeController.registerRoutes(app, employeeService, salaryCalculatorService);

        System.out.println("Javalin server started on port 4567.");
        System.out.println("API Endpoints:");
        System.out.println("  POST /api/login (Public) - Login a user");
        System.out.println("  POST /api/register (Protected) - Register a new user");
        System.out.println("  GET /api/protected/employees (Protected) - Get all employee summaries");
        System.out.println("  GET /api/protected/employees/{employeeNumber} (Protected) - Get employee details");
        System.out.println("  POST /api/protected/employees (Protected) - Create new employee");
        System.out.println("  PATCH /api/protected/employees/{employeeNumber} (Protected) - Update employee");
        System.out.println("  DELETE /api/protected/employees/{employeeNumber} (Protected) - Delete employee");
        System.out.println("  GET /api/protected/monthly-cutoffs (Protected) - Get available monthly cutoffs (NEW)");
        System.out.println("  GET /api/protected/employees/{employeeNumber}/salary (Protected) - Calculate monthly salary (NEW)");
        System.out.println("  GET /api/protected/test (Protected) - Test authentication");

        app.get("/", ctx -> ctx.result("Employee Management System API is running!"));
    }
}
