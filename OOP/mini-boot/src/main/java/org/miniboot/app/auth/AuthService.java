package org.miniboot.app.auth;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * AuthService: Xử lý logic xác thực người dùng
 * - Kiểm tra thông tin đăng nhập từ 3 bảng: Admins, Employees, Customers
 * - Tạo JWT token cho người dùng hợp lệ
 * - Quản lý phiên đăng nhập và quyền truy cập
 *
 * Đã cập nhật theo database mới:
 * - Đọc từ 3 file riêng biệt thay vì users.txt
 * - Hỗ trợ 3 loại user: ADMIN, EMPLOYEE (doctor/nurse), CUSTOMER
 */
public class AuthService {
    // Singleton để đảm bảo chỉ có một instance AuthService trong ứng dụng
    private static AuthService instance;
    // SessionManager: quản lý session (create/invalidate/get). Là một singleton bên trong.
    private static final SessionManager sessionManager = SessionManager.getInstance();

    // Đường dẫn 3 file dữ liệu mới
    private static final String ADMINS_FILE = "oop_ui/src/main/resources/Data/admins.txt";
    private static final String EMPLOYEES_FILE = "oop_ui/src/main/resources/Data/employees.txt";
    private static final String CUSTOMERS_FILE = "oop_ui/src/main/resources/Data/customers.txt";

