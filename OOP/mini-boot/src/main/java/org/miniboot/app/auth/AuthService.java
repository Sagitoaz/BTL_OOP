package org.miniboot.app.auth;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * AuthService: Xử lý logic xác thực người dùng
 * - Kiểm tra thông tin đăng nhập
 * - Tạo JWT token cho người dùng hợp lệ
 * - Quản lý phiên đăng nhập và quyền truy cập
 *
 * Ghi chú quan trọng cho người đọc:
 * - Dữ liệu người dùng được lưu trong file text (USER_FILE) theo định dạng pipe-separated.
 *   Khi bảo trì/triển khai thực tế nên chuyển sang DB an toàn thay vì file text.
 * - Mật khẩu trong file được lưu ở dạng đã băm (salt:hash). Việc xác thực sử dụng PasswordService.
 * - Session được quản lý tạm thời qua SessionManager (in-memory). Với môi trường nhiều instance cần dùng store chia sẻ.
 */
public class AuthService {
    // Singleton để đảm bảo chỉ có một instance AuthService trong ứng dụng
    private static AuthService instance;
    // SessionManager: quản lý session (create/invalidate/get). Là một singleton bên trong.
    private static final SessionManager sessionManager = SessionManager.getInstance();
    // Đường dẫn file chứa dữ liệu người dùng (format: id|username|password|role|email|fullName|phone|createdAt|active)
    private static final String USER_FILE = "oop_ui/src/main/resources/Data/users.txt";

    private AuthService() {}

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Đăng nhập với tên đăng nhập và mật khẩu
     *
     * Luồng xử lý:
     * 1. Tìm user theo username (đọc từ file)
     * 2. Kiểm tra tài khoản active
     * 3. Xác thực mật khẩu bằng PasswordService.verifyPassword
     * 4. Nếu hợp lệ -> tạo session thông qua SessionManager và trả về sessionId
     *
     * Lưu ý bảo mật/triển khai:
     * - Thông báo lỗi ở đây in ra console cho mục đích debug; trong production không nên in chi tiết (ví dụ: 'username không tồn tại') để tránh lộ thông tin.
     * - Session hiện tại là in-memory (SessionManager). Nếu cần scale, đổi sang Redis hoặc store phân tán.
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
     * Tìm người dùng theo tên đăng nhập bằng cách đọc file USER_FILE.
     * File được đọc từng dòng, mỗi dòng tách theo dấu '|'.
     *
     * Trả về Optional<UserData> nếu tìm thấy, Optional.empty nếu không tìm thấy hoặc có lỗi IO.
     *
     * Ghi chú: hiện tại hàm này đọc toàn bộ file mỗi lần gọi -> không hiệu quả cho hệ thống lớn.
     * Nên cache hoặc dùng DB cho dữ liệu production.
     */
    private static Optional<UserData> findUserByUsername(String username) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(USER_FILE));
            for (String line : lines) {
                String[] parts = line.split("\\|");
                // Kiểm tra độ dài mảng và username khớp
                if (parts.length >= 9 && parts[1].equals(username)) {
                    // parts mapping: id|username|password|role|email|fullName|phone|createdAt|active
                    return Optional.of(new UserData(
                        parts[0], parts[1], parts[2], parts[3],
                        parts[4], parts[5], parts[6],
                        Boolean.parseBoolean(parts[8])
                    ));
                }
            }
        } catch (IOException e) {
            // Ghi log lỗi đọc file; không ném tiếp để caller có thể xử lý an toàn
            System.err.println("Error reading user file: " + e.getMessage());
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
