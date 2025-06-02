package com.imperionite.cp2c.config;

import com.imperionite.cp2c.controller.AuthController;
import com.imperionite.cp2c.controller.EmployeeController;
import com.imperionite.cp2c.dao.CSVEmployeeDao;
import com.imperionite.cp2c.dao.CSVUserDao;
import com.imperionite.cp2c.model.User;
import com.imperionite.cp2c.security.PasswordUtil;
import com.imperionite.cp2c.service.AuthService;
import com.imperionite.cp2c.service.EmployeeService;
import com.imperionite.cp2c.util.ResourceCsvReader;

import io.javalin.Javalin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Central configuration class for initializing DAOs, Services, and Controllers,
 * and handling user seeding.
 */
public class AppConfigurator {

    // Define constants for resource and data file names/headers
    private static final String EMPLOYEES_STATIC_CSV_RESOURCE_NAME = "employees.csv";
    private static final String EMPLOYEES_DATA_CSV_FILENAME = "employees.csv";
    private static final String EMPLOYEE_CSV_HEADER = "Employee #,Last Name,First Name,Birthday,Address,Phone Number,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate";

    // Define constants for passwords
    private static final String ADMIN_PASSWORD = "adminPassword";
    private static final String USER_PASSWORD = "userPassword";

    private CSVUserDao userDao;
    private CSVEmployeeDao employeeDao;
    private AuthService authService;
    private EmployeeService employeeService;

    // No need for a constructor with `app` if you're passing it to methods
    public AppConfigurator() {
        // Default constructor
    }

    public void configureAndStart(Javalin app) {
        System.out.println("AppConfigurator: Starting application component configuration..."); // Improved log

        // Initialize DAOs first, as they handle file existence
        initializeDaos();

        // After DAOs are initialized and files are ready, proceed with others
        authService = new AuthService(userDao);
        seedUsers(); // This will now correctly check the populated DAOs
        initializeServices();
        registerControllers(app); // Pass app here

        System.out.println("AppConfigurator: Application components configured successfully.");
        System.out.println("AppConfigurator: API base URL: http://localhost:" + app.port() + "/api");
        System.out.println("AppConfigurator: Server fully initialized and ready.");
    }