    private AuthService() {}

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Đăng nhập với tên đăng nhập và mật khẩu
     * Tìm kiếm trong cả 3 file: admins, employees, customers
     */
    public Optional<String> login(String username, String password) {
        try {
            Optional<UserData> userOpt = findUserByUsername(username);
            if (!userOpt.isPresent()) {
                System.out.println("✗ Login failed: User does not exist - " + username);
                return Optional.empty();
            }

            UserData user = userOpt.get();

            // Check active status
            if (!user.active) {
                System.out.println("✗ Login failed: Account is not active - " + username);
                return Optional.empty();
            }

            // Verify password
            // PasswordService.verifyPassword sẽ tách salt từ stored hash và băm password nhập để so sánh
            if (!PasswordService.verifyPassword(password, user.password)) {
                System.out.println("✗ Login failed: Invalid password - " + username);
                return Optional.empty();
            }

            // Create session for user
            // SessionManager.createSession trả về sessionId (chuỗi) để client lưu (ví dụ cookie hoặc header)
            String sessionId = sessionManager.createSession(user.id, user.username, user.role);
            System.out.println("✓ Login successful: " + username + " (" + user.role + ")");

            return Optional.of(sessionId);

        } catch (Exception e) {
            // Bắt và in lỗi tổng quát. Tránh lộ stack trace chi tiết ra production log.
            System.err.println("✗ Login error: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Xác thực người dùng và tạo token (cho API)
     *
     * Luồng xử lý tương tự login, nhưng trả về JWT token thay vì sessionId.
     * Token được tạo bằng JwtService.generateToken(username).
     * Token nên có expiration; việc validate token dựa vào JwtService.validateTokenAndGetUserId.
     */
    public static String authenticate(String username, String password) throws Exception {
        Optional<UserData> userOpt = findUserByUsername(username);
        if (!userOpt.isPresent()) {
            throw new Exception("User does not exist: " + username);
        }

        UserData user = userOpt.get();

        // Check active status
        if (!user.active) {
            throw new Exception("Account is not active: " + username);
        }

        // Verify password
        if (!PasswordService.verifyPassword(password, user.password)) {
            throw new Exception("Invalid password");
        }

        // Generate JWT token
        // JwtService.generateToken nên đóng gói thông tin cần thiết (ví dụ: username, exp)
        String token = JwtService.generateToken(username);

        System.out.println("✓ Authentication successful: " + username + " (" + user.role + ")");

        return token;
    }

    /**
     * Xác thực token từ header Authorization
     *
     * Format header mong đợi: "Authorization: Bearer <token>"
     * - Kiểm tra null/format
     * - Gọi JwtService để validate và lấy userId (hoặc username tuỳ implement)
     *
     * Trả về userId nếu token hợp lệ, ngược lại ném Exception.
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
     *
     * Gọi SessionManager.invalidateSession để remove session khỏi store.
     */
    public static void logout(String sessionId) {
        sessionManager.invalidateSession(sessionId);
    }

    /**
     * Lấy thông tin phiên đăng nhập hiện tại theo sessionId
     * Trả về Optional.empty nếu session không tồn tại hoặc đã hết hạn.
     */
    public static Optional<SessionManager.Session> getCurrentSession(String sessionId) {
        return sessionManager.getSession(sessionId);
    }

    /**
     * Kiểm tra quyền truy cập cho session given
     *
     * Quy trình:
     * 1. Lấy session từ SessionManager
     * 2. Lấy role trong session
     * 3. Kiểm tra quyền bằng RolePermissions.hasPermission
     */
    public static boolean hasPermission(String sessionId, RolePermissions.Permission permission) {
        Optional<SessionManager.Session> sessionOpt = sessionManager.getSession(sessionId);
        if (!sessionOpt.isPresent()) return false;

        String role = sessionOpt.get().getRole();
        return RolePermissions.hasPermission(role, permission);
    }

    /**
     * Tìm người dùng theo username từ cả 3 file
     * Thứ tự tìm kiếm: Admins -> Employees -> Customers
     */
    private static Optional<UserData> findUserByUsername(String username) {
        // Tìm trong Admins
        Optional<UserData> admin = findInAdmins(username);
        if (admin.isPresent()) return admin;

        // Tìm trong Employees
        Optional<UserData> employee = findInEmployees(username);
        if (employee.isPresent()) return employee;

        // Tìm trong Customers
        Optional<UserData> customer = findInCustomers(username);
        if (customer.isPresent()) return customer;

        return Optional.empty();
    }

    /**
     * Tìm trong file admins.txt
     * Format: id|username|password|email|is_active
     */
    private static Optional<UserData> findInAdmins(String username) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(ADMINS_FILE));
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 5 && parts[1].equals(username)) {
                    return Optional.of(new UserData(
                        parts[0],                           // id
                        parts[1],                           // username
                        parts[2],                           // password (hashed)
                        "ADMIN",                            // role
                        parts[3],                           // email
                        "Admin User",                       // fullName
                        "",                                 // phone
                        Boolean.parseBoolean(parts[4])      // active
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading admins file: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Tìm trong file employees.txt
     * Format: id|username|password|firstname|lastname|avatar|role|license_no|email|phone|is_active|created_at
     */
    private static Optional<UserData> findInEmployees(String username) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(EMPLOYEES_FILE));
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 12 && parts[1].equals(username)) {
                    String employeeRole = parts[6]; // "doctor" hoặc "nurse"
                    String roleForSession = "EMPLOYEE"; // Hoặc có thể dùng "DOCTOR"/"NURSE" nếu cần phân biệt

                    return Optional.of(new UserData(
                        parts[0],                           // id
                        parts[1],                           // username
                        parts[2],                           // password (hashed)
                        roleForSession,                     // role
                        parts[8],                           // email
                        parts[3] + " " + parts[4],          // fullName (firstname + lastname)
                        parts[9],                           // phone
                        Boolean.parseBoolean(parts[10])     // active
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading employees file: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Tìm trong file customers.txt
     * Format: id|username|password|firstname|lastname|phone|email|dob|gender|address|note|created_at
     */
    private static Optional<UserData> findInCustomers(String username) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(CUSTOMERS_FILE));
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 12 && parts[1].equals(username)) {
                    return Optional.of(new UserData(
                        parts[0],                           // id
                        parts[1],                           // username
                        parts[2],                           // password (hashed)
                        "CUSTOMER",                         // role (hoặc "PATIENT")
                        parts[6],                           // email
                        parts[3] + " " + parts[4],          // fullName (firstname + lastname)
                        parts[5],                           // phone
                        true                                // active (customers không có trường is_active, mặc định true)
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading customers file: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Lớp dữ liệu người dùng đơn giản dùng nội bộ trong AuthService
     * Chứa các trường cần thiết để xác thực và tạo session.
     */
    private static class UserData {
        String id, username, password, role, email, fullName, phone;
        boolean active;

        UserData(String id, String username, String password, String role,
                 String email, String fullName, String phone, boolean active) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.role = role;
            this.email = email;
            this.fullName = fullName;
            this.phone = phone;
            this.active = active;
        }
    }
}
