package org.miniboot.app.dao;

import org.miniboot.app.config.DatabaseConfig;
import org.miniboot.app.domain.models.CustomerAndPrescription.Customer;
import org.miniboot.app.domain.models.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UserDAO - Data Access Object cho User operations
 *
 * Quản lý việc đọc/ghi thông tin user vào PostgreSQL database
 * Hỗ trợ 3 loại user: Admin, Employee, Customer
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    private final DatabaseConfig dbConfig;

    public UserDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    /**
     * Tìm user theo username
     * Tìm kiếm trong 3 bảng: Admins, Employees, Customers
     */
    public Optional<UserRecord> findByUsername(String username) {
        // Tìm trong Admins
        Optional<UserRecord> admin = findAdminByUsername(username);
        if (admin.isPresent()) return admin;

        // Tìm trong Employees
        Optional<UserRecord> employee = findEmployeeByUsername(username);
        if (employee.isPresent()) return employee;

        // Tìm trong Customers
        Optional<UserRecord> customer = findCustomerByUsername(username);
        return customer;
    }

    /**
     * Tìm admin theo username
     */
    private Optional<UserRecord> findAdminByUsername(String username) {
        String sql = "SELECT id, username, password, email, is_active FROM Admins WHERE username = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserRecord record = new UserRecord();
                record.id = rs.getInt("id");
                record.username = rs.getString("username");
                record.password = rs.getString("password");
                record.email = rs.getString("email");
                record.role = "ADMIN";
                record.fullName = "Admin User";
                record.active = rs.getBoolean("is_active");
                return Optional.of(record);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding admin by username: " + username, e);
        }

        return Optional.empty();
    }

    /**
     * Tìm employee theo username
     */
    private Optional<UserRecord> findEmployeeByUsername(String username) {
        String sql = "SELECT id, username, password, firstname, lastname, role, email, phone, is_active " +
                    "FROM Employees WHERE username = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserRecord record = new UserRecord();
                record.id = rs.getInt("id");
                record.username = rs.getString("username");
                record.password = rs.getString("password");
                record.email = rs.getString("email");
                record.role = "EMPLOYEE"; // hoặc rs.getString("role").toUpperCase()
                record.fullName = rs.getString("firstname") + " " + rs.getString("lastname");
                record.phone = rs.getString("phone");
                record.active = rs.getBoolean("is_active");
                return Optional.of(record);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding employee by username: " + username, e);
        }

        return Optional.empty();
    }

    /**
     * Tìm customer theo username
     */
    private Optional<UserRecord> findCustomerByUsername(String username) {
        String sql = "SELECT id, username, password, firstname, lastname, email, phone " +
                    "FROM Customers WHERE username = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserRecord record = new UserRecord();
                record.id = rs.getInt("id");
                record.username = rs.getString("username");
                record.password = rs.getString("password");
                record.email = rs.getString("email");
                record.role = "CUSTOMER";
                record.fullName = rs.getString("firstname") + " " + rs.getString("lastname");
                record.phone = rs.getString("phone");
                record.active = true; // Customers không có is_active, mặc định true
                return Optional.of(record);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding customer by username: " + username, e);
        }

        return Optional.empty();
    }

    /**
     * Tìm user theo email
     */
    public Optional<UserRecord> findByEmail(String email) {
        // Tìm trong Customers trước (vì thường là customer reset password)
        String sql = "SELECT id, username, firstname, lastname, email, phone " +
                    "FROM Customers WHERE email = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserRecord record = new UserRecord();
                record.id = rs.getInt("id");
                record.username = rs.getString("username");
                record.email = email;
                record.role = "CUSTOMER";
                record.fullName = rs.getString("firstname") + " " + rs.getString("lastname");
                record.phone = rs.getString("phone");
                record.active = true;
                return Optional.of(record);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by email: " + email, e);
        }

        return Optional.empty();
    }

    /**
     * Lưu customer mới vào database với đầy đủ thông tin
     */
    public boolean saveCustomer(String username, String hashedPassword, String firstname,
                                String lastname, String phone, String email,
                                String address, String dob, String gender) {
        String sql = "INSERT INTO Customers (username, password, firstname, lastname, phone, email, address, dob, gender, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?::date, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, firstname);
            stmt.setString(4, lastname);
            stmt.setString(5, phone != null && !phone.isEmpty() ? phone : null);
            stmt.setString(6, email);
            stmt.setString(7, address != null && !address.isEmpty() ? address : null);

            // Xử lý date of birth - chuyển từ dd/MM/yyyy sang yyyy-MM-dd
            if (dob != null && !dob.isEmpty()) {
                try {
                    // Parse dd/MM/yyyy
                    String[] parts = dob.split("/");
                    if (parts.length == 3) {
                        String dobFormatted = parts[2] + "-" + parts[1] + "-" + parts[0];
                        stmt.setString(8, dobFormatted);
                    } else {
                        stmt.setString(8, null);
                    }
                } catch (Exception e) {
                    LOGGER.warning("Invalid date format: " + dob);
                    stmt.setString(8, null);
                }
            } else {
                stmt.setString(8, null);
            }

            stmt.setString(9, gender);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                LOGGER.info("✓ Customer saved to database: " + username);
                return true;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving customer: " + username, e);
        }

        return false;
    }

    /**
     * Cập nhật mật khẩu cho user
     */
    public boolean updatePassword(int userId, String userType, String newHashedPassword) {
        String sql;

        switch (userType.toUpperCase()) {
            case "ADMIN":
                sql = "UPDATE Admins SET password = ? WHERE id = ?";
                break;
            case "EMPLOYEE":
                sql = "UPDATE Employees SET password = ? WHERE id = ?";
                break;
            case "CUSTOMER":
                sql = "UPDATE Customers SET password = ? WHERE id = ?";
                break;
            default:
                LOGGER.warning("Unknown user type: " + userType);
                return false;
        }

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newHashedPassword);
            stmt.setInt(2, userId);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                LOGGER.info("✓ Password updated for user ID: " + userId);
                return true;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating password for user ID: " + userId, e);
        }

        return false;
    }

    /**
     * Lưu token reset mật khẩu
     */
    public boolean savePasswordResetToken(String token, int userId, String userType, int expiryMinutes) {
        String sql = "INSERT INTO Password_Reset_Tokens (token, user_id, user_type, expires_at) " +
                    "VALUES (?, ?, ?, DATEADD(MINUTE, ?, CURRENT_TIMESTAMP))";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            stmt.setInt(2, userId);
            stmt.setString(3, userType.toLowerCase());
            stmt.setInt(4, expiryMinutes);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                LOGGER.info("✓ Reset token saved: " + token);
                return true;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving reset token", e);
        }

        return false;
    }

    /**
     * Validate reset token và lấy thông tin user
     */
    public Optional<UserRecord> validateResetToken(String token) {
        String sql = "SELECT user_id, user_type FROM Password_Reset_Tokens " +
                    "WHERE token = ? AND used = 0 AND expires_at > CURRENT_TIMESTAMP";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String userType = rs.getString("user_type");

                // Tạo UserRecord đơn giản với info cần thiết
                UserRecord record = new UserRecord();
                record.id = userId;
                record.role = userType.toUpperCase();

                return Optional.of(record);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error validating reset token", e);
        }

        return Optional.empty();
    }

    /**
     * Đánh dấu token đã được sử dụng
     */
    public boolean markTokenAsUsed(String token) {
        String sql = "UPDATE Password_Reset_Tokens SET used = 1 WHERE token = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            int rows = stmt.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error marking token as used", e);
        }

        return false;
    }

    /**
     * Lưu session vào database
     */
    public boolean saveSession(String sessionId, int userId, String userType, int expiryHours) {
        String sql = "INSERT INTO User_Sessions (session_id, user_id, user_type, expires_at, last_activity) " +
                    "VALUES (?, ?, ?, DATEADD(HOUR, ?, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP)";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sessionId);
            stmt.setInt(2, userId);
            stmt.setString(3, userType.toLowerCase());
            stmt.setInt(4, expiryHours);

            int rows = stmt.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving session", e);
        }

        return false;
    }

    /**
     * Kiểm tra session có hợp lệ không
     */
    public boolean validateSession(String sessionId) {
        String sql = "SELECT session_id FROM User_Sessions " +
                    "WHERE session_id = ? AND expires_at > CURRENT_TIMESTAMP";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sessionId);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error validating session", e);
        }

        return false;
    }

    /**
     * Xóa session (logout)
     */
    public boolean deleteSession(String sessionId) {
        String sql = "DELETE FROM User_Sessions WHERE session_id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sessionId);
            int rows = stmt.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting session", e);
        }

        return false;
    }

    /**
     * UserRecord - Simple DTO để truyền dữ liệu user
     */
    public static class UserRecord {
        public int id;
        public String username;
        public String password; // hashed
        public String role;
        public String email;
        public String fullName;
        public String phone;
        public boolean active;
    }
}
