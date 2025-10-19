package org.miniboot.app.domain.models;

/**
 * Interface User - đại diện chung cho tất cả các loại người dùng trong hệ thống.
 * Theo database mới: không còn kế thừa, mỗi loại user là bảng riêng (Admins, Employees, Customers)
 *
 * Đã cập nhật để tương thích với OOP_UI:
 * - ID sử dụng kiểu int thay vì String
 * - Thêm default methods cho getFullName() và setFullName()
 * - Thêm default method cho getPhone() và setPhone()
 * - getUserRole() trả về UserRole enum để tương thích với OOP UI
 */
public interface User {
    int getId();
    void setId(int id);

    String getUsername();
    void setUsername(String username);

    String getPassword();
    void setPassword(String password);

    String getEmail();
    void setEmail(String email);

    boolean isActive();
    void setActive(boolean active);

    /**
     * Trả về UserRole enum để tương thích với OOP UI
     */
    UserRole getUserRole();

    /**
     * Trả về tên đầy đủ của user.
     * Default implementation cho Admin (không có firstname/lastname)
     */
    default String getFullName() {
        return getUsername();
    }

    /**
     * Thiết lập tên đầy đủ.
     * Default implementation (không làm gì cho Admin)
     */
    default void setFullName(String fullName) {
        // Default: không làm gì
    }

    /**
     * Trả về số điện thoại.
     * Default implementation cho Admin (không có phone)
     */
    default String getPhone() {
        return null;
    }

    /**
     * Thiết lập số điện thoại.
     * Default implementation (không làm gì cho Admin)
     */
    default void setPhone(String phone) {
        // Default: không làm gì
    }
}
