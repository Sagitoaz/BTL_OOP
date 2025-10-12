package org.example.oop.Data.models;

/**
 * Enum UserRole - xác định các vai trò người dùng trong hệ thống.
 *
 * Ghi chú cho người duy trì:
 * - Các giá trị role dùng để phân quyền và điều khiển truy cập trong ứng dụng.
 * - displayName dùng để hiển thị thân thiện trên giao diện; không dùng displayName cho logic phân quyền.
 * - Nếu thêm role mới, kiểm tra các chỗ switch/permission để cập nhật tương ứng.
 */
public enum UserRole {
    // Vai trò quản trị viên: quyền cao nhất
    ADMIN("Administrator"),

    // Vai trò bác sĩ: truy cập hồ sơ bệnh nhân, lịch, chẩn đoán
    DOCTOR("Doctor"),

    // Vai trò nhân viên: lễ tân, y tá, thao tác nghiệp vụ hàng ngày
    STAFF("Staff"),

    // Vai trò bệnh nhân: truy cập hồ sơ cá nhân, đặt lịch
    PATIENT("Patient");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Lấy tên hiển thị thân thiện của vai trò (dùng cho UI).
     */
    public String getDisplayName() {
        return displayName;
    }
}