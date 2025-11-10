package org.miniboot.app.controllers;

import org.miniboot.app.auth.AuthService;
import org.miniboot.app.config.ApiEndpoints;
import org.miniboot.app.config.AuthConstants;
import org.miniboot.app.config.ErrorMessages;
import org.miniboot.app.config.HttpConstants;
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

    /**
     * Mount các route vào Router
     */
    public static void mount(Router router) {
        router.post(ApiEndpoints.AUTH_LOGIN, AuthController::login, false);
        router.get(ApiEndpoints.AUTH_PROFILE, AuthController::getProfile, true);
        router.get(ApiEndpoints.BASE_DOCTORS, AuthController::getDoctors, true);
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
            String username = extractJsonField(body, AuthConstants.FIELD_USERNAME);
            String password = extractJsonField(body, AuthConstants.FIELD_PASSWORD);

            if (username == null || password == null) {
                return Json.error(HttpConstants.STATUS_BAD_REQUEST,
                    username == null ? ErrorMessages.ERROR_MISSING_USERNAME : ErrorMessages.ERROR_MISSING_PASSWORD);
            }

            // Xác thực và tạo token
            String token = AuthService.authenticate(username, password);

            return Json.ok(Map.of(
                    AuthConstants.FIELD_ACCESS_TOKEN, token,
                    AuthConstants.FIELD_TOKEN_TYPE, AuthConstants.TOKEN_TYPE,
                    AuthConstants.FIELD_EXPIRES_IN, String.valueOf(AuthConstants.TOKEN_EXPIRATION_SECONDS)));

        } catch (Exception e) {
            return Json.error(HttpConstants.STATUS_UNAUTHORIZED, e.getMessage());
        }
    }

    /**
     * GET /auth/profile
     * Header: Authorization: Bearer <token>
     * Response: {"userId": "admin", "username": "admin", "message": "Profile info"}
     */
    private static HttpResponse getProfile(HttpRequest request) {
        try {
            String authHeader = request.header(HttpConstants.HEADER_AUTHORIZATION);
            String userId = AuthService.validateToken(authHeader);

            return Json.ok(Map.of(
                    AuthConstants.FIELD_USER_ID, userId,
                    AuthConstants.FIELD_USERNAME, userId,
                    AuthConstants.FIELD_MESSAGE, "Thong tin user",
                    AuthConstants.FIELD_ROLE, AuthConstants.ROLE_ADMIN));

        } catch (Exception e) {
            return Json.error(HttpConstants.STATUS_UNAUTHORIZED, e.getMessage());
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
            String authHeader = request.header(HttpConstants.HEADER_AUTHORIZATION);
            String userId = AuthService.validateToken(authHeader);

            // Trả về danh sách bác sĩ demo
            return Json.ok(Map.of(
                    AuthConstants.FIELD_MESSAGE, "Danh sach bac si",
                    "requestedBy", userId,
                    "data", java.util.List.of(
                            Map.of("id", 1, "name", "BS. Nguyen Van A", "specialty", "Nhi khoa"),
                            Map.of("id", 2, "name", "BS. Tran Thi B", "specialty", "Noi khoa"),
                            Map.of("id", 3, "name", "BS. Le Van C", "specialty", "Ngoai khoa"))));

        } catch (Exception e) {
            return Json.error(HttpConstants.STATUS_FORBIDDEN, ErrorMessages.ERROR_FORBIDDEN + ": " + e.getMessage());
        }
    }

    /**
     * Helper method: Trích xuất giá trị field từ JSON string đơn giản
     */
    private static String extractJsonField(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }
}
