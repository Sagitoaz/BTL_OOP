package org.example.oop.Data.models;

import java.time.LocalDateTime;

/**
 * Lớp Patient - đại diện cho người bệnh (patient) trong hệ thống.
 *
 * Ghi chú cho người duy trì:
 * - Kế thừa từ User và mặc định role = PATIENT.
 * - Các phương thức đặc thù (ví dụ bookAppointment) là nơi triển khai nghiệp vụ liên quan tới bệnh nhân.
 * - Nếu cần trường thông tin cá nhân bổ sung (ngày sinh, giới tính, địa chỉ), hãy mở rộng model
 *   và đồng bộ với nơi lưu/đọc dữ liệu (repository và toFileFormat/fromFileFormat nếu có).
 */
public class Patient extends User {
    public Patient(String id, String username, String password,
                   String email, String fullName, String phone) {
        super(id, username, password, UserRole.PATIENT, email, fullName, phone);
    }

    // Phương thức đặt lịch cho bệnh nhân - triển khai nghiệp vụ ở đây
    public void bookAppointment() { /* Implementation */ }
}
