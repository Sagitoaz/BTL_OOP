package org.example.oop.Control;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AuthServiceWrapper - Wrapper để FE gọi backend AuthService
 * Chỉ sử dụng DATABASE (PostgreSQL) thông qua AuthService
 */
public class AuthServiceWrapper {
    private static final Logger LOGGER = Logger.getLogger(AuthServiceWrapper.class.getName());
    private static Object authServiceInstance;

    static {
        try {
            // Khởi tạo AuthService (singleton)
            Class<?> authServiceClass = Class.forName("org.miniboot.app.auth.AuthService");
            Method getInstanceMethod = authServiceClass.getMethod("getInstance");
            authServiceInstance = getInstanceMethod.invoke(null);
            LOGGER.info("AuthServiceWrapper initialized with DATABASE mode");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "CRITICAL: Cannot initialize AuthService - Database required!", e);
            throw new RuntimeException("Database connection required but failed to initialize", e);
        }
    }

    /**
     * Login - gọi AuthService.login()
     */
    public static Optional<String> login(String username, String password) {
        try {
            // Gọi AuthService.login()
            Method loginMethod = authServiceInstance.getClass().getMethod("login", String.class, String.class);
            Optional<String> sessionOpt = (Optional<String>) loginMethod.invoke(authServiceInstance, username, password);

            if (sessionOpt.isPresent()) {
                String sessionId = sessionOpt.get();

                // Lấy thông tin user từ session để lưu vào SessionStorage
                Method getCurrentSessionMethod = authServiceInstance.getClass().getMethod("getCurrentSession", String.class);
                Optional<?> currentSessionOpt = (Optional<?>) getCurrentSessionMethod.invoke(authServiceInstance, sessionId);

                if (currentSessionOpt.isPresent()) {
                    Object session = currentSessionOpt.get();

                    // getUserId() trả về String, cần convert sang int
                    String userIdStr = (String) session.getClass().getMethod("getUserId").invoke(session);
                    int userId = Integer.parseInt(userIdStr);

                    String role = (String) session.getClass().getMethod("getRole").invoke(session);
                    String userName = (String) session.getClass().getMethod("getUsername").invoke(session);

                    // Lưu vào SessionStorage
                    SessionStorage.setCurrentUserId(userId);
                    SessionStorage.setCurrentUserRole(role);
                    SessionStorage.setCurrentUsername(userName);
                    SessionStorage.setCurrentSessionId(sessionId);

                    LOGGER.info("Login successful: " + username + " [" + role + "]");
                }

                return sessionOpt;
            }

            return Optional.empty();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Login failed", e);
            return Optional.empty();
        }
    }

    /**
     * Logout - gọi AuthService.logout()
     */
    public static void logout(String sessionId) {
        try {
            Method logoutMethod = authServiceInstance.getClass().getMethod("logout", String.class);
            logoutMethod.invoke(authServiceInstance, sessionId);

            // Clear SessionStorage
            SessionStorage.clear();

            LOGGER.info("Logout successful");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Logout failed", e);
        }
    }

    /**
     * Lấy session hiện tại
     */
    public static Optional<Object> getCurrentSession(String sessionId) {
        try {
            Method getCurrentSessionMethod = authServiceInstance.getClass().getMethod("getCurrentSession", String.class);
            return (Optional<Object>) getCurrentSessionMethod.invoke(authServiceInstance, sessionId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Get current session failed", e);
            return Optional.empty();
        }
    }

    /**
     * Kiểm tra password mạnh - gọi PasswordService
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
                && password.matches(".*[@#$%^&+=!].*");
        }
    }

    /**
     * Đăng ký customer mới - gọi AuthService.registerCustomer()
     */
    public static boolean register(String username, String email, String password, String fullName,
                                   String phone, String address, String dob, String gender) {
        try {
            LOGGER.info("Starting registration for username: " + username);

            // Tách fullName thành firstname và lastname
            String[] names = fullName.trim().split("\\s+", 2);
            String lastname = names.length > 0 ? names[0] : "";
            String firstname = names.length > 1 ? names[1] : "";

            LOGGER.info("Registration data - username: " + username + ", email: " + email +
                       ", firstname: " + firstname + ", lastname: " + lastname +
                       ", phone: " + phone + ", dob: " + dob + ", gender: " + gender);

            // Gọi AuthService.registerCustomer()
            Method registerMethod = authServiceInstance.getClass().getMethod(
                "registerCustomer", String.class, String.class, String.class, String.class,
                String.class, String.class, String.class, String.class, String.class);

            boolean success = (boolean) registerMethod.invoke(
                authServiceInstance, username, email, password, firstname, lastname,
                phone, address, dob, gender);

            if (success) {
                LOGGER.info("Registration successful: " + username);
            } else {
                LOGGER.warning("Registration failed for: " + username);
            }

            return success;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Registration failed with exception for username: " + username, e);
            return false;
        }
    }

    /**
     * Yêu cầu reset mật khẩu - gọi AuthService.requestPasswordReset()
     */
    public static String requestPasswordReset(String email) {
        try {
            LOGGER.info("Requesting password reset for email: " + email);

            // Gọi AuthService.requestPasswordReset()
            Method requestResetMethod = authServiceInstance.getClass().getMethod("requestPasswordReset", String.class);
            String token = (String) requestResetMethod.invoke(authServiceInstance, email);

            if (token != null) {
                LOGGER.info("Reset token generated for email: " + email);
            } else {
                LOGGER.warning("Email not found: " + email);
            }

            return token;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Password reset request failed", e);
            return null;
        }
    }

    /**
     * Reset mật khẩu bằng token - gọi AuthService.resetPassword()
     */
    public static boolean resetPassword(String token, String newPassword) {
        try {
            LOGGER.info("Resetting password with token");

            // Gọi AuthService.resetPassword()
            Method resetMethod = authServiceInstance.getClass().getMethod("resetPassword", String.class, String.class);
            boolean success = (boolean) resetMethod.invoke(authServiceInstance, token, newPassword);

            if (success) {
                LOGGER.info("Password reset successful");
            } else {
                LOGGER.warning("Password reset failed - invalid or expired token");
            }

            return success;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Password reset failed", e);
            return false;
        }
    }

    /**
     * Hash password với salt - gọi PasswordService
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
     * Verify password - gọi PasswordService
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

            // Tách fullName thành firstname và lastname (theo cấu trúc tiếng Việt: HỌ -
            // TÊN)
            // Ví dụ: "Nguyễn Văn A" → lastname="Nguyễn", firstname="Văn A"
            String[] names = fullName.trim().split("\\s+", 2);
            String lastname = names.length > 0 ? names[0] : ""; // Họ (từ đầu tiên)
            String firstname = names.length > 1 ? names[1] : ""; // Tên (phần còn lại)

            // Gender đã được chuyển đổi sang tiếng Anh IN HOA (MALE, FEMALE, OTHER) từ
            // Controller
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
     * Yêu cầu reset mật khẩu - tạo token đơn giản (không lưu DB vì bảng không tồn
     * tại)
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

    /**
     * Đổi mật khẩu theo username (YÊU CẦU PHẢI ĐĂNG NHẬP)
     * Tự động tìm trong cả Employee và Customer
     * 
     * ⚠️ BẢO MẬT: Chỉ cho phép đổi mật khẩu của chính user đang đăng nhập
     * 
     * @param username        Tên đăng nhập (PHẢI TRÙNG với user đang đăng nhập)
     * @param currentPassword Mật khẩu hiện tại
     * @param newPassword     Mật khẩu mới
     * @return true nếu thành công
     */
    public static boolean changePasswordByUsername(String username, String currentPassword, String newPassword) {
        try {
            // 🔒 BƯỚC 0: KIỂM TRA BẢO MẬT - User phải đang đăng nhập
            String loggedInUsername = SessionStorage.getCurrentUsername();

            if (loggedInUsername == null || loggedInUsername.isEmpty()) {
                LOGGER.warning("✗ Security violation: No user logged in");
                throw new RuntimeException("Bạn cần đăng nhập để đổi mật khẩu");
            }
            if (!username.equals(loggedInUsername)) {
                LOGGER.warning("✗ Security violation: User '" + loggedInUsername +
                        "' attempted to change password for '" + username + "'");
                throw new RuntimeException("Bạn chỉ có thể đổi mật khẩu của chính mình!\n\n" +
                        "Tài khoản đang đăng nhập: " + loggedInUsername + "\n" +
                        "Tài khoản bạn đang cố đổi: " + username);
            }
            Method findByUsernameMethod = userDAOInstance.getClass().getMethod("findByUsername", String.class);
            Optional<?> userOpt = (Optional<?>) findByUsernameMethod.invoke(userDAOInstance, username);
            if (!userOpt.isPresent()) {
                LOGGER.warning("✗ User not found: " + username);
                throw new RuntimeException("Không tìm thấy tài khoản");
            }
            Object userRecord = userOpt.get();
            int userId = (int) userRecord.getClass().getField("id").get(userRecord);
            String role = (String) userRecord.getClass().getField("role").get(userRecord);
            String storedPassword = (String) userRecord.getClass().getField("password").get(userRecord);
            boolean active = (boolean) userRecord.getClass().getField("active").get(userRecord);
            if (!active) {
                LOGGER.warning("✗ Account inactive: " + username);
                throw new RuntimeException("Tài khoản đã bị vô hiệu hóa");
            }
            if (!verifyPassword(currentPassword, storedPassword)) {
                LOGGER.warning("✗ Current password incorrect for: " + username);
                throw new RuntimeException("Mật khẩu hiện tại không đúng");
            }
            String hashedPassword = hashPasswordWithSalt(newPassword);
            Method updatePasswordMethod = userDAOInstance.getClass().getMethod(
                    "updatePassword", int.class, String.class, String.class);
            boolean success = (boolean) updatePasswordMethod.invoke(
                    userDAOInstance, userId, role, hashedPassword);
            if (success) {
                LOGGER.info("✓ Password changed successfully for: " + username + " [" + role + "]");
                return true;
            } else {
                LOGGER.warning("✗ Password update failed in database for: " + username);
                throw new RuntimeException("Cập nhật mật khẩu thất bại");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Change password by username failed", e);
            throw new RuntimeException("Lỗi hệ thống khi đổi mật khẩu: " + e.getMessage());
        }
    }
}
