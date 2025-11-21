package org.miniboot.app.auth;

import org.miniboot.app.dao.UserDAO;
import org.miniboot.app.dao.UserDAO.UserRecord;

import java.security.SecureRandom;
import java.sql.SQLException;
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

    public String updatePassword(int userId, String userType, String newHashedPassword) throws SQLException {
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
            throw new SQLException(e);
        }
    }

    /**
     * Xác thực người dùng và tạo token (cho API)
     * Sử dụng DATABASE thay vì file
     */
    public String authenticate(String username, String password) throws Exception {
        // Tìm user từ DATABASE
        Optional<UserRecord> userOpt;
        try {
            userOpt = userDAO.findByUsername(username);
        } catch (SQLException e) {
            // Exception khi kết nối DB
            throw new SQLException(e);
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
        System.out.println("🔑 [AuthService] Generated JWT for user: " + username);
        System.out.println("🔑 [AuthService] Token (first 30 chars): " +
            (token.length() > 30 ? token.substring(0, 30) + "..." : token));

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
    public void logout(String sessionId) throws SQLException {
        // Xóa khỏi memory
        sessionManager.invalidateSession(sessionId);

        // Xóa khỏi database
        try {
            userDAO.deleteSession(sessionId);
        } catch (SQLException e) {
            throw new SQLException(e);
        }

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
     * Yêu cầu reset mật khẩu cho CUSTOMER - Tạo mã xác nhận gửi qua email
     * CHỈ TÌM TRONG BẢNG CUSTOMERS (tránh trùng email với Employee/Admin)
     * TẠO MÃ XÁC NHẬN 6 SỐ VÀ GỬI QUA EMAIL
     */
    public String requestPasswordReset(String email) {
        try {
            // CHỈ tìm trong bảng Customers (không tìm Employee/Admin)
            Optional<UserRecord> customerOpt = userDAO.findCustomerByEmail(email);

            if (customerOpt.isEmpty()) {
                LOGGER.warning("Password reset failed: Customer email not found - " + email);
                return null;
            }

            // Tạo mã xác nhận 6 số ngẫu nhiên (VD: 123456)
            String verificationCode = generateVerificationCode();

            // Lưu mã xác nhận vào bộ nhớ tạm với thời gian hết hạn 15 phút
            saveVerificationCode(email, verificationCode, 15);

            LOGGER.info("✓ Verification code created for customer: " + email);

            // Trả về mã xác nhận để gửi qua email
            return verificationCode;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating verification code", e);
            return null;
        }
    }

    /**
     * Xác thực mã và đổi mật khẩu mới
     * @param email Email của customer
     * @param verificationCode Mã xác nhận 6 số
     * @param newPassword Mật khẩu mới
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean resetPasswordWithCode(String email, String verificationCode, String newPassword) {
        try {
            // Kiểm tra mã xác nhận có hợp lệ không
            if (!verifyVerificationCode(email, verificationCode)) {
                LOGGER.warning("Invalid or expired verification code for: " + email);
                return false;
            }

            // Tìm customer theo email
            Optional<UserRecord> customerOpt = userDAO.findCustomerByEmail(email);
            if (customerOpt.isEmpty()) {
                return false;
            }

            UserRecord customer = customerOpt.get();

            // Hash và cập nhật mật khẩu mới
            String hashedPassword = PasswordService.hashPasswordWithSalt(newPassword);
            boolean updated = userDAO.updateCustomerPassword(customer.id, hashedPassword);

            if (updated) {
                // Xóa mã xác nhận sau khi dùng xong
                removeVerificationCode(email);
                LOGGER.info("✓ Password reset successful for customer: " + email);
                return true;
            }

            return false;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error resetting password with code", e);
            return false;
        }
    }

    /**
     * Tạo mã xác nhận 6 số ngẫu nhiên
     */
    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // Số từ 100000 đến 999999
        return String.valueOf(code);
    }

    // ========== BỘ NHỚ TẠM LƯU MÃ XÁC NHẬN ==========
    // Map<email, VerificationData>
    private static final Map<String, VerificationData> verificationCodes = new HashMap<>();

    private static class VerificationData {
        String code;
        long expiryTime; // timestamp khi hết hạn

        VerificationData(String code, long expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }

    /**
     * Lưu mã xác nhận vào bộ nhớ tạm
     */
    private void saveVerificationCode(String email, String code, int expiryMinutes) {
        long expiryTime = System.currentTimeMillis() + (expiryMinutes * 60 * 1000);
        verificationCodes.put(email.toLowerCase(), new VerificationData(code, expiryTime));

        LOGGER.info("Saved verification code for: " + email + " (expires in " + expiryMinutes + " minutes)");
    }

    /**
     * Kiểm tra mã xác nhận có hợp lệ không
     */
    private boolean verifyVerificationCode(String email, String code) {
        VerificationData data = verificationCodes.get(email.toLowerCase());

        if (data == null) {
            LOGGER.warning("No verification code found for: " + email);
            return false;
        }

        // Kiểm tra hết hạn chưa
        if (System.currentTimeMillis() > data.expiryTime) {
            verificationCodes.remove(email.toLowerCase());
            LOGGER.warning("Verification code expired for: " + email);
            return false;
        }

        // Kiểm tra mã có đúng không
        boolean valid = data.code.equals(code);
        if (!valid) {
            LOGGER.warning("Invalid verification code for: " + email);
        }

        return valid;
    }

    /**
     * Xóa mã xác nhận sau khi dùng xong
     */
    private void removeVerificationCode(String email) {
        verificationCodes.remove(email.toLowerCase());
        LOGGER.info("Removed verification code for: " + email);
    }

    /**
     * Reset mật khẩu với token
     * DEPRECATED - Không dùng nữa
     */
    @Deprecated
    public boolean resetPassword(String token, String newPassword) {
        LOGGER.warning("resetPassword(token, password) is deprecated. Use resetPasswordWithCode(email, code, newPassword) instead.");
        return false;
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

    /**
     * Đổi mật khẩu cho user đang đăng nhập
     * Kiểm tra mật khẩu hiện tại và cập nhật mật khẩu mới
     */
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        try {
            // Tìm user từ DATABASE
            Optional<UserRecord> userOpt = userDAO.findByUsername(username);

            if (userOpt.isEmpty()) {
                LOGGER.warning("Change password failed: User not found - " + username);
                return false;
            }

            UserRecord user = userOpt.get();

            // Check active status
            if (!user.active) {
                LOGGER.warning("Change password failed: Account is not active - " + username);
                throw new RuntimeException("Tài khoản đã bị vô hiệu hóa");
            }

            // Verify current password
            if (!PasswordService.verifyPassword(currentPassword, user.password)) {
                LOGGER.warning("Change password failed: Current password incorrect - " + username);
                throw new RuntimeException("Mật khẩu hiện tại không đúng");
            }

            // Hash new password với bcrypt
            String hashedPassword = PasswordService.hashPasswordWithSalt(newPassword);

            // Update password trong DATABASE
            boolean updated = userDAO.updatePassword(user.id, user.role, hashedPassword);

            if (updated) {
                LOGGER.info("Password changed successfully for user: " + username + " [" + user.role + "]");
                return true;
            } else {
                LOGGER.warning("Failed to update password for user: " + username);
                throw new RuntimeException("Cập nhật mật khẩu thất bại");
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error changing password", e);
            throw new RuntimeException("Lỗi hệ thống khi đổi mật khẩu: " + e.getMessage());
        }
    }
}

