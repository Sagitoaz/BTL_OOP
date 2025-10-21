package org.example.oop.Control;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AuthServiceWrapper - DATABASE ONLY VERSION
 *
 * Chỉ sử dụng PostgreSQL database
 * Không còn fallback về file
 * Không lưu session vào DB (vì bảng không tồn tại trong schema)
 */
public class AuthServiceWrapper {
    private static final Logger LOGGER = Logger.getLogger(AuthServiceWrapper.class.getName());
    private static Object userDAOInstance;

    static {
        try {
            // Khởi tạo UserDAO (database-based)
            Class<?> userDAOClass = Class.forName("org.miniboot.app.dao.UserDAO");
            userDAOInstance = userDAOClass.getDeclaredConstructor().newInstance();
            LOGGER.info("✓ AuthServiceWrapper initialized with DATABASE mode");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "✗ CRITICAL: Cannot initialize UserDAO - Database required!", e);
            throw new RuntimeException("Database connection required but failed to initialize", e);
        }
    }

    /**
     * Login - chỉ dùng database, không lưu session
     */
    public static Optional<String> login(String username, String password) {
        try {
            // Tìm user trong database
            Method findByUsernameMethod = userDAOInstance.getClass().getMethod("findByUsername", String.class);
            Optional<?> userOpt = (Optional<?>) findByUsernameMethod.invoke(userDAOInstance, username);

            if (!userOpt.isPresent()) {
                LOGGER.info("✗ Login failed: User not found - " + username);
                return Optional.empty();
            }

            Object userRecord = userOpt.get();

            // Lấy thông tin user
            String storedPassword = (String) userRecord.getClass().getField("password").get(userRecord);
            boolean active = (boolean) userRecord.getClass().getField("active").get(userRecord);

            if (!active) {
                LOGGER.info("✗ Login failed: Account inactive - " + username);
                return Optional.empty();
            }

            // Verify password
            if (!verifyPassword(password, storedPassword)) {
                LOGGER.info("✗ Login failed: Invalid password - " + username);
                return Optional.empty();
            }

            // Tạo session ID (chỉ lưu trong memory, không lưu DB)
            String sessionId = java.util.UUID.randomUUID().toString();
            int userId = (int) userRecord.getClass().getField("id").get(userRecord);
            String role = (String) userRecord.getClass().getField("role").get(userRecord);

            // Lưu thông tin user vào SessionStorage
            SessionStorage.setCurrentUserId(userId);
            SessionStorage.setCurrentUserRole(role);
            SessionStorage.setCurrentUsername(username);

            LOGGER.info("✓ Login successful: " + username + " [" + role + "]");
            return Optional.of(sessionId);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Login failed", e);
            return Optional.empty();
        }
    }

    /**
     * Logout - xóa session khỏi memory
     */
    public static void logout(String sessionId) {
        try {
            SessionStorage.clear();
            LOGGER.info("✓ Logout successful");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Logout failed", e);
        }
    }

    /**
     * Lấy session hiện tại từ memory
     */
    public static Optional<Object> getCurrentSession(String sessionId) {
        try {
            if (SessionStorage.getCurrentSessionId() != null &&
                SessionStorage.getCurrentSessionId().equals(sessionId)) {
                return Optional.of(sessionId);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Get current session failed", e);
        }
        return Optional.empty();
    }

    /**
     * Hash password với salt
     */
    public static String hashPasswordWithSalt(String password) {
        try {
            Class<?> passwordServiceClass = Class.forName("org.miniboot.app.auth.PasswordService");
            Method hashMethod = passwordServiceClass.getMethod("hashPasswordWithSalt", String.class);
            return (String) hashMethod.invoke(null, password);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Password hashing failed", e);
            throw new RuntimeException("Password hashing failed - cannot continue", e);
        }
    }

    /**
     * Verify password
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            Class<?> passwordServiceClass = Class.forName("org.miniboot.app.auth.PasswordService");
            Method verifyMethod = passwordServiceClass.getMethod("verifyPassword", String.class, String.class);
            return (Boolean) verifyMethod.invoke(null, password, hashedPassword);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Password verification failed", e);
            return false;
        }
    }

    /**
     * Kiểm tra password mạnh
     */
    public static boolean isPasswordStrong(String password) {
        try {
            Class<?> passwordServiceClass = Class.forName("org.miniboot.app.auth.PasswordService");
            Method isStrongMethod = passwordServiceClass.getMethod("isPasswordStrong", String.class);
            return (Boolean) isStrongMethod.invoke(null, password);
        } catch (Exception e) {
            // Fallback password strength check
            return password != null && password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*")
                && password.matches(".*[!@#$%^&*()].*");
        }
    }

    /**
     * Đăng ký user mới - chỉ lưu vào database với đầy đủ thông tin
     */
    public static boolean register(String username, String email, String password, String fullName,
                                   String phone, String address, String dob, String gender) {
        try {
            // Kiểm tra username đã tồn tại
            Method findByUsernameMethod = userDAOInstance.getClass().getMethod("findByUsername", String.class);
            Optional<?> existingUser = (Optional<?>) findByUsernameMethod.invoke(userDAOInstance, username);

            if (existingUser.isPresent()) {
                LOGGER.warning("✗ Registration failed: Username exists - " + username);
                return false;
            }

            // Hash password
            String hashedPassword = hashPasswordWithSalt(password);

            // Tách fullName thành firstname và lastname (theo cấu trúc tiếng Việt: HỌ - TÊN)
            // Ví dụ: "Nguyễn Văn A" → lastname="Nguyễn", firstname="Văn A"
            String[] names = fullName.trim().split("\\s+", 2);
            String lastname = names.length > 0 ? names[0] : "";       // Họ (từ đầu tiên)
            String firstname = names.length > 1 ? names[1] : "";      // Tên (phần còn lại)

            // Gender đã được chuyển đổi sang tiếng Anh IN HOA (MALE, FEMALE, OTHER) từ Controller
            // Không cần chuyển đổi nữa, truyền trực tiếp vào database

            // Lưu vào database với đầy đủ thông tin
            Method saveCustomerMethod = userDAOInstance.getClass().getMethod(
                "saveCustomer", String.class, String.class, String.class, String.class,
                String.class, String.class, String.class, String.class, String.class);

            boolean success = (boolean) saveCustomerMethod.invoke(
                userDAOInstance, username, hashedPassword, firstname, lastname,
                phone, email, address, dob, gender);

            if (success) {
                LOGGER.info("✓ Registration successful: " + username + " with gender: " + gender);
            }

            return success;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Registration failed", e);
            return false;
        }
    }

    /**
     * Yêu cầu reset mật khẩu - tạo token đơn giản (không lưu DB vì bảng không tồn tại)
     */
    public static String requestPasswordReset(String email) {
        try {
            // Tìm user theo email
            Method findByEmailMethod = userDAOInstance.getClass().getMethod("findByEmail", String.class);
            Optional<?> userOpt = (Optional<?>) findByEmailMethod.invoke(userDAOInstance, email);

            if (!userOpt.isPresent()) {
                LOGGER.warning("✗ Email not found: " + email);
                return null;
            }

            Object userRecord = userOpt.get();
            int userId = (int) userRecord.getClass().getField("id").get(userRecord);
            String role = (String) userRecord.getClass().getField("role").get(userRecord);

            // Tạo token đơn giản (6 chữ số cuối của timestamp)
            String token = String.valueOf(System.currentTimeMillis()).substring(7);

            // Lưu token tạm trong SessionStorage để validate
            SessionStorage.setResetToken(token);
            SessionStorage.setResetUserId(userId);
            SessionStorage.setResetUserRole(role);

            LOGGER.info("✓ Reset token generated for email: " + email);
            return token;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Password reset request failed", e);
            return null;
        }
    }

    /**
     * Reset mật khẩu bằng token - cập nhật vào database
     */
    public static boolean resetPassword(String token, String newPassword) {
        try {
            // Validate token từ SessionStorage
            String storedToken = SessionStorage.getResetToken();
            if (storedToken == null || !storedToken.equals(token)) {
                LOGGER.warning("✗ Invalid or expired token");
                return false;
            }

            int userId = SessionStorage.getResetUserId();
            String userType = SessionStorage.getResetUserRole();

            // Hash password mới
            String hashedPassword = hashPasswordWithSalt(newPassword);

            // Cập nhật password trong database
            Method updatePasswordMethod = userDAOInstance.getClass().getMethod(
                "updatePassword", int.class, String.class, String.class);
            boolean success = (boolean) updatePasswordMethod.invoke(
                userDAOInstance, userId, userType, hashedPassword);

            if (success) {
                // Xóa token sau khi sử dụng
                SessionStorage.clearResetToken();
                LOGGER.info("✓ Password reset successful for user ID: " + userId);
            }

            return success;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Password reset failed", e);
            return false;
        }
    }

    /**
     * Đổi mật khẩu cho user đang đăng nhập
     */
    public static boolean changePassword(String currentPassword, String newPassword) {
        try {
            String username = SessionStorage.getCurrentUsername();
            if (username == null) {
                LOGGER.warning("✗ No user logged in");
                return false;
            }

            // Verify current password
            Method findByUsernameMethod = userDAOInstance.getClass().getMethod("findByUsername", String.class);
            Optional<?> userOpt = (Optional<?>) findByUsernameMethod.invoke(userDAOInstance, username);

            if (!userOpt.isPresent()) {
                return false;
            }

            Object userRecord = userOpt.get();
            String storedPassword = (String) userRecord.getClass().getField("password").get(userRecord);

            if (!verifyPassword(currentPassword, storedPassword)) {
                LOGGER.warning("✗ Current password incorrect");
                return false;
            }

            // Update password
            int userId = SessionStorage.getCurrentUserId();
            String role = SessionStorage.getCurrentUserRole();
            String hashedPassword = hashPasswordWithSalt(newPassword);

            Method updatePasswordMethod = userDAOInstance.getClass().getMethod(
                "updatePassword", int.class, String.class, String.class);
            boolean success = (boolean) updatePasswordMethod.invoke(
                userDAOInstance, userId, role, hashedPassword);

            if (success) {
                LOGGER.info("✓ Password changed successfully for: " + username);
            }

            return success;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Change password failed", e);
            return false;
        }
    }
}
