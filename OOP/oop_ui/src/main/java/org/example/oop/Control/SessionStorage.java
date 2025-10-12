package org.example.oop.Control;

import java.util.Optional;

/**
 * SessionStorage - Quản lý session cho JavaFX UI
 *
 * Mục đích:
 * - Lưu trữ tạm thời ID của phiên hiện tại (currentSessionId) để các controller trong UI
 *   có thể truy xuất thông tin người dùng đang đăng nhập.
 * - Sử dụng AuthServiceWrapper để truy vấn session thực tế, tránh tương tác trực tiếp
 *   với module/service bên ngoài trong môi trường JavaFX (giải quyết vấn đề module system).
 */
public class SessionStorage {
    // Lưu ID phiên hiện tại. Giá trị null nghĩa là chưa có phiên đăng nhập.
    // Đây là biến static nhằm giữ trạng thái toàn cục cho toàn bộ UI.
    private static String currentSessionId;

    /**
     * Thiết lập ID phiên hiện tại.
     *
     * Ghi chú quan trọng:
     * - Hàm này nên được gọi khi đăng nhập thành công và trả về sessionId từ AuthServiceWrapper.
     * - Không có đồng bộ hóa ở đây; giả sử các thao tác gọi từ JavaFX Application Thread.
     * - Nếu cần thread-safety, hãy thêm synchronized hoặc chuyển logic quản lý session sang một lớp
     *   chuyên biệt có khả năng xử lý đồng bộ.
     */
    public static void setCurrentSessionId(String sessionId) {
        currentSessionId = sessionId;
    }

    /**
     * Lấy ID phiên hiện tại (có thể là null nếu chưa đăng nhập).
     */
    public static String getCurrentSessionId() {
        return currentSessionId;
    }

    /**
     * Trả về Optional chứa đối tượng session thực tế từ AuthServiceWrapper.
     *
     * Hành vi:
     * - Nếu currentSessionId là null thì trả về Optional.empty().
     * - Nếu AuthServiceWrapper không tìm thấy session hoặc có lỗi, AuthServiceWrapper
     *   sẽ quyết định trả Optional.empty() hoặc Optional.of(sessionObject).
     *
     * Mục đích dùng Optional:
     * - Tránh trả null trực tiếp và ép buộc caller phải kiểm tra tồn tại trước khi sử dụng.
     */
    public static Optional<Object> getCurrentSession() {
        if (currentSessionId == null) {
            return Optional.empty();
        }
        return AuthServiceWrapper.getCurrentSession(currentSessionId);
    }

    /**
     * Kiểm tra trạng thái đã đăng nhập hay chưa.
     *
     * Logic:
     * - Phải có currentSessionId khác null và getCurrentSession() trả về Optional có giá trị.
     */
    public static boolean isLoggedIn() {
        return currentSessionId != null && getCurrentSession().isPresent();
    }

    /**
     * Đăng xuất:
     * - Gọi AuthServiceWrapper.logout để xóa session phía dịch vụ nếu có.
     * - Thiết lập lại currentSessionId về null để đánh dấu trạng thái chưa đăng nhập.
     */
    public static void logout() {
        if (currentSessionId != null) {
            AuthServiceWrapper.logout(currentSessionId);
            currentSessionId = null;
        }
    }

    /**
     * Lấy username từ đối tượng session hiện tại bằng reflection.
     *
     * Lưu ý:
     * - Đối tượng session ở đây được giữ dưới dạng Object vì ràng buộc module hoặc phụ thuộc.
     * - Dùng reflection để gọi getUsername() nếu tồn tại; nếu xảy ra lỗi thì log ra stderr
     *   và trả về null.
     * - Caller nên kiểm tra null để tránh NullPointerException.
     */
    public static String getCurrentUsername() {
        Optional<Object> session = getCurrentSession();
        if (session.isPresent()) {
            try {
                Object sessionObj = session.get();
                return (String) sessionObj.getClass().getMethod("getUsername").invoke(sessionObj);
            } catch (Exception e) {
                // Giữ thông báo lỗi bằng tiếng Anh để tránh vấn đề mã hóa đầu ra trên terminal
                System.err.println("Failed to get username: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Lấy role của người dùng từ đối tượng session hiện tại bằng reflection.
     *
     * Tương tự như getCurrentUsername(), nếu lỗi xảy ra thì log và trả về null.
     */
    public static String getCurrentUserRole() {
        Optional<Object> session = getCurrentSession();
        if (session.isPresent()) {
            try {
                Object sessionObj = session.get();
                return (String) sessionObj.getClass().getMethod("getRole").invoke(sessionObj);
            } catch (Exception e) {
                // Giữ thông báo lỗi bằng tiếng Anh để tránh vấn đề mã hóa đầu ra trên terminal
                System.err.println("Failed to get user role: " + e.getMessage());
            }
        }
        return null;
    }
}
