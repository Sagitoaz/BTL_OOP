package org.miniboot.app.controllers;

import org.miniboot.app.auth.AuthService;
import org.miniboot.app.auth.PasswordService;
import org.miniboot.app.config.ApiEndpoints;
import org.miniboot.app.config.AuthConstants;
import org.miniboot.app.config.ErrorMessages;
import org.miniboot.app.config.HttpConstants;
import org.miniboot.app.dao.UserDAO;
import org.miniboot.app.dao.UserDAO.UserRecord;
import org.miniboot.app.http.HttpRequest;
import org.miniboot.app.http.HttpResponse;
import org.miniboot.app.router.Router;
import org.miniboot.app.util.Json;
import org.miniboot.app.util.errorvalidation.*;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AuthController: Controller xử lý các endpoint về authentication
 * - POST /auth/login: Đăng nhập và nhận JWT token
 * - GET /auth/profile: Lấy thông tin user (cần token)
 * - GET /doctors: Danh sách bác sĩ (cần token - protected)
 * - POST /auth/change-password: Đổi mật khẩu (cần token)
 * - POST /auth/forgot-password: Quên mật khẩu (không cần token)
 * - POST /auth/register: Đăng ký tài khoản mới
 */
public class AuthController {

    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

    // Get AuthService instance
    private static final AuthService authService = AuthService.getInstance();
    private static final UserDAO userDAO = new UserDAO();

    /**
     * Mount các route vào Router
     */
    public static void mount(Router router) {
        router.post(ApiEndpoints.AUTH_LOGIN, AuthController::login, false);
        router.get(ApiEndpoints.AUTH_PROFILE, AuthController::getProfile, true);
        router.get(ApiEndpoints.BASE_DOCTORS, AuthController::getDoctors, true);

        // 3 endpoint mới
        router.post(ApiEndpoints.AUTH_CHANGE_PASSWORD, AuthController::changePassword, true);
        router.post(ApiEndpoints.AUTH_RESET_PASSWORD, AuthController::forgotPassword, false);
        router.post(ApiEndpoints.AUTH_REGISTER, AuthController::signUp, false);
    }

