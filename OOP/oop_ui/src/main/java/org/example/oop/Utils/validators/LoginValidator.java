package org.example.oop.Utils.validators;

/**
 * LoginValidator - Centralized login validation logic
 *
 * Validates login credentials and user state
 * Following the validator pattern similar to CustomerValidator
 */
public class LoginValidator {

    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_PASSWORD_LENGTH = 128;

    /**
     * Validate username is not null or empty
     */
    public static ValidationResult validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.error(400, "EMPTY_USERNAME",
                "Tên đăng nhập không được để trống.");
        }

        if (username.length() > MAX_USERNAME_LENGTH) {
            return ValidationResult.error(400, "USERNAME_TOO_LONG",
                "Tên đăng nhập không được vượt quá " + MAX_USERNAME_LENGTH + " ký tự.");
        }

        // Check for invalid characters
        if (!username.matches("^[a-zA-Z0-9_@.]+$")) {
            return ValidationResult.error(400, "INVALID_USERNAME",
                "Tên đăng nhập chỉ được chứa chữ cái, số và ký tự _, @, .");
        }

        return null; // Valid
    }

    /**
     * Validate password is not null or empty
     */
    public static ValidationResult validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return ValidationResult.error(400, "EMPTY_PASSWORD",
                "Mật khẩu không được để trống.");
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            return ValidationResult.error(400, "PASSWORD_TOO_LONG",
                "Mật khẩu không hợp lệ.");
        }

        return null; // Valid
    }

    /**
     * Full validation for login credentials
     */
    public static ValidationResult validateLoginCredentials(String username, String password) {
        // Step 1: Validate username
        ValidationResult usernameError = validateUsername(username);
        if (usernameError != null) return usernameError;

        // Step 2: Validate password
        ValidationResult passwordError = validatePassword(password);
        if (passwordError != null) return passwordError;

        return null; // All validations passed
    }

    /**
     * Create error for user not found
     */
    public static ValidationResult userNotFound() {
        return ValidationResult.error(401, "USER_NOT_FOUND",
            "Tên đăng nhập hoặc mật khẩu không đúng.");
    }

    /**
     * Create error for incorrect password
     */
    public static ValidationResult incorrectPassword() {
        return ValidationResult.error(401, "INCORRECT_PASSWORD",
            "Tên đăng nhập hoặc mật khẩu không đúng.");
    }

    /**
     * Create error for inactive account
     */
    public static ValidationResult accountInactive() {
        return ValidationResult.error(401, "ACCOUNT_INACTIVE",
            "Tài khoản đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.");
    }

    /**
     * Create error for account locked (too many failed attempts)
     */
    public static ValidationResult accountLocked(long unlockTimeMinutes) {
        return ValidationResult.error(429, "ACCOUNT_LOCKED",
            "Tài khoản tạm thời bị khóa do đăng nhập sai quá nhiều lần. " +
            "Vui lòng thử lại sau " + unlockTimeMinutes + " phút.");
    }

    /**
     * Create error for too many login attempts
     */
    public static ValidationResult tooManyAttempts() {
        return ValidationResult.error(429, "TOO_MANY_ATTEMPTS",
            "Bạn đã đăng nhập sai quá nhiều lần. Vui lòng đợi 1 phút và thử lại.");
    }

    /**
     * Create error for session creation failed
     */
    public static ValidationResult sessionCreationFailed() {
        return ValidationResult.error(500, "SESSION_ERROR",
            "Không thể tạo phiên đăng nhập. Vui lòng thử lại.");
    }

    /**
     * Create error for database error during login
     */
    public static ValidationResult databaseError(String message) {
        return ValidationResult.error(503, "DATABASE_ERROR",
            "Lỗi kết nối cơ sở dữ liệu: " + message);
    }

    /**
     * Create error for unexpected error
     */
    public static ValidationResult unexpectedError() {
        return ValidationResult.error(500, "INTERNAL_ERROR",
            "Lỗi hệ thống không mong đợi. Vui lòng thử lại sau.");
    }
}
