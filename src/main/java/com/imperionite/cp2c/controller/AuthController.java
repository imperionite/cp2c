package com.imperionite.cp2c.controller;

import com.imperionite.cp2c.dto.AuthResponse;
import com.imperionite.cp2c.dto.LoginRequest;
import com.imperionite.cp2c.dto.RegisterRequest;
import com.imperionite.cp2c.dto.MessageResponse;
import com.imperionite.cp2c.model.User;
import com.imperionite.cp2c.service.AuthService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.http.HandlerType;

/**
 * Controller for handling authentication-related API endpoints.
 */
public class AuthController {

    /**
     * Registers all authentication routes and before-filters.
     *
     * @param app         The Javalin app instance to register routes with.
     * @param authService The AuthService instance to use for business logic.
     */
    public static void registerRoutes(Javalin app, AuthService authService) {

        // IMPORTANT: Register before-filters *before* their corresponding routes
        // to ensure they are applied correctly.
        // The order of calls to AuthController.registerRoutes and
        // EmployeeController.registerRoutes
        // in Main.java is CRUCIAL. AuthController must be registered first.

        // Before filter to check for authentication token on protected routes.
        // This filter will execute *before* any handler for paths matching
        // "/api/protected/*"
        // or exactly "/api/register".
        app.before("/api/protected/*", ctx -> authenticate(ctx, authService));
        app.before("/api/register", ctx -> authenticate(ctx, authService));

        // POST /api/login endpoint for user authentication (NOT PROTECTED)
        // This is a public endpoint allowing users to obtain a token.
        app.post("/api/login", ctx -> {
            System.out.println("AuthController: Received login request to /api/login.");

            LoginRequest loginRequest = ctx.bodyAsClass(LoginRequest.class);

            if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
                ctx.status(400); // Bad Request
                System.out.println("AuthController: Bad Request - Username and password are required for login.");
                ctx.json(new MessageResponse("Username and password are required"));
                return; // Stop further processing for this request
            }

            AuthResponse authResponse = authService.login(loginRequest.getUsername(), loginRequest.getPassword());

            if (authResponse.getToken() != null) {
                ctx.status(200); 
                System.out.println("AuthController: Login successful for user: " + loginRequest.getUsername());
            } else {
                ctx.status(401); 
                System.out.println("AuthController: Login failed for user: " + loginRequest.getUsername() + ". Reason: "
                        + authResponse.getMessage());
            }
            ctx.json(authResponse);
        });

        // POST /api/register endpoint for user registration (PROTECTED)
        // This endpoint *requires* a valid token to register a new user.
        // The authentication logic is handled by the `before` filter registered above.
        app.post("/api/register", ctx -> {
            System.out.println("AuthController: Processing protected registration request to /api/register.");
            // If the request reaches this point, it means the `authenticate` before-filter
            // has successfully validated the token and stored the authenticated user in the
            // context.

            RegisterRequest registerRequest = ctx.bodyAsClass(RegisterRequest.class);

            if (registerRequest == null || registerRequest.getUsername() == null
                    || registerRequest.getPassword() == null) {
                ctx.status(400);
                System.out
                        .println("AuthController: Bad Request - Username and password are required for registration.");
                ctx.json(new MessageResponse("Username and password are required for registration"));
                return;
            }

            AuthResponse authResponse = authService.registerUser(registerRequest.getUsername(),
                    registerRequest.getPassword());

            if (authResponse.getToken() != null) {
                ctx.status(201); // Created
                System.out.println("AuthController: User registered successfully: " + registerRequest.getUsername());
            } else {
                // AuthService will return an error message if username already exists or format
                // is wrong; Conflict (e.g., username already exists) or 400 Bad Request (e.g., invalid format)
                ctx.status(409); 
                System.out.println("AuthController: User registration failed for: " + registerRequest.getUsername()
                        + ". Reason: " + authResponse.getMessage());
            }
            ctx.json(authResponse);
        });

        // Simple test endpoint to confirm filter functionality
        app.get("/api/protected/test", ctx -> {
            User currentUser = ctx.attribute("currentUser"); // Retrieve authenticated user from context
            ctx.result("This is a PROTECTED endpoint. You are authenticated as: "
                    + (currentUser != null ? currentUser.getUsername() : "UNKNOWN"));
            System.out.println("AuthController: Accessed protected test endpoint by user: "
                    + (currentUser != null ? currentUser.getUsername() : "UNKNOWN"));
        });
    }

    /**
     * Authentication filter logic used by Javalin's `before` handler.
     * Extracts and validates the Authorization Bearer token.
     * If authentication fails, it throws an `UnauthorizedResponse` which
     * immediately halts request processing.
     * If successful, it stores the authenticated User object as an attribute on the
     * context.
     *
     * @param ctx         The Javalin Context object.
     * @param authService The AuthService instance.
     */
    public static void authenticate(Context ctx, AuthService authService) {
        System.out.println("AuthController: Entering authentication filter for path: " + ctx.path());

        // Skip authentication for OPTIONS requests (CORS preflight)
        if (ctx.method() == HandlerType.OPTIONS) {
            return; // skip auth for OPTIONS
        }

        String authorizationHeader = ctx.header("Authorization");

        // 1. Check for Authorization header presence and format
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            System.out.println(
                    "AuthController: Auth header missing or invalid format. Throwing UnauthorizedResponse for path: "
                            + ctx.path());
            throw new UnauthorizedResponse("Authorization token required (Bearer token format)");
        }

        // 2. Extract the token
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        System.out.println(
                "AuthController: Extracted token for validation: "
                        + (token.length() > 10 ? token.substring(0, 10) + "..." : token) + ". Path: " + ctx.path());

        // 3. Validate the token using AuthService
        User authenticatedUser = authService.validateToken(token);

        // 4. Check if token validation was successful
        if (authenticatedUser == null) {
            System.out.println(
                    "AuthController: Invalid or expired token. Throwing UnauthorizedResponse for path: " + ctx.path());
            throw new UnauthorizedResponse("Invalid or expired token");
        }

        // 5. If authentication is successful, store the user in the context for
        // downstream handlers
        ctx.attribute("currentUser", authenticatedUser);
        System.out.println("AuthController: Authentication successful for user: " + authenticatedUser.getUsername()
                + ". Proceeding for path: " + ctx.path());
    }
}
