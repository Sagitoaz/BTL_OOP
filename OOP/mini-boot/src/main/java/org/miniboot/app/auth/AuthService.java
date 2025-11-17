package org.miniboot.app.auth;

import org.miniboot.app.dao.UserDAO;
import org.miniboot.app.dao.UserDAO.UserRecord;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AuthService: Xử lý logic xác thực người dùng
 * Sử dụng UserDAO để đọc từ DATABASE
 * - Kiểm tra thông tin đăng nhập từ PostgreSQL (3 bảng: Admins, Employees, Customers)
 * - Tạo session cho người dùng hợp lệ
 * - Quản lý phiên đăng nhập và quyền truy cập
 */
public class AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());

    // Singleton instance
    private static AuthService instance;

    // SessionManager: quản lý session trong memory
    private static final SessionManager sessionManager = SessionManager.getInstance();

    // UserDAO: Data Access Object để tương tác với database
    private final UserDAO userDAO;

    private AuthService() {
        this.userDAO = new UserDAO();
    }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Đăng nhập với tên đăng nhập và mật khẩu
     * Tìm kiếm trong DATABASE: admins, employees, customers
     */
    public Optional<String> login(String username, String password) {
        try {
            // Tìm user từ DATABASE thông qua UserDAO
            Optional<UserRecord> userOpt = userDAO.findByUsername(username);

            if (userOpt.isEmpty()) {
                LOGGER.warning("Login failed: User does not exist - " + username);
                return Optional.empty();
            }

            UserRecord user = userOpt.get();

            // Check active status
            if (!user.active) {
                LOGGER.warning("Login failed: Account is not active - " + username);
                return Optional.empty();
            }

            // Verify password với bcrypt (password đã hash trong database)
            if (!PasswordService.verifyPassword(password, user.password)) {
                LOGGER.warning("Login failed: Invalid password - " + username);
                return Optional.empty();
            }

            // Tạo session với userId là String
            String sessionId = sessionManager.createSession(String.valueOf(user.id), user.username, user.role);

            LOGGER.info("Login successful: " + username + " (" + user.role + ")");
            return Optional.of(sessionId);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Login error", e);
            return Optional.empty();
        }
    }
    public String findByUsername(String username) {
        try {
            // Tìm user từ DATABASE
            Optional<UserRecord> userOpt = userDAO.findByUsername(username);

            if (userOpt.isEmpty()) {
                throw new Exception("User does not exist: " + username);
            }

            UserRecord user = userOpt.get();
            return user.role;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding user role", e);
            return null;
        }
    }
    public String updatePassword(int userId, String userType, String newHashedPassword){
        try {
            boolean updated = userDAO.updatePassword(userId, userType, newHashedPassword);
            if (updated) {
                LOGGER.info("Password updated successfully for user ID: " + userId);
                return "success";
            } else {
                LOGGER.warning("Failed to update password for user ID: " + userId);
                return "failed";
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating password for user ID: " + userId, e);
            return "error";
        }
    }

    /**
     * Xác thực người dùng và tạo token (cho API)
     * Sử dụng DATABASE thay vì file
     */
    public String authenticate(String username, String password) throws Exception {
        // Tìm user từ DATABASE
        Optional<UserRecord> userOpt = Optional.empty();
        try{
            userOpt = userDAO.findByUsername(username);
        }
        catch (Exception e){
            // Exception khi kết nối DB
            throw new Exception(e);
        }

        if (userOpt.isEmpty()) {
            throw new Exception("User Not Found");
        }

        UserRecord user = userOpt.get();

        // Check active status
        if (!user.active) {
            throw new Exception("Account is not active");
        }

        // Verify password với bcrypt
        if (!PasswordService.verifyPassword(password, user.password)) {
            throw new Exception("incorectPassword");
        }

        // Generate JWT token
        String token = JwtService.generateToken(username);

        LOGGER.info("Authentication successful: " + username + " (" + user.role + ")");
        return token;
    }

    /**
     * Xác thực token từ header Authorization
     * Format: "Authorization: Bearer <token>"
     */
    public static String validateToken(String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Missing authentication token");
        }

        String token = authHeader.substring(7);
        String userId = JwtService.validateTokenAndGetUserId(token);

        if (userId == null) {
            throw new Exception("Invalid or expired token");
        }

        return userId;
    }

    /**
     * Đăng xuất (hủy phiên)
     * Xóa session khỏi memory và database
     */
    public void logout(String sessionId) {
        // Xóa khỏi memory
        sessionManager.invalidateSession(sessionId);

        // Xóa khỏi database
        userDAO.deleteSession(sessionId);

        LOGGER.info("User logged out: sessionId=" + sessionId);
    }

    /**
     * Lấy thông tin phiên đăng nhập hiện tại theo sessionId
     */
    public Optional<SessionManager.Session> getCurrentSession(String sessionId) {
        return sessionManager.getSession(sessionId);
    }

    /**
     * Kiểm tra quyền truy cập cho session
     */
    public boolean hasPermission(String sessionId, RolePermissions.Permission permission) {
        Optional<SessionManager.Session> sessionOpt = sessionManager.getSession(sessionId);
        if (sessionOpt.isEmpty()) return false;

        String role = sessionOpt.get().getRole();
        return RolePermissions.hasPermission(role, permission);
    }

    /**
     * Yêu cầu reset mật khẩu
     * Tạo token và lưu vào database
     */
    public String requestPasswordReset(String email) {
        try {
            // Tìm user theo email từ DATABASE
            Optional<UserRecord> userOpt = userDAO.findByEmail(email);

            if (userOpt.isEmpty()) {
                LOGGER.warning("Password reset failed: Email not found - " + email);
                return null;
            }

            UserRecord user = userOpt.get();

            // Tạo token ngẫu nhiên
            String token = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Lưu token vào DATABASE với thời gian hết hạn 15 phút
            boolean saved = userDAO.savePasswordResetToken(token, user.id, user.role, 15);

            if (saved) {
                LOGGER.info("Password reset token created for: " + email);
                return token;
            } else {
                LOGGER.warning("Failed to save reset token");
                return null;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating reset token", e);
            return null;
        }
    }

    /**
     * Reset mật khẩu với token
     * Validate token từ database và cập nhật password
     */
    public boolean resetPassword(String token, String newPassword) {
        try {
            // Validate token từ DATABASE
            Optional<UserRecord> userOpt = userDAO.validateResetToken(token);

            if (userOpt.isEmpty()) {
                LOGGER.warning("Invalid or expired reset token: " + token);
                return false;
            }

            UserRecord user = userOpt.get();

            // Hash password mới với bcrypt
            String hashedPassword = PasswordService.hashPasswordWithSalt(newPassword);

            // Cập nhật password trong DATABASE
            boolean updated = userDAO.updatePassword(user.id, user.role, hashedPassword);

            if (updated) {
                // Đánh dấu token đã sử dụng
                userDAO.markTokenAsUsed(token);

                LOGGER.info("Password reset successful for user ID: " + user.id);
                return true;
            } else {
                LOGGER.warning("Failed to update password");
                return false;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error resetting password", e);
            return false;
        }
    }

    /**
     * Đăng ký customer mới
     * Lưu vào DATABASE thông qua UserDAO
     */
    public boolean registerCustomer(String username, String email, String password,
                                   String firstname, String lastname, String phone,
                                   String address, String dob, String gender) {
        try {
            // Kiểm tra username đã tồn tại chưa
            Optional<UserRecord> existing = userDAO.findByUsername(username);
            if (existing.isPresent()) {
                LOGGER.warning("Registration failed: Username already exists - " + username);
                return false;
            }

            // Kiểm tra email đã tồn tại chưa
            Optional<UserRecord> existingEmail = userDAO.findByEmail(email);
            if (existingEmail.isPresent()) {
                LOGGER.warning("Registration failed: Email already exists - " + email);
                return false;
            }

            // Hash password với bcrypt
            String hashedPassword = PasswordService.hashPasswordWithSalt(password);

            // Lưu customer vào DATABASE
            boolean saved = userDAO.saveCustomer(username, hashedPassword, firstname, lastname,
                                                phone, email, address, dob, gender);

            if (saved) {
                LOGGER.info("Customer registered successfully: " + username);
                return true;
            } else {
                LOGGER.warning("Failed to save customer");
                return false;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error registering customer", e);
            return false;
        }
    }
}
