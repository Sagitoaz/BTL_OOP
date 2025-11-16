package org.example.oop.Utils.validators;

/**
 * ValidationResult - Unified result class for validation operations
 *
 * Used across all validators for consistent error handling
 * Similar to ApiResponse pattern
 */
public class ValidationResult {
    private final boolean success;
    private final int statusCode;
    private final String errorCode;
    private final String message;

    private ValidationResult(boolean success, int statusCode, String errorCode, String message) {
        this.success = success;
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.message = message;
    }

    public static ValidationResult success(String message) {
        return new ValidationResult(true, 200, null, message);
    }

    public static ValidationResult error(int statusCode, String errorCode, String message) {
        return new ValidationResult(false, statusCode, errorCode, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        if (success) {
            return "Success: " + message;
        } else {
            return String.format("Error [%d - %s]: %s", statusCode, errorCode, message);
        }
    }
}

