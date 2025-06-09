package com.imperionite.cp2c;

import com.imperionite.cp2c.controller.AuthController;
import com.imperionite.cp2c.controller.EmployeeController;
import com.imperionite.cp2c.dao.EmployeeDao;
import com.imperionite.cp2c.dao.UserDao;
import com.imperionite.cp2c.dto.MessageResponse;
import com.imperionite.cp2c.model.Employee; // Import Employee model for seeding
import com.imperionite.cp2c.service.AuthService;
import com.imperionite.cp2c.service.EmployeeService;
import io.javalin.Javalin;
import io.javalin.http.UnauthorizedResponse; // Import specifically for 401
import io.javalin.json.JavalinJackson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List; // Import List

public class Main {

    // Define paths for writable CSV files in the 'data' folder
    private static final String USERS_CSV_FILE_PATH = "data/users.csv";
    private static final String EMPLOYEES_CSV_FILE_PATH = "data/employees.csv";

    // Define the path for the initial employees.csv file located in resources
    private static final String INITIAL_EMPLOYEES_CSV_RESOURCE = "/employees.csv"; // Note the leading '/' for classpath
                                                                                   // resource

    // Define the common password for seeded users
    private static final String SEED_USER_PASSWORD = "userPassword";

    public static void main(String[] args) throws IOException {
        // Ensure the 'data' directory exists
        Path dataDirPath = Paths.get("data");
        if (!Files.exists(dataDirPath)) {
            try {
                Files.createDirectories(dataDirPath);
                System.out.println("Created data directory: " + dataDirPath.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error creating data directory: " + e.getMessage());
                // Exit if data directory cannot be created as DAOs will fail
                System.exit(1);
            }
        }

        // --- Initial Data Copy Logic ---
        // This section copies the initial employees.csv from resources to the data
        // folder
        // if data/employees.csv does not exist or is empty.
        Path targetEmployeeCsvPath = Paths.get(EMPLOYEES_CSV_FILE_PATH);
        boolean targetEmployeeCsvExistsAndHasContent = Files.exists(targetEmployeeCsvPath)
                && (Files.size(targetEmployeeCsvPath) > 0);

        if (!targetEmployeeCsvExistsAndHasContent) {
            System.out.println(
                    "No existing employees.csv found in data/ or it's empty. Attempting to copy initial data from resources...");
            try (InputStream is = Main.class.getResourceAsStream(INITIAL_EMPLOYEES_CSV_RESOURCE)) {
                if (is != null) {
                    // Ensure the data directory exists before copying
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
        // --- End Initial Data Copy Logic ---

        // Initialize DAOs with their respective CSV file paths.
        // The DAOs' constructors will ensure the CSV files exist with headers.
        // Note: UserDao's constructor will still create users.csv if it doesn't exist.
        UserDao userDao = new UserDao(USERS_CSV_FILE_PATH);
        EmployeeDao employeeDao = new EmployeeDao(EMPLOYEES_CSV_FILE_PATH); // Now points to the potentially copied file

        // Initialize Services, injecting DAOs and other services as dependencies
        AuthService authService = new AuthService(userDao);
        EmployeeService employeeService = new EmployeeService(employeeDao, userDao, authService);

        // --- Data Seeding Logic (for Users based on Employees) ---
        // This section will run once on application startup.
        // It ensures that for every employee, there's a corresponding user account.

        System.out.println("\n--- Starting User Data Seeding ---");
        List<Employee> allEmployees = employeeService.getAllEmployees(); // Get all existing employees (now potentially
                                                                         // from the copied file)
        if (allEmployees.isEmpty()) {
            System.out.println("No employees found in " + EMPLOYEES_CSV_FILE_PATH + ". Skipping user seeding.");
            System.out.println("To seed users, ensure " + EMPLOYEES_CSV_FILE_PATH + " contains employee data.");
        } else {
            System.out.println("Found " + allEmployees.size() + " employees. Attempting to seed users...");
            for (Employee employee : allEmployees) {
                String employeeNumber = employee.getEmployeeNumber();
                String username = "user-" + employeeNumber; // Construct username based on employee number

                // Check if user already exists to avoid duplicates
                if (userDao.findByUsername(username).isEmpty()) {
                    System.out.println("Seeding user: " + username);
                    // Use AuthService to register the user. This will hash the password, generate
                    // an ID and token, and save to CSV.
                    // This call will also handle username format/uniqueness internally.
                    authService.registerUser(username, SEED_USER_PASSWORD);
                } else {
                    System.out.println("User '" + username + "' already exists. Skipping seeding for this user.");
                }
            }
            System.out.println("--- User Data Seeding Complete ---\n");
        }
        // --- End User Data Seeding Logic ---

        // Initialize Javalin app
        Javalin app = Javalin.create(config -> {
            // Configure Javalin's JSON mapper to use Jackson (default, but explicit for
            // clarity)
            config.jsonMapper(new JavalinJackson());
            // Add a request logger for debugging purposes
            config.requestLogger.http((ctx, time) -> {
                System.out.println(String.format("Request: %s %s - Status: %d - Time: %.2f ms",
                        ctx.req().getMethod(), ctx.req().getRequestURI(), ctx.res().getStatus(), time));
            });
            // Enable CORS for development (adjust for production)
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    it.allowHost("http://localhost:5173", "http://127.0.0.1:5173"); // Allow specific origin
                    it.allowCredentials = true; // Allow credentials (cookies, HTTP authentication, etc.)

                });
            });

        }).start(4567); // Start Javalin on port 4567

        // Register exception handlers here, after app creation using app.exception()
        // Handle UnauthorizedResponse specifically to ensure correct status and message
        app.exception(UnauthorizedResponse.class, (e, ctx) -> {
            // UnauthorizedResponse already has the correct status (401) and message.
            // Javalin handles this automatically, but we include it for explicit logging.
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
            e.printStackTrace(); // Print full stack trace for debugging
        });

        // Register all routes from controllers
        // IMPORTANT: AuthController routes (especially before-filters) should be
        // registered BEFORE other controllers to ensure authentication is applied.
        AuthController.registerRoutes(app, authService);
        EmployeeController.registerRoutes(app, employeeService);

        System.out.println("Javalin server started on port 4567.");
        System.out.println("API Endpoints:");
        System.out.println("  POST /api/login (Public) - Login a user");
        System.out.println("  POST /api/register (Protected) - Register a new user");
        System.out.println("  GET /api/protected/employees (Protected) - Get all employee summaries");
        System.out.println("  GET /api/protected/employees/{employeeNumber} (Protected) - Get employee details");
        System.out.println("  POST /api/protected/employees (Protected) - Create new employee");
        System.out.println("  PATCH /api/protected/employees/{employeeNumber} (Protected) - Update employee");
        System.out.println("  DELETE /api/protected/employees/{employeeNumber} (Protected) - Delete employee");
        System.out.println("  GET /api/protected/test (Protected) - Test authentication");

        // Optional: Add a simple root endpoint for health check or info
        app.get("/", ctx -> ctx.result("Employee Management System API is running!"));
    }
}
