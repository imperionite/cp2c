package com.imperionite.cp2c.controller;

import com.imperionite.cp2c.dto.AuthResponse;
import com.imperionite.cp2c.dto.LoginRequest;
import com.imperionite.cp2c.dto.RegisterRequest;
import com.imperionite.cp2c.dto.MessageResponse;
import com.imperionite.cp2c.model.User;
import com.imperionite.cp2c.service.AuthService;
import io.javalin.Javalin;
import io.javalin.http.Context;

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

        // Before filter to check for authentication token on protected routes
        // This filter will execute *before* any handler for paths matching "/api/protected/*"
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
                ctx.status(200); // OK
                System.out.println("AuthController: Login successful for user: " + loginRequest.getUsername());
            } else {
                ctx.status(401); // Unauthorized
                System.out.println("AuthController: Login failed for user: " + loginRequest.getUsername());
            }
            ctx.json(authResponse);
        });


        // POST /api/register endpoint for user registration (PROTECTED)
        // This endpoint *requires* a valid token to register a new user.
        // The authentication logic is handled by the `before` filter registered above.
        app.post("/api/register", ctx -> {
            System.out.println("AuthController: Received registration request to /api/register.");
            // If the request reaches this point, it means the `authenticate` before-filter
            // has successfully validated the token and stored the authenticated user in the context.

            RegisterRequest registerRequest = ctx.bodyAsClass(RegisterRequest.class);

            if (registerRequest == null || registerRequest.getUsername() == null
                    || registerRequest.getPassword() == null) {
                ctx.status(400); // Bad Request
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
                // AuthService will return an error message if username already exists or format is wrong
                ctx.status(409); // Conflict (e.g., username already exists) or 400 Bad Request (e.g., invalid format)
                System.out.println("AuthController: User registration failed for: " + registerRequest.getUsername()
                        + ". Reason: " + authResponse.getMessage());
            }
            ctx.json(authResponse);
        });
    }

    /**
     * Authentication filter logic used by Javalin's `before` handler.
     * Extracts and validates the Authorization Bearer token.
     * If authentication fails, it sets the appropriate status and response,
     * and CRITICALLY, halts the request processing using `return;`.
     * If successful, it stores the authenticated User object as an attribute on the context.
     *
     * @param ctx         The Javalin Context object.
     * @param authService The AuthService instance.
     */
    public static void authenticate(Context ctx, AuthService authService) {
        System.out.println("AuthController: Entering authentication filter for path: " + ctx.path());
        String authorizationHeader = ctx.header("Authorization");

        // 1. Check for Authorization header presence and format
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            System.out.println("AuthController: Authorization header missing or invalid format. Halting with 401.");
            ctx.status(401); // Unauthorized
            ctx.json(new MessageResponse("Authorization token required (Bearer token format)"));
            return; // <<< CRITICAL: Stop the request processing here
        }

        // 2. Extract the token
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        System.out.println(
                "AuthController: Extracted token: " + (token.length() > 10 ? token.substring(0, 10) + "..." : token)); // Log first 10 chars or full if shorter

        // 3. Validate the token using AuthService
        User authenticatedUser = authService.validateToken(token);

        // 4. Check if token validation was successful
        if (authenticatedUser == null) {
            System.out.println("AuthController: Invalid or expired token. Halting with 401 for path: " + ctx.path());
            ctx.status(401); // Unauthorized
            ctx.json(new MessageResponse("Invalid or expired token"));
            return; // <<< CRITICAL: Stop the request processing here
        }

        // 5. If authentication is successful, store the user in the context for downstream handlers
        ctx.attribute("currentUser", authenticatedUser);
        System.out.println("AuthController: Authentication successful for user: " + authenticatedUser.getUsername() + " accessing path: " + ctx.path());
        // If execution reaches here, the request will proceed to the next handler in the chain (e.g., the route handler).
    }
}
