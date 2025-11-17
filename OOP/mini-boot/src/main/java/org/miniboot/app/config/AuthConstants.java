package org.miniboot.app.config;

/**
 * AuthConstants: Chứa các hằng số liên quan đến Authentication & Authorization
 */
public final class AuthConstants {

    // Private constructor để ngăn khởi tạo
    private AuthConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ========== JWT CONFIGURATION ==========
    public static final String JWT_SECRET_KEY = "miniboot-secret-key-must-be-at-least-256-bits-long-for-security";
    public static final long JWT_EXPIRATION_TIME = 86400000; // 24 hours in milliseconds
    public static final String JWT_ISSUER = "mini-boot-server";
    public static final String JWT_AUDIENCE = "mini-boot-client";

    // ========== TOKEN CONFIGURATION ==========
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_TYPE = "Bearer";
    public static final int TOKEN_EXPIRATION_SECONDS = 86400; // 24 hours

    // ========== SESSION CONFIGURATION ==========
    public static final long SESSION_TIMEOUT = 1800000; // 30 minutes in milliseconds
    public static final String SESSION_ID_HEADER = "X-Session-ID";
    public static final String SESSION_COOKIE_NAME = "SESSIONID";

    // ========== PASSWORD CONFIGURATION ==========
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PASSWORD_MAX_LENGTH = 128;
    public static final int BCRYPT_ROUNDS = 10;

    // ========== AUTHENTICATION ERROR MESSAGES ==========
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid username or password";
    public static final String ERROR_TOKEN_EXPIRED = "Token has expired";
    public static final String ERROR_TOKEN_INVALID = "Invalid token";
    public static final String ERROR_TOKEN_MISSING = "Authorization token is missing";
    public static final String ERROR_UNAUTHORIZED = "Unauthorized access";
    public static final String ERROR_FORBIDDEN = "Access forbidden";
    public static final String ERROR_USER_NOT_FOUND = "User not found";
    public static final String ERROR_USER_ALREADY_EXISTS = "User already exists";
    public static final String ERROR_WEAK_PASSWORD = "Password is too weak";
    public static final String ERROR_MISSING_USERNAME = "Username is required";
    public static final String ERROR_MISSING_PASSWORD = "Password is required";

    // ========== AUTHENTICATION SUCCESS MESSAGES ==========
    public static final String SUCCESS_LOGIN = "Login successful";
    public static final String SUCCESS_LOGOUT = "Logout successful";
    public static final String SUCCESS_REGISTER = "Registration successful";
    public static final String SUCCESS_PASSWORD_CHANGED = "Password changed successfully";
    public static final String SUCCESS_PASSWORD_RESET = "Password reset successful";

    // ========== REQUEST FIELD NAMES ==========
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_PASSWORD = "password";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_OLD_PASSWORD = "oldPassword";
    public static final String FIELD_NEW_PASSWORD = "newPassword";
    public static final String FIELD_CONFIRM_PASSWORD = "confirmPassword";

    // ========== RESPONSE FIELD NAMES ==========
    public static final String FIELD_ACCESS_TOKEN = "access_token";
    public static final String FIELD_TOKEN_TYPE = "token_type";
    public static final String FIELD_EXPIRES_IN = "expires_in";
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_ROLE = "role";
    public static final String FIELD_MESSAGE = "message";

    // ========== USER ROLES ==========
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";
    public static final String ROLE_DOCTOR = "doctor";
    public static final String ROLE_NURSE = "nurse";
    public static final String ROLE_RECEPTIONIST = "receptionist";
    public static final String ROLE_ACCOUNTANT = "accountant";
    public static final String ROLE_INVENTORY_STAFF = "inventory_staff";
    public static final String ROLE_CUSTOMER = "customer";

    // ========== PERMISSION ACTIONS ==========
    public static final String PERMISSION_READ = "read";
    public static final String PERMISSION_WRITE = "write";
    public static final String PERMISSION_UPDATE = "update";
    public static final String PERMISSION_DELETE = "delete";
    public static final String PERMISSION_ADMIN = "admin";

    // ========== AUTHENTICATION ENDPOINTS ==========
    public static final String ENDPOINT_LOGIN = "/auth/login";
    public static final String ENDPOINT_LOGOUT = "/auth/logout";
    public static final String ENDPOINT_REGISTER = "/auth/register";
    public static final String ENDPOINT_PROFILE = "/auth/profile";
    public static final String ENDPOINT_CHANGE_PASSWORD = "/auth/change-password";
    public static final String ENDPOINT_RESET_PASSWORD = "/auth/reset-password";
    public static final String ENDPOINT_VERIFY_TOKEN = "/auth/verify";
    public static final String ENDPOINT_REFRESH_TOKEN = "/auth/refresh";
}

