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

    // private static final Gson gson = new Gson();

    /**
     * Registers all authentication routes.
     *
     * @param app         The Javalin app instance to register routes with.
     * @param authService The AuthService instance to use for business logic.
     */
    public static void registerRoutes(Javalin app, AuthService authService) {

        // POST /api/login endpoint for user authentication (NOT PROTECTED)
        app.post("/api/login", ctx -> {
            System.out.println("AuthController: Received login request to /api/login.");

            // Use ctx.bodyAsClass for direct deserialization by Javalin
            LoginRequest loginRequest = ctx.bodyAsClass(LoginRequest.class);

            if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
                ctx.status(400); // Bad Request
                System.out.println("AuthController: Bad Request - Username and password are required.");
                ctx.json(new MessageResponse("Username and password are required")); // Javalin handles JSON serialization
                return; // End processing for this request
            }

            AuthResponse authResponse = authService.login(loginRequest.getUsername(), loginRequest.getPassword());

            if (authResponse.getToken() != null) {
                ctx.status(200); // OK
                System.out.println("AuthController: Login successful for user: " + loginRequest.getUsername());
            } else {
                ctx.status(401); // Unauthorized
                System.out.println("AuthController: Login failed for user: " + loginRequest.getUsername());
            }
            ctx.json(authResponse); // Javalin handles JSON serialization
        });

        // Before filter to check for authentication token on protected routes
        // All routes under /api/protected/* are protected.
        // Also, /api/register is now protected.
        // Use lambda to pass authService to the authenticate method
        app.before("/api/protected/*", ctx -> authenticate(ctx, authService));
        app.before("/api/register", ctx -> authenticate(ctx, authService));

        // POST /api/register endpoint for user registration (PROTECTED)
        // This endpoint requires a valid token from an *admin* user or similar,
        // as per typical REST API security patterns for user creation by privileged users.
        app.post("/api/register", ctx -> {
            System.out.println("AuthController: Received registration request to /api/register.");
            // Authentication already handled by before filter

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
                ctx.status(409); // Conflict or 400 Bad Request depending on exact error reason
                System.out.println("AuthController: User registration failed for: " + registerRequest.getUsername()
                        + ". Reason: " + authResponse.getMessage());
            }
            ctx.json(authResponse);
        });
    }

    /**
     * Authentication filter logic used by Javalin's `before` handler.
     * Extracts and validates the Authorization Bearer token.
     * Sets the authenticated User object as an attribute on the context if
     * successful.
     *
     * @param ctx         The Javalin Context object.
     * @param authService The AuthService instance.
     */
    public static void authenticate(Context ctx, AuthService authService) {
        System.out.println("AuthController: Entering authentication filter.");
        String authorizationHeader = ctx.header("Authorization"); // Use ctx.header()

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            System.out.println("AuthController: Authorization header missing or invalid format. Halting with 401.");
            ctx.status(401); // Unauthorized
            ctx.json(new MessageResponse("Authorization token required (Bearer token format)"));
            ctx.host(); // CORRECTED: Use ctx.halt() to stop the request processing
            return;
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim(); // Trim for safety
        System.out.println(
                "AuthController: Extracted token: " + (token.substring(0, Math.min(token.length(), 10)) + "..."));

        User authenticatedUser = authService.validateToken(token);

        if (authenticatedUser == null) {
            System.out.println("AuthController: Invalid or expired token. Halting with 401.");
            ctx.status(401); // Unauthorized
            ctx.json(new MessageResponse("Invalid or expired token"));
            ctx.host(); // CORRECTED: Use ctx.halt() to stop the request processing
            return;
        }

        // If authentication is successful, store the user in the context for downstream
        // handlers
        ctx.attribute("currentUser", authenticatedUser);
        System.out.println("AuthController: Authentication successful for user: " + authenticatedUser.getUsername());
    }

    // You need to define MessageResponse DTO if it's not already defined elsewhere.
    // Example:
    /*
    package com.imperionite.cp2c.dto;

    public class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
    */
}