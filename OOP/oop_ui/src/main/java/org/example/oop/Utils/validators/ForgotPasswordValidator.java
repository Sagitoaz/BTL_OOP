package org.example.oop.Utils.validators;

import java.util.regex.Pattern;

/**
 * ForgotPasswordValidator - Centralized forgot/reset password validation logic
 *
 * Validates email, token, and reset password flow
 * Following the validator pattern similar to CustomerValidator
 */
public class ForgotPasswordValidator {

    // Email regex pattern (RFC 5322 simplified)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final int TOKEN_LENGTH = 6;
    private static final int MAX_EMAIL_LENGTH = 100;

    /**
     * Validate email is not null or empty
     */
    public static ValidationResult validateEmailNotEmpty(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.error(400, "EMPTY_EMAIL",
                "Email không được để trống.");
        }
        return null; // Valid
    }

    /**
     * Validate email format
     */
    public static ValidationResult validateEmailFormat(String email) {
        if (email.length() > MAX_EMAIL_LENGTH) {
            return ValidationResult.error(400, "EMAIL_TOO_LONG",
                "Email không được vượt quá " + MAX_EMAIL_LENGTH + " ký tự.");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ValidationResult.error(400, "INVALID_EMAIL_FORMAT",
                "Định dạng email không hợp lệ. Vui lòng nhập email đúng (vd: example@email.com).");
        }

        return null; // Valid
    }

    /**
     * Full validation for email
     */
    public static ValidationResult validateEmail(String email) {
        // Step 1: Check not empty
        ValidationResult emptyError = validateEmailNotEmpty(email);
        if (emptyError != null) return emptyError;

        // Step 2: Check format
        ValidationResult formatError = validateEmailFormat(email);
        if (formatError != null) return formatError;

        return null; // All validations passed
    }

    /**
     * Validate reset token is not null or empty
     */
    public static ValidationResult validateTokenNotEmpty(String token) {
        if (token == null || token.trim().isEmpty()) {
            return ValidationResult.error(400, "EMPTY_TOKEN",
                "Mã xác thực không được để trống.");
        }
        return null; // Valid
    }

    /**
     * Validate token format and length
     */
    public static ValidationResult validateTokenFormat(String token) {
        if (token.length() != TOKEN_LENGTH) {
            return ValidationResult.error(400, "INVALID_TOKEN_FORMAT",
                "Mã xác thực phải có " + TOKEN_LENGTH + " chữ số.");
        }

        if (!token.matches("^[0-9]+$")) {
            return ValidationResult.error(400, "INVALID_TOKEN_FORMAT",
                "Mã xác thực chỉ được chứa số.");
        }

        return null; // Valid
    }

    /**
     * Full validation for reset token
     */
    public static ValidationResult validateToken(String token) {
        // Step 1: Check not empty
        ValidationResult emptyError = validateTokenNotEmpty(token);
        if (emptyError != null) return emptyError;

        // Step 2: Check format
        ValidationResult formatError = validateTokenFormat(token);
        if (formatError != null) return formatError;

        return null; // All validations passed
    }

    /**
     * Validate full reset password request (email + new password)
     */
    public static ValidationResult validateResetPasswordRequest(
            String email,
            String token,
            String newPassword,
            String confirmPassword) {

        // Step 1: Validate email
        ValidationResult emailError = validateEmail(email);
        if (emailError != null) return emailError;

        // Step 2: Validate token
        ValidationResult tokenError = validateToken(token);
        if (tokenError != null) return tokenError;

        // Step 3: Validate new password (use PasswordValidator)
        ValidationResult passwordError = PasswordValidator.validateNewPassword(newPassword, confirmPassword);
        if (passwordError != null) return passwordError;

        return null; // All validations passed
    }

    // ================================
    // ERROR FACTORY METHODS
    // ================================

    /**
     * Create error for email not found
     */
    public static ValidationResult emailNotFound() {
        return ValidationResult.error(404, "EMAIL_NOT_FOUND",
            "Email không tồn tại trong hệ thống.");
    }

    /**
     * Create error for invalid or expired token
     */
    public static ValidationResult invalidToken() {
        return ValidationResult.error(401, "INVALID_TOKEN",
            "Mã xác thực không hợp lệ hoặc đã hết hạn.");
    }

    /**
     * Create error for token expired
     */
    public static ValidationResult tokenExpired() {
        return ValidationResult.error(401, "TOKEN_EXPIRED",
            "Mã xác thực đã hết hạn. Vui lòng yêu cầu mã mới.");
    }

    /**
     * Create error for token already used
     */
    public static ValidationResult tokenAlreadyUsed() {
        return ValidationResult.error(409, "TOKEN_ALREADY_USED",
            "Mã xác thực đã được sử dụng. Vui lòng yêu cầu mã mới.");
    }

    /**
     * Create error for too many reset requests
     */
    public static ValidationResult tooManyResetRequests() {
        return ValidationResult.error(429, "TOO_MANY_REQUESTS",
            "Bạn đã yêu cầu đặt lại mật khẩu quá nhiều lần. Vui lòng đợi 5 phút và thử lại.");
    }

    /**
     * Create error for email sending failed
     */
    public static ValidationResult emailSendingFailed() {
        return ValidationResult.error(503, "EMAIL_SENDING_FAILED",
            "Không thể gửi email. Vui lòng thử lại sau.");
    }

    /**
     * Create error for reset password failed
     */
    public static ValidationResult resetPasswordFailed() {
        return ValidationResult.error(500, "RESET_FAILED",
            "Không thể đặt lại mật khẩu. Vui lòng thử lại sau.");
    }

    /**
     * Create success response for reset request
     */
    public static ValidationResult resetRequestSuccess(String email) {
        return ValidationResult.success(
            "Mã xác thực đã được gửi đến email " + maskEmail(email) + ". " +
            "Vui lòng kiểm tra hộp thư và làm theo hướng dẫn.");
    }

    /**
     * Create success response for password reset
     */
    public static ValidationResult resetPasswordSuccess() {
        return ValidationResult.success(
            "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập với mật khẩu mới.");
    }

    /**
     * Mask email for security (show only first char and domain)
     * Example: john.doe@example.com -> j*******@example.com
     */
    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 1) {
            return email; // Too short to mask
        }

        return localPart.charAt(0) + "*".repeat(localPart.length() - 1) + "@" + domain;
    }
}