    /**
     * POST /auth/login
     * Body: {"username": "admin", "password": "123456"}
     * Response: {"access_token": "jwt...", "token_type": "Bearer"}
     */
    private static HttpResponse login(HttpRequest request) {
        try {

            HttpResponse rateLimitError = RateLimiter.checkRateLimit(request);
            if (rateLimitError != null) {
                return rateLimitError;
            }

            // Parse JSON body
            String body = request.bodyText();

            // Simple JSON parsing (trong thực tế nên dùng thư viện JSON)
            String username = extractJsonField(body, AuthConstants.FIELD_USERNAME);
            String password = extractJsonField(body, AuthConstants.FIELD_PASSWORD);

            HttpResponse loginValidationError = LoginValidator.validateLoginCredentials(username, password);
            if (loginValidationError != null) {
                return loginValidationError;
            }

            if (username == null || password == null) {
                return Json.error(HttpConstants.STATUS_BAD_REQUEST,
                    username == null ? ErrorMessages.ERROR_MISSING_USERNAME : ErrorMessages.ERROR_MISSING_PASSWORD);
            }

            // Xác thực và tạo token - gọi instance method
            String token = authService.authenticate(username, password);

            return Json.ok(Map.of(
                    AuthConstants.FIELD_ACCESS_TOKEN, token,
                    AuthConstants.FIELD_TOKEN_TYPE, AuthConstants.TOKEN_TYPE,
                    AuthConstants.FIELD_EXPIRES_IN, String.valueOf(AuthConstants.TOKEN_EXPIRATION_SECONDS)));

        } catch (Exception e) {
            if(e.getMessage().equals("User Not Found")){
                return LoginValidator.userNotFound();
            }
            else if(e.getMessage().equals("Account is not active")){
                return LoginValidator.accountInactive();
            }
            else if(e.getMessage().equals("incorectPassword")){
                return LoginValidator.incorectPassword();
            }
            else{
                return DatabaseErrorHandler.handleDatabaseException(e);
            }

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
     * POST /auth/change-password
     * Header: Authorization: Bearer <token>
     * Body: {"oldPassword": "old123", "newPassword": "new123", "confirmPassword": "new123"}
     * Response: {"message": "Password changed successfully"}
     */
    private static HttpResponse changePassword(HttpRequest request) {
        try {
            // 1. Kiểm tra rate limit
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(request);
            if (rateLimitError != null) {
                return rateLimitError;
            }

            // 2. Validate JWT token
            String authHeader = request.header(HttpConstants.HEADER_AUTHORIZATION);
            String username;
            try {
                username = AuthService.validateToken(authHeader);
            } catch (Exception e) {
                return ChangePasswordValidator.invalidToken();
            }

            // 3. Parse request body
            String body = request.bodyText();
            String oldPassword = extractJsonField(body, "oldPassword");
            String newPassword = extractJsonField(body, "newPassword");
            String confirmPassword = extractJsonField(body, "confirmPassword");

            // 4. Validate input fields - oldPassword not empty
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return Json.error(HttpConstants.STATUS_BAD_REQUEST, "Mật khẩu cũ không được để trống.");
            }

            // 5. Validate new password format
            HttpResponse newPasswordError = ChangePasswordValidator.validateNewPasswordFormat(newPassword);
            if (newPasswordError != null) {
                return newPasswordError;
            }

            // 6. Validate confirm password match
            HttpResponse confirmPasswordError = ChangePasswordValidator.validateConfirmPasswordMatch(newPassword, confirmPassword);
            if (confirmPasswordError != null) {
                return confirmPasswordError;
            }


            // 7. Tìm user từ database
            Optional<UserRecord> userOpt;
            try{
                userOpt = userDAO.findByUsername(username);
            } catch (SQLException e){
                return DatabaseErrorHandler.handleDatabaseException(e);
            }
            if (userOpt.isEmpty()) {
                return Json.error(HttpConstants.STATUS_NOT_FOUND, ErrorMessages.ERROR_USER_NOT_FOUND);
            }

            UserRecord user = userOpt.get();

            // 8. Verify old password
            if (!PasswordService.verifyPassword(oldPassword, user.password)) {
                LOGGER.warning("Change password failed: Incorrect old password for user " + username);
                return ChangePasswordValidator.incorrectOldPassword();
            }

            // 9. Hash new password
            String hashedNewPassword = PasswordService.hashPasswordWithSalt(newPassword);

            // 10. Update password in database
            try{
                String result = authService.updatePassword(user.id, user.role, hashedNewPassword);

                if ("success".equals(result)) {
                    LOGGER.info("✓ Password changed successfully for user: " + username);
                    return Json.ok(Map.of(
                            "message", "Đổi mật khẩu thành công.",
                            "username", username
                    ));
                } else if ("failed".equals(result)) {
                    return ChangePasswordValidator.updateFailed();
                } else {
                    return ChangePasswordValidator.unexpectedError();
                }
            }
           catch (SQLException e){
                return DatabaseErrorHandler.handleDatabaseException(e);
           }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error changing password", e);
            return ChangePasswordValidator.unexpectedError();
        }
    }

    /**
     * POST /auth/forgot-password
     * Body: {"email": "user@example.com"}
     * Response: {"message": "Reset token sent", "token": "ABC123"} (trong thực tế gửi qua email)
     */
    private static HttpResponse forgotPassword(HttpRequest request) {
        try {
            // 1. Kiểm tra rate limit
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(request);
            if (rateLimitError != null) {
                return rateLimitError;
            }

            // 2. Parse request body
            String body = request.bodyText();
            String email = extractJsonField(body, "email");

            // 3. Validate email not empty first
            if (email == null || email.trim().isEmpty()) {
                return Json.error(HttpConstants.STATUS_BAD_REQUEST, "Email không được để trống.");
            }

            // 4. Validate email format
            HttpResponse emailValidationError = ForgotPasswordValidator.validateEmailFormat(email);
            if (emailValidationError != null) {
                return emailValidationError;
            }

            // 5. Tìm user theo email
            Optional<UserRecord> userOpt = Optional.empty();
            try {
                userOpt = userDAO.findByEmail(email);
            } catch (SQLException e) {
                return DatabaseErrorHandler.handleDatabaseException(e);
            }

            if (userOpt.isEmpty()) {
                return ForgotPasswordValidator.emailNotFound();
            }

            // 6. Tạo reset token
            String resetToken = null;
            try {
                resetToken = authService.requestPasswordReset(email);
            } catch (Exception e) {

                return ForgotPasswordValidator.mailServiceFailed();
            }

            // 7. Validate token được tạo
            if (resetToken == null || resetToken.trim().isEmpty()) {
                return ForgotPasswordValidator.mailServiceFailed();
            }
            LOGGER.info("✓ Password reset token created for: " + email);

            // Trong thực tế, gửi token qua email
            // Ở đây chỉ trả về cho mục đích demo/testing
            return Json.ok(Map.of(
                    "message", "Mã khôi phục mật khẩu đã được tạo thành công.",
                    "email", email,
                    "token", resetToken, // Chỉ để test, production phải gửi qua email
                    "note", "Mã có hiệu lực trong 15 phút."
            ));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing forgot password", e);
            return ForgotPasswordValidator.unexpectedError();
        }
    }

    /**
     * POST /auth/register
     * Body: {
     *   "username": "newuser",
     *   "password": "Pass@123",
     *   "confirmPassword": "Pass@123",
     *   "email": "user@example.com",
     *   "firstname": "Nguyen",
     *   "lastname": "Van A",
     *   "phone": "0123456789",
     *   "address": "123 Street",
     *   "dob": "01/01/1990",
     *   "gender": "Male"
     * }
     * Response: {"message": "Registration successful", "username": "newuser"}
     */
    private static HttpResponse signUp(HttpRequest request) {
        try {
            // 1. Kiểm tra rate limit
            HttpResponse rateLimitError = RateLimiter.checkRateLimit(request);
            if (rateLimitError != null) {
                return rateLimitError;
            }

            // 2. Parse request body
            String body = request.bodyText();
            String username = extractJsonField(body, "username");
            String password = extractJsonField(body, "password");
            String confirmPassword = extractJsonField(body, "confirmPassword");
            String email = extractJsonField(body, "email");
            String firstname = extractJsonField(body, "firstname");
            String lastname = extractJsonField(body, "lastname");
            String phone = extractJsonField(body, "phone");
            String address = extractJsonField(body, "address");
            String dob = extractJsonField(body, "dob");
            String gender = extractJsonField(body, "gender");

            // 3. Validate required fields
           HttpResponse requiredFieldError = SignUpValidator.validateRequiredFields(
                username, firstname, lastname, password, email, phone);
            if (requiredFieldError != null) {
                return requiredFieldError;
            }

            // 5. Validate password format
            HttpResponse passwordError = ChangePasswordValidator.validateNewPasswordFormat(password);
            if (passwordError != null) {
                return passwordError;
            }

            // 6. Validate confirm password
            HttpResponse confirmError = ChangePasswordValidator.validateConfirmPasswordMatch(password, confirmPassword);
            if (confirmError != null) {
                return confirmError;
            }

            // 9. Check if username already exists
            Optional<UserRecord> existingUser;
            try{
                existingUser = userDAO.findByUsername(username);
            } catch (SQLException e){
                return DatabaseErrorHandler.handleDatabaseException(e);
            }
            if (existingUser.isPresent()) {
                return SignUpValidator.existingUsername();
            }

            // 10. Check if email already exists
            Optional<UserRecord> existingEmail;
            try{
                existingEmail = userDAO.findByEmail(email);
            } catch (SQLException e){
                return DatabaseErrorHandler.handleDatabaseException(e);
            }
            if (existingEmail.isPresent()) {
                return SignUpValidator.existingEmail();
            }

            // 11. Hash password
            String hashedPassword = PasswordService.hashPasswordWithSalt(password);

            // 12. Save customer to database
            try{
                boolean saved = userDAO.saveCustomer(
                        username, hashedPassword, firstname, lastname,
                        phone, email, address, dob, gender
                );

                if (saved) {
                    LOGGER.info("✓ New user registered: " + username);
                    return Json.ok(Map.of(
                            "message", "Đăng ký tài khoản thành công!",
                            "username", username,
                            "email", email
                    ));
                } else {
                    return SignUpValidator.errorCreatingUser();
                }
            }
            catch (SQLException e){
                return DatabaseErrorHandler.handleDatabaseException(e);
            }


        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during sign up", e);
            return SignUpValidator.errorCreatingUser();
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
