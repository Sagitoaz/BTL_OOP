package org.example.oop.Control;

import java.util.Optional;

/**
 * SessionStorage - Quản lý session cho JavaFX UI
 *
 * Lưu trữ session trong memory (không dùng database)
 */
public class SessionStorage {
    private static String currentSessionId;
    private static int currentUserId;
    private static String currentUserRole;
    private static String currentUsername;

    // Dữ liệu cho reset password
    private static String resetToken;
    private static int resetUserId;
    private static String resetUserRole;

    // Session methods
    public static void setCurrentSessionId(String sessionId) {
        currentSessionId = sessionId;
    }

    public static String getCurrentSessionId() {
        return currentSessionId;
    }

    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserRole(String role) {
        currentUserRole = role;
    }

    public static String getCurrentUserRole() {
        return currentUserRole;
    }

    public static void setCurrentUsername(String username) {
        currentUsername = username;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    // Reset password methods
    public static void setResetToken(String token) {
        resetToken = token;
    }

    public static String getResetToken() {
        return resetToken;
    }

    public static void setResetUserId(int userId) {
        resetUserId = userId;
    }

    public static int getResetUserId() {
        return resetUserId;
    }

    public static void setResetUserRole(String role) {
        resetUserRole = role;
    }

    public static String getResetUserRole() {
        return resetUserRole;
    }

    public static void clearResetToken() {
        resetToken = null;
        resetUserId = 0;
        resetUserRole = null;
    }

    // Legacy methods để tương thích với code cũ
    public static Optional<Object> getCurrentSession() {
        if (currentSessionId == null) {
            return Optional.empty();
        }
        return AuthServiceWrapper.getCurrentSession(currentSessionId);
    }

    public static boolean isLoggedIn() {
        return currentSessionId != null && currentUsername != null;
    }

    public static void logout() {
        if (currentSessionId != null) {
            AuthServiceWrapper.logout(currentSessionId);
        }
        clear();
    }

    public static void clear() {
        currentSessionId = null;
        currentUserId = 0;
        currentUserRole = null;
        currentUsername = null;
        clearResetToken();
    }
}
