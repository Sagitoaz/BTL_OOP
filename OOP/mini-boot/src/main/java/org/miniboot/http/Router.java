package org.miniboot.http;

import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.controller.AuthController;
import org.miniboot.auth.AuthMiddleware;
import org.miniboot.auth.JwtService;

import java.util.HashMap;
import java.util.Map;

public class Router {
    private final AuthController authController;
    private final AuthMiddleware authMiddleware;
    private final Map<String, String> routes;

    public Router() {
        this.authController = new AuthController();
        this.authMiddleware = new AuthMiddleware(new JwtService());
        this.routes = new HashMap<>();
        setupRoutes();
    }

    private void setupRoutes() {
        routes.put("POST /auth/login", "auth_login");
        routes.put("GET /doctors", "protected_doctors");
        routes.put("GET /health", "public_health");
    }

    public HttpResponse route(HttpRequest request) {
        String routeKey = request.method + " " + request.path;
        String handler = routes.get(routeKey);

        if (handler == null) {
            return HttpResponse.json(404, "{\"error\":\"Not Found\"}");
        }

        try {
            switch (handler) {
                case "auth_login":
                    return handleLogin(request);
                case "protected_doctors":
                    return handleDoctors(request);
                case "public_health":
                    return handleHealth(request);
                default:
                    return HttpResponse.json(404, "{\"error\":\"Handler not found\"}");
            }
        } catch (Exception e) {
            return HttpResponse.json(500, "{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private HttpResponse handleLogin(HttpRequest request) {
        if (!"POST".equals(request.method)) {
            return HttpResponse.json(405, "{\"error\":\"Method not allowed\"}");
        }

        String responseJson = authController.login(new String(request.body));
        return HttpResponse.json(200, responseJson);
    }

    private HttpResponse handleDoctors(HttpRequest request) {
        try {
            // Kiểm tra Authorization header
            String authHeader = request.headers.get("Authorization");
            if (authHeader == null) {
                return HttpResponse.json(401, "{\"error\":\"Missing Authorization header\"}");
            }

            // Validate JWT token
            String userId = authMiddleware.validateRequest(authHeader);

            // Trả về danh sách bác sĩ giả
            String doctorsJson = "{"
                + "\"doctors\": ["
                + "  {\"id\": 1, \"name\": \"Dr. Smith\", \"specialty\": \"Cardiology\"},"
                + "  {\"id\": 2, \"name\": \"Dr. Johnson\", \"specialty\": \"Neurology\"},"
                + "  {\"id\": 3, \"name\": \"Dr. Brown\", \"specialty\": \"Orthopedics\"}"
                + "],"
                + "\"requestedBy\": \"" + userId + "\","
                + "\"message\": \"Doctors list retrieved successfully\""
                + "}";

            return HttpResponse.json(200, doctorsJson);

        } catch (Exception e) {
            return HttpResponse.json(401, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private HttpResponse handleHealth(HttpRequest request) {
        String healthJson = "{"
            + "\"status\": \"OK\","
            + "\"timestamp\": \"" + System.currentTimeMillis() + "\","
            + "\"service\": \"Miniboot Auth Server\""
            + "}";
        return HttpResponse.json(200, healthJson);
    }
}
