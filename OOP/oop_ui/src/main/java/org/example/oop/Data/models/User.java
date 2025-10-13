package org.example.oop.Data.models;

import java.time.LocalDateTime;

/**
 * Interface User - đại diện chung cho tất cả các loại người dùng trong hệ thống.
 * Theo database mới: không còn kế thừa, mỗi loại user là bảng riêng (Admins, Employees, Customers)
 * Interface này giúp xử lý polymorphism khi cần
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
     * Trả về role của user để phân quyền
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
        return "";
    }

    /**
     * Thiết lập số điện thoại.
     * Default implementation (không làm gì cho Admin)
     */
    default void setPhone(String phone) {
        // Default: không làm gì
    }
}