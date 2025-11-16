package org.example.oop.Utils.validators;

import org.example.oop.Control.SessionStorage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChangePasswordValidator - Centralized change password validation logic
 *
 * Validates change password flow with:
 * - Session validation
 * - Rate limiting
 * - Password verification
 * - Business rules
 *
 * Following the validator pattern similar to CustomerValidator
 */
public class ChangePasswordValidator {

    // Rate limiting for change password attempts
    private static final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long RATE_LIMIT_WINDOW_MS = 60000; // 1 minute

    private static class RateLimitInfo {
        int attempts;
        long windowStart;

        RateLimitInfo() {
            this.attempts = 1;
            this.windowStart = System.currentTimeMillis();
        }
    }

    /**
     * STEP 0: Rate limiting check (429 - Too Many Requests)
     */
    public static ValidationResult checkRateLimit() {
        String username = SessionStorage.getCurrentUsername();
        if (username == null) {
            username = "anonymous";
        }

        String key = username + ":changePassword";
        long now = System.currentTimeMillis();

        RateLimitInfo info = rateLimitMap.compute(key, (k, existing) -> {
            if (existing == null) {
                return new RateLimitInfo();
            }

            // Check if window has expired
            if (now - existing.windowStart >= RATE_LIMIT_WINDOW_MS) {
                // Reset window
                return new RateLimitInfo();
            }

            // Increment attempts in current window
            existing.attempts++;
            return existing;
        });

        if (info.attempts > MAX_ATTEMPTS) {
            long remainingTime = (RATE_LIMIT_WINDOW_MS - (now - info.windowStart)) / 1000;
            return ValidationResult.error(429, "RATE_LIMIT_EXCEEDED",
                String.format("Bạn đã thực hiện quá nhiều yêu cầu đổi mật khẩu. " +
                    "Vui lòng đợi %d giây và thử lại.", remainingTime));
        }

        return null; // Allowed
    }

    /**
     * STEP 1: Check if user is logged in (401 - Unauthorized)
     */
    public static ValidationResult validateSession() {
        String username = SessionStorage.getCurrentUsername();
        if (username == null || username.isEmpty()) {
            return ValidationResult.error(401, "UNAUTHORIZED",
                "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }

        int userId = SessionStorage.getCurrentUserId();
        String role = SessionStorage.getCurrentUserRole();

        if (userId <= 0 || role == null) {
            return ValidationResult.error(401, "SESSION_INVALID",
                "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.");
        }

        return null; // Valid session
    }

    /**
     * STEP 2: Validate all password fields
     */
    public static ValidationResult validateAllFields(
            String currentPassword,
            String newPassword,
            String confirmNewPassword) {

        // Use PasswordValidator for comprehensive validation
        return PasswordValidator.validateChangePassword(
            currentPassword,
            newPassword,
            confirmNewPassword
        );
    }

    /**
     * STEP 3: Verify current password is correct (called from AuthServiceWrapper)
     */
    public static ValidationResult currentPasswordIncorrect() {
        return ValidationResult.error(401, "OLD_PASSWORD_INCORRECT",
            "Mật khẩu hiện tại không đúng. Vui lòng thử lại.");
    }

    /**
     * STEP 4: Check if account is active
     */
    public static ValidationResult accountInactive() {
        return ValidationResult.error(401, "ACCOUNT_INACTIVE",
            "Tài khoản đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.");
    }

    /**
     * STEP 5: Check if user exists in database
     */
    public static ValidationResult userNotFound() {
        return ValidationResult.error(401, "USER_NOT_FOUND",
            "Người dùng không tồn tại trong hệ thống.");
    }

    // ================================
    // DATABASE ERROR HANDLERS
    // ================================

    /**
     * Handle password hashing error (500)
     */
    public static ValidationResult hashError() {
        return ValidationResult.error(500, "HASH_ERROR",
            "Lỗi mã hóa mật khẩu. Vui lòng thử lại sau.");
    }

    /**
     * Handle concurrent update conflict (409)
     */
    public static ValidationResult concurrentUpdateConflict() {
        return ValidationResult.error(409, "CONFLICT",
            "Có xung đột khi cập nhật. Vui lòng thử lại.");
    }

    /**
     * Handle version conflict (412)
     */
    public static ValidationResult versionConflict() {
        return ValidationResult.error(412, "PRECONDITION_FAILED",
            "Dữ liệu đã thay đổi. Vui lòng làm mới và thử lại.");
    }

    /**
     * Handle database connection error (503)
     */
    public static ValidationResult databaseConnectionError() {
        return ValidationResult.error(503, "SERVICE_UNAVAILABLE",
            "Không thể kết nối đến cơ sở dữ liệu. Vui lòng thử lại sau.");
    }

    /**
     * Handle database timeout (503)
     */
    public static ValidationResult databaseTimeout() {
        return ValidationResult.error(503, "SERVICE_UNAVAILABLE",
            "Kết nối cơ sở dữ liệu bị timeout. Vui lòng thử lại sau.");
    }

    /**
     * Handle generic database error (500)
     */
    public static ValidationResult databaseError(String message) {
        return ValidationResult.error(500, "DATABASE_ERROR",
            "Lỗi cơ sở dữ liệu: " + message);
    }

    /**
     * Handle update failed (500)
     */
    public static ValidationResult updateFailed() {
        return ValidationResult.error(500, "UPDATE_FAILED",
            "Không thể cập nhật mật khẩu. Vui lòng thử lại sau.");
    }

    /**
     * Handle network timeout (504)
     */
    public static ValidationResult networkTimeout() {
        return ValidationResult.error(504, "GATEWAY_TIMEOUT",
            "Kết nối bị timeout. Vui lòng kiểm tra mạng và thử lại.");
    }

    /**
     * Handle unexpected error (500)
     */
    public static ValidationResult unexpectedError(String message) {
        return ValidationResult.error(500, "INTERNAL_ERROR",
            "Lỗi hệ thống không mong đợi: " + message);
    }

    // ================================
    // SUCCESS RESPONSES
    // ================================

    /**
     * Create success response
     */
    public static ValidationResult success() {
        return ValidationResult.success(
            "Đổi mật khẩu thành công! Vui lòng đăng nhập lại với mật khẩu mới.");
    }

    /**
     * Full validation chain for change password (client-side)
     * Call this BEFORE calling AuthServiceWrapper.changePassword()
     */
    public static ValidationResult validateBeforeSubmit(
            String currentPassword,
            String newPassword,
            String confirmNewPassword) {

        // STEP 0: Rate limiting
        ValidationResult rateLimitError = checkRateLimit();
        if (rateLimitError != null) return rateLimitError;

        // STEP 1: Session validation
        ValidationResult sessionError = validateSession();
        if (sessionError != null) return sessionError;

        // STEP 2: Password fields validation
        ValidationResult fieldsError = validateAllFields(
            currentPassword, newPassword, confirmNewPassword);
        if (fieldsError != null) return fieldsError;

        return null; // All validations passed, ready to submit
    }
}
