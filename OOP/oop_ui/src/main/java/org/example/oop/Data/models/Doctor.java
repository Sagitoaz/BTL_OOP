package org.example.oop.Data.models;

import java.time.LocalDateTime;

/**
 * Lớp Doctor - đại diện cho bác sĩ trong hệ thống phòng khám.
 *
 * Ghi chú cho người duy trì:
 * - Kế thừa từ User và mặc định role = DOCTOR.
 * - Các trường specialization và licenseNumber lưu thông tin chuyên môn và giấy phép hành nghề.
 * - Nếu cần thêm thông tin như lịch làm việc, phòng khám phụ trách, hãy mở rộng model và
 *   cập nhật nơi lưu/đọc dữ liệu (repository và toFileFormat/fromFileFormat nếu sử dụng).
 * - Các phương thức đặc thù của bác sĩ (ví dụ viewAppointments) là nơi triển khai nghiệp vụ liên quan tới lịch khám.
 */
public class Doctor extends User {
    private String specialization;
    private String licenseNumber;

    public Doctor(int id, String username, String password,
                  String email, String fullName, String phone) {
        super(id, username, password, UserRole.DOCTOR, email, fullName, phone);
    }

    // Phương thức xem lịch hẹn của bác sĩ - triển khai nghiệp vụ ở đây
    public void viewAppointments() { /* Implementation */ }

    // Getter/Setter cho trường chuyên môn và giấy phép (nếu cần dùng)
    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
}