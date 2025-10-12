package org.example.oop.Data.models;

import java.time.LocalDateTime;

/**
 * Lớp Staff - đại diện cho nhân viên hỗ trợ trong hệ thống (ví dụ lễ tân, y tá).
 *
 * Ghi chú cho người duy trì:
 * - Kế thừa từ User và mặc định role = STAFF.
 * - Các phương thức đặc thù như manageAppointments là nơi triển khai nghiệp vụ liên quan đến lịch
 *   và tác vụ của nhân viên.
 * - Nếu cần thêm thông tin (ví dụ department, shift), mở rộng model và cập nhật nơi lưu/đọc dữ liệu.
 */
public class Staff extends User {
    public Staff(int id, String username, String password,
                 String email, String fullName, String phone) {
        super(id, username, password, UserRole.STAFF, email, fullName, phone);
    }

    // Phương thức quản lý lịch/phiên làm việc của nhân viên - triển khai nghiệp vụ ở đây
    public void manageAppointments() { /* Implementation */ }
}
