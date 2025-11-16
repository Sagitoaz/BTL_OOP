package org.example.oop.Utils.validators;

/**
 * PasswordValidator - Centralized password validation logic
 *
 * Validates password rules for:
 * - Change Password
 * - Reset Password
 * - Registration
 *
 * Following the validator pattern similar to CustomerValidator
 */
public class PasswordValidator {

    // Password constraints
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;

    /**
     * Validate password is not null or empty
     */
    public static ValidationResult validateNotEmpty(String password, String fieldName) {
        if (password == null || password.trim().isEmpty()) {
            return ValidationResult.error(400, "EMPTY_PASSWORD",
                fieldName + " không được để trống.");
        }
        return null; // Valid
    }

    /**
     * Validate password length
     */
    public static ValidationResult validateLength(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return ValidationResult.error(400, "PASSWORD_TOO_SHORT",
                "Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự.");
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return ValidationResult.error(400, "PASSWORD_TOO_LONG",
                "Mật khẩu không được vượt quá " + MAX_PASSWORD_LENGTH + " ký tự.");
        }
        return null; // Valid
    }

    /**
     * Validate password strength (complexity)
     * Must contain: uppercase, lowercase, digit, special character
     */
    public static ValidationResult validateStrength(String password) {
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        if (!hasUppercase) {
            return ValidationResult.error(400, "WEAK_PASSWORD",
                "Mật khẩu phải chứa ít nhất một chữ cái viết hoa.");
        }
        if (!hasLowercase) {
            return ValidationResult.error(400, "WEAK_PASSWORD",
                "Mật khẩu phải chứa ít nhất một chữ cái viết thường.");
        }
        if (!hasDigit) {
            return ValidationResult.error(400, "WEAK_PASSWORD",
                "Mật khẩu phải chứa ít nhất một chữ số.");
        }
        if (!hasSpecial) {
            return ValidationResult.error(400, "WEAK_PASSWORD",
                "Mật khẩu phải chứa ít nhất một ký tự đặc biệt (!@#$%^&*...).");
        }

        return null; // Valid
    }

    /**
     * Validate passwords match (for confirmation)
     */
    public static ValidationResult validatePasswordsMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            return ValidationResult.error(400, "PASSWORD_MISMATCH",
                "Mật khẩu xác nhận không khớp.");
        }
        return null; // Valid
    }

    /**
     * Validate new password is different from old password
     */
    public static ValidationResult validatePasswordChanged(String oldPassword, String newPassword) {
        if (oldPassword.equals(newPassword)) {
            return ValidationResult.error(400, "SAME_PASSWORD",
                "Mật khẩu mới phải khác mật khẩu hiện tại.");
        }
        return null; // Valid
    }

    /**
     * Full validation for new password (registration or reset)
     */
    public static ValidationResult validateNewPassword(String password, String confirmPassword) {
        // Step 1: Check not empty
        ValidationResult emptyError = validateNotEmpty(password, "Mật khẩu");
        if (emptyError != null) return emptyError;

        ValidationResult confirmEmptyError = validateNotEmpty(confirmPassword, "Xác nhận mật khẩu");
        if (confirmEmptyError != null) return confirmEmptyError;

        // Step 2: Check length
        ValidationResult lengthError = validateLength(password);
        if (lengthError != null) return lengthError;

        // Step 3: Check strength
        ValidationResult strengthError = validateStrength(password);
        if (strengthError != null) return strengthError;

        // Step 4: Check passwords match
        ValidationResult matchError = validatePasswordsMatch(password, confirmPassword);
        if (matchError != null) return matchError;

        return null; // All validations passed
    }

    /**
     * Full validation for change password
     */
    public static ValidationResult validateChangePassword(
            String currentPassword,
            String newPassword,
            String confirmNewPassword) {

        // Step 1: Validate current password not empty
        ValidationResult currentEmptyError = validateNotEmpty(currentPassword, "Mật khẩu hiện tại");
        if (currentEmptyError != null) return currentEmptyError;

        // Step 2: Validate new password (with confirmation)
        ValidationResult newPasswordError = validateNewPassword(newPassword, confirmNewPassword);
        if (newPasswordError != null) return newPasswordError;

        // Step 3: Check new password is different from current
        ValidationResult changedError = validatePasswordChanged(currentPassword, newPassword);
        if (changedError != null) return changedError;

        return null; // All validations passed
    }
}
