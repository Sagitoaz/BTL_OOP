package org.miniboot.app.util.errorvalidation;

import org.miniboot.app.http.HttpResponse;

import java.util.regex.Pattern;

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
    public static HttpResponse validateEmailNotEmpty(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationUtils.error(400, "BAD_REQUEST",
                    "Email không được để trống.");
        }
        return null; // Valid
    }

    /**
     * Validate email format
     */
    public static HttpResponse validateEmailFormat(String email) {
        if (email.length() > MAX_EMAIL_LENGTH) {
            return ValidationUtils.error(400, "BAD_REQUEST",
                    "Email không được vượt quá " + MAX_EMAIL_LENGTH + " ký tự.");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ValidationUtils.error(400, "BAD_REQUEST",
                    "Định dạng email không hợp lệ. Vui lòng nhập email đúng (vd: example@email.com).");
        }

        return null; // Valid
    }

    /**
     * Full validation for email
     */
    public static HttpResponse validateEmail(String email) {
        // Step 1: Check not empty
        HttpResponse emptyError = validateEmailNotEmpty(email);
        if (emptyError != null) return emptyError;

        // Step 2: Check format
        HttpResponse formatError = validateEmailFormat(email);
        if (formatError != null) return formatError;

        return null; // All validations passed
    }

    /**
     * Validate reset token is not null or empty
     */
    public static HttpResponse validateTokenNotEmpty(String token) {
        if (token == null || token.trim().isEmpty()) {
            return ValidationUtils.error(400, "BAD_REQUEST",
                    "Mã xác thực không được để trống.");
        }
        return null; // Valid
    }

    /**
     * Validate token format and length
     */
    public static HttpResponse validateTokenFormat(String token) {
        if (token.length() != TOKEN_LENGTH) {
            return ValidationUtils.error(400, "BAD_REQUEST",
                    "Mã xác thực phải có " + TOKEN_LENGTH + " chữ số.");
        }

        if (!token.matches("^[0-9]+$")) {
            return ValidationUtils.error(400, "BAD_REQUEST",
                    "Mã xác thực chỉ được chứa số.");
        }

        return null; // Valid
    }

    /**
     * Full validation for reset token
     */
    public static HttpResponse validateToken(String token) {
        // Step 1: Check not empty
        HttpResponse emptyError = validateTokenNotEmpty(token);
        if (emptyError != null) return emptyError;

        // Step 2: Check format
        HttpResponse formatError = validateTokenFormat(token);
        if (formatError != null) return formatError;

        return null; // All validations passed
    }

    /**
     * Validate full reset password request (email + new password)
     */
    public static HttpResponse validateResetPasswordRequest(
            String email,
            String token,
            String newPassword,
            String confirmPassword) {

        // Step 1: Validate email
        HttpResponse emailError = validateEmail(email);
        if (emailError != null) return emailError;

        // Step 2: Validate token
        HttpResponse tokenError = validateToken(token);
        if (tokenError != null) return tokenError;

        // Step 3: Validate new password (use PasswordValidator)
        HttpResponse passwordError = ChangePasswordValidator.validateConfirmPasswordMatch(newPassword, confirmPassword);
        if (passwordError != null) return passwordError;

        return null; // All validations passed
    }

    /**
     * Create error for email not found
     */
    public static HttpResponse emailNotFound() {
        return ValidationUtils.error(404, "NOT_FOUND",
                "Email không tồn tại trong hệ thống.");
    }




    /**
     * Create error for email sending failed
     */
    public static HttpResponse emailSendingFailed() {
        return ValidationUtils.error(503, "SERVICE_UNAVAILABLE",
                "Không thể gửi email. Vui lòng thử lại sau.");
    }
    public static HttpResponse unexpectedError() {
        return ValidationUtils.error(500, "INTERNAL_SERVER_ERROR",
                "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.");
    }





}
