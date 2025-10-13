package org.example.oop.Control;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * AuthServiceWrapper
 *
 * Mục đích:
 * - Cung cấp lớp bọc (wrapper) để sử dụng các chức năng xác thực từ module mini-boot
 *   (org.miniboot.app.auth.AuthService và PasswordService) mà không gặp ràng buộc module
 *   hoặc phụ thuộc trực tiếp tại thời điểm biên dịch.
 * - Sử dụng reflection để gọi các phương thức tĩnh và instance của AuthService/PasswordService.
 *
 * Ghi chú quan trọng cho người bảo trì:
 * - Reflection cho phép gọi method tại runtime nhưng làm mất tính an toàn kiểu (type-safety),
 *   và dễ gây lỗi nếu chữ ký method thay đổi. Khi nâng cấp mini-boot, kiểm tra lại tên class/method.
 * - Mọi lỗi reflection được catch và log qua System.err; wrapper trả về giá trị an toàn (Optional.empty
 *   hoặc false) để tránh ném ngoại lệ lên UI.
 * - Tránh in hoặc log mật khẩu; các phương thức hash/verify nên được gọi thay vì in mật khẩu.
 */
public class AuthServiceWrapper {
    // Instance của AuthService được giữ dưới dạng Object vì dùng reflection
    private static Object authServiceInstance;

    static {
        try {
            // Nạp class AuthService từ mini-boot tại runtime
            Class<?> authServiceClass = Class.forName("org.miniboot.app.auth.AuthService");
            // Gọi phương thức tĩnh getInstance() để lấy singleton instance (nếu có)
            Method getInstanceMethod = authServiceClass.getMethod("getInstance");
            authServiceInstance = getInstanceMethod.invoke(null);
        } catch (Exception e) {
            // Ghi log lỗi khởi tạo; không ném tiếp để tránh crash app
            System.err.println("Failed to initialize AuthService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Wrapper cho AuthService.login(username, password)
     * Trả về Optional chứa sessionId nếu đăng nhập thành công.
     *
     * Lưu ý:
     * - Dùng Optional để xử lý an toàn khi reflection gặp lỗi.
     * - Không in mật khẩu vào log.
     */
    public static Optional<String> login(String username, String password) {
        try {
            Method loginMethod = authServiceInstance.getClass().getMethod("login", String.class, String.class);
            Object result = loginMethod.invoke(authServiceInstance, username, password);
            return (Optional<String>) result;
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Wrapper cho AuthService.logout(sessionId)
     * Gọi phương thức tĩnh logout trên class AuthService.
     */
    public static void logout(String sessionId) {
        try {
            Class<?> authServiceClass = Class.forName("org.miniboot.app.auth.AuthService");
            Method logoutMethod = authServiceClass.getMethod("logout", String.class);
            logoutMethod.invoke(null, sessionId);
        } catch (Exception e) {
            System.err.println("Logout failed: " + e.getMessage());
        }
    }

    /**
     * Wrapper cho AuthService.getCurrentSession(sessionId)
     * Trả về Optional chứa session object (kiểu tạm Object vì reflection).
     */
    public static Optional<Object> getCurrentSession(String sessionId) {
        try {
            Class<?> authServiceClass = Class.forName("org.miniboot.app.auth.AuthService");
            Method getCurrentSessionMethod = authServiceClass.getMethod("getCurrentSession", String.class);
            Object result = getCurrentSessionMethod.invoke(null, sessionId);
            return (Optional<Object>) result;
        } catch (Exception e) {
            System.err.println("Get current session failed: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Wrapper cho PasswordService.hashPasswordWithSalt(password)
     * Trả về chuỗi dạng "salt:hash".
     *
     * Ghi chú bảo mật:
     * - Không bao giờ ghi lại giá trị mật khẩu gốc. Nếu hashing thất bại wrapper trả về
     *   plaintext như fallback (không an toàn) — production nên xử lý khác.
     */
    public static String hashPasswordWithSalt(String password) {
        try {
            Class<?> passwordServiceClass = Class.forName("org.miniboot.app.auth.PasswordService");
            Method hashMethod = passwordServiceClass.getMethod("hashPasswordWithSalt", String.class);
            return (String) hashMethod.invoke(null, password);
        } catch (Exception e) {
            System.err.println("Password hashing failed: " + e.getMessage());
            return password; // fallback to plain text (not recommended for production)
        }
    }

    /**
     * Wrapper cho PasswordService.verifyPassword(password, hashedPassword)
     * Trả về true nếu mật khẩu hợp lệ so với hash đã lưu.
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            Class<?> passwordServiceClass = Class.forName("org.miniboot.app.auth.PasswordService");
            Method verifyMethod = passwordServiceClass.getMethod("verifyPassword", String.class, String.class);
            return (Boolean) verifyMethod.invoke(null, password, hashedPassword);
        } catch (Exception e) {
            System.err.println("Password verification failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Wrapper cho PasswordService.isPasswordStrong(password)
     * Nếu reflection thất bại thì có fallback kiểm tra sơ bộ.
     */
    public static boolean isPasswordStrong(String password) {
        try {
            Class<?> passwordServiceClass = Class.forName("org.miniboot.app.auth.PasswordService");
            Method isStrongMethod = passwordServiceClass.getMethod("isPasswordStrong", String.class);
            return (Boolean) isStrongMethod.invoke(null, password);
        } catch (Exception e) {
            System.err.println("Password strength check failed: " + e.getMessage());
            // Fallback password strength check
            return password != null && password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*")
                && password.matches(".*[!@#$%^&*()].*");
        }
    }
}
