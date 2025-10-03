package org.miniboot.app.controllers;

import org.miniboot.app.auth.AuthService;
import org.miniboot.app.auth.JwtService;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.Json;

import java.util.Map;

/**
 * AuthController: Controller xử lý các endpoint về authentication
 * - POST /auth/login: Đăng nhập và nhận JWT token
 * - GET /auth/profile: Lấy thông tin user (cần token)
 * - GET /doctors: Danh sách bác sĩ (cần token - protected)
 */
public class AuthController {

    private static final AuthService authService = new AuthService(new JwtService());

    /**
     * Mount các route vào Router
     */
    public static void mount(Router router) {
        router.post("/auth/login", AuthController::login);
        router.get("/auth/profile", AuthController::getProfile);
        router.get("/doctors", AuthController::getDoctors);
    }

    /**
     * POST /auth/login
     * Body: {"username": "admin", "password": "123456"}
     * Response: {"access_token": "jwt...", "token_type": "Bearer"}
     */
    private static HttpResponse login(HttpRequest request) {
        try {
            // Parse JSON body
            String body = request.bodyText();

            // Simple JSON parsing (trong thực tế nên dùng thư viện JSON)
            String username = extractJsonField(body, "username");
            String password = extractJsonField(body, "password");

            if (username == null || password == null) {
                return Json.error(400, "Thieu username hoac password");
            }

            // Xác thực và tạo token
            String token = authService.authenticate(username, password);

            return Json.ok(Map.of(
                "access_token", token,
                "token_type", "Bearer",
                "expires_in", "86400"
            ));

        } catch (Exception e) {
            return Json.error(401, e.getMessage());
        }
    }

    /**
     * GET /auth/profile
     * Header: Authorization: Bearer <token>
     * Response: {"userId": "admin", "username": "admin", "message": "Profile info"}
     */
    private static HttpResponse getProfile(HttpRequest request) {
        try {
            String authHeader = request.header("Authorization");
            String userId = authService.validateToken(authHeader);

            return Json.ok(Map.of(
                "userId", userId,
                "username", userId,  // Thêm trường username (giống userId)
                "message", "Thong tin user",
                "role", "admin"
            ));

        } catch (Exception e) {
            return Json.error(401, e.getMessage());
        }
    }

    /**
     * GET /doctors (Protected endpoint)
     * Header: Authorization: Bearer <token>
     * Response: Danh sách bác sĩ
     */
    private static HttpResponse getDoctors(HttpRequest request) {
        try {
            // Kiểm tra token
            String authHeader = request.header("Authorization");
            String userId = authService.validateToken(authHeader);

            // Trả về danh sách bác sĩ demo
            return Json.ok(Map.of(
                "message", "Danh sach bac si",
                "requestedBy", userId,
                "data", java.util.List.of(
                    Map.of("id", 1, "name", "BS. Nguyen Van A", "specialty", "Nhi khoa"),
                    Map.of("id", 2, "name", "BS. Tran Thi B", "specialty", "Noi khoa"),
                    Map.of("id", 3, "name", "BS. Le Van C", "specialty", "Ngoai khoa")
                )
            ));

        } catch (Exception e) {
            return Json.error(403, "Forbidden: " + e.getMessage());
        }
    }

    /**
     * Tiện ích: Extract field từ JSON string đơn giản
     * (Trong thực tế nên dùng thư viện JSON như Gson, Jackson)
     */
    private static String extractJsonField(String json, String field) {
        String pattern = "\"" + field + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}