    private void initializeDaos() {
        System.out.println("AppConfigurator: Initializing DAOs and checking data files...");

        Path dataDir = Paths.get("data");
        Path employeeDataPath = Paths.get(dataDir.toString(), EMPLOYEES_DATA_CSV_FILENAME);

        try {
            // 1. Ensure data directory exists
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
                System.out.println("AppConfigurator: Created data directory: " + dataDir.toAbsolutePath());
            } else {
                System.out.println("AppConfigurator: Data directory already exists: " + dataDir.toAbsolutePath());
            }

            // 2. Handle employees.csv initialization
            boolean employeeDataFileExists = Files.exists(employeeDataPath);
            long employeeDataFileSize = employeeDataFileExists ? Files.size(employeeDataPath) : 0;
            System.out.println("AppConfigurator: Employee data file '" + EMPLOYEES_DATA_CSV_FILENAME + "' exists: " + employeeDataFileExists + ", size: " + employeeDataFileSize + " bytes.");

            if (!employeeDataFileExists || employeeDataFileSize == 0) {
                System.out.println("AppConfigurator: Employee data file not found or is empty. Attempting to copy from resources and add header.");
                try {
                    // Copy from src/main/resources to data/employees.csv
                    ResourceCsvReader.copyResourceToFile(EMPLOYEES_STATIC_CSV_RESOURCE_NAME, employeeDataPath);
                    System.out.println("AppConfigurator: Resource '" + EMPLOYEES_STATIC_CSV_RESOURCE_NAME + "' copied to " + employeeDataPath.toAbsolutePath());

                    // Check if file is still empty after copy (can happen if source resource is empty)
                    if (Files.size(employeeDataPath) == 0) {
                        try (java.io.BufferedWriter writer = Files.newBufferedWriter(employeeDataPath, java.nio.file.StandardOpenOption.APPEND)) {
                            writer.write(EMPLOYEE_CSV_HEADER);
                            writer.newLine();
                            System.out.println("AppConfigurator: Added header to data/employees.csv (file was empty after copy).");
                        }
                    } else {
                        System.out.println("AppConfigurator: data/employees.csv has content after copy. Header not appended automatically.");
                    }
                } catch (IOException e) {
                    System.err.println("AppConfigurator: CRITICAL ERROR: Failed to copy initial employees.csv from resources: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("Failed to initialize employees.csv data file.", e);
                }
            } else {
                System.out.println("AppConfigurator: data/employees.csv already exists and has content. Skipping initial copy.");
            }

            // 3. Initialize DAOs after file handling
            userDao = new CSVUserDao();
            System.out.println("AppConfigurator: CSVUserDao initialized.");
            // Assuming CSVUserDao creates users.csv if it doesn't exist upon initialization
            // You might want to add similar checks for users.csv as you did for employees.csv
            // if it's not being created reliably by CSVUserDao's constructor.

            employeeDao = new CSVEmployeeDao();
            System.out.println("AppConfigurator: CSVEmployeeDao initialized.");
            System.out.println("AppConfigurator: Loaded " + employeeDao.getAllEmployees().size() + " employees from data/employees.csv.");

        } catch (IOException | RuntimeException e) { // Catch both IOException and RuntimeException
            System.err.println("AppConfigurator: Failed during DAO initialization or file handling: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("App initialization failed due to DAO/file error.", e);
        }
    }

    private void seedUsers() {
        System.out.println("AppConfigurator: Seeding users...");
        try {
            List<User> currentUsers = userDao.getAllUsers();
            System.out.println("AppConfigurator: Current users in data/users.csv: " + currentUsers.size());

            boolean adminExists = currentUsers.stream().anyMatch(u -> u.getUsername().equals("admin"));
            long employeeUsersCount = currentUsers.stream().filter(u -> u.getUsername().startsWith("user-")).count();
            long initialEmployeeCount = employeeDao.getAllEmployees().size();

            System.out.println("AppConfigurator: Admin user exists: " + adminExists);
            System.out.println("AppConfigurator: Employee users found: " + employeeUsersCount);
            System.out.println("AppConfigurator: Total employees found: " + initialEmployeeCount);


            // Seed only if admin doesn't exist OR if employee users are fewer than actual employees
            if (!adminExists || employeeUsersCount < initialEmployeeCount) {
                System.out.println("AppConfigurator: Seeding required based on current user and employee counts.");
                seedAdminAndEmployeeUsers();
            } else {
                System.out.println("AppConfigurator: Users already adequately seeded. Skipping seeding process.");
            }
        } catch (Exception e) {
            System.err.println("AppConfigurator: Error during user seeding: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("User seeding failed.", e);
        }
    }

    private void seedAdminAndEmployeeUsers() {
        System.out.println("AppConfigurator: Performing admin and employee user seeding...");

        // Seed admin user if not exists
        if (userDao.findByUsername("admin").isEmpty()) {
            String adminHashedPassword = PasswordUtil.hashPassword(ADMIN_PASSWORD);
            User admin = new User(UUID.randomUUID().toString(), "admin", adminHashedPassword, "");
            userDao.saveUser(admin);
            System.out.println("AppConfigurator: Admin user 'admin' created.");
        } else {
            System.out.println("AppConfigurator: Admin user 'admin' already exists. Skipping creation.");
        }

        // Seed employee users
        List<String> employeeNumbers = employeeDao.getAllEmployees().stream()
                .map(employee -> employee.getEmployeeNumber())
                .collect(Collectors.toList());
        System.out.println("AppConfigurator: Found " + employeeNumbers.size() + " employee numbers for user seeding.");

        String userHashedPassword = PasswordUtil.hashPassword(USER_PASSWORD); // Hash the user password
        int newEmployeeUsersCreated = 0;
        for (String employeeNumber : employeeNumbers) {
            String username = "user-" + employeeNumber;
            if (userDao.findByUsername(username).isEmpty()) { // Only create if user doesn't already exist
                User user = new User(UUID.randomUUID().toString(), username, userHashedPassword, ""); // Use userHashedPassword
                userDao.saveUser(user);
                newEmployeeUsersCreated++;
                System.out.println("AppConfigurator: Created employee user: " + username);
            } else {
                System.out.println("AppConfigurator: Employee user '" + username + "' already exists. Skipping.");
            }
        }
        System.out.println("AppConfigurator: Created " + newEmployeeUsersCreated + " new employee users.");
        System.out.println("AppConfigurator: Total users after seeding: " + userDao.getAllUsers().size());
    }

    private void initializeServices() {
        System.out.println("AppConfigurator: Initializing services...");
        employeeService = new EmployeeService(employeeDao, userDao, authService);
        System.out.println("AppConfigurator: Services initialized.");
    }

    private void registerControllers(Javalin app) {
        System.out.println("AppConfigurator: Registering controllers...");
        AuthController.registerRoutes(app, authService);
        EmployeeController.registerRoutes(app, employeeService);
        // Re-add your protected hello route if you want it
        app.get("/api/protected/hello", ctx -> {
            User currentUser = ctx.attribute("currentUser");
            if (currentUser != null) {
                ctx.result("Hello from a protected route! Authenticated user: " + currentUser.getUsername());
            } else {
                ctx.status(401);
                ctx.result("Unauthorized: No authenticated user found.");
            }
        });
        System.out.println("AppConfigurator: Controllers registered.");
    }
}