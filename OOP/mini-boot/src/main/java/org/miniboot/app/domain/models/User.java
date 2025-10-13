package org.miniboot.app.domain.models;

import java.time.LocalDateTime;

/**
 * Interface User - đại diện chung cho tất cả các loại người dùng trong hệ thống.
 * Theo database mới: không còn kế thừa, mỗi loại user là bảng riêng (Admins, Employees, Customers)
 */
public interface User {
    String getId();
    void setId(String id);

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
    String getRole();
}
