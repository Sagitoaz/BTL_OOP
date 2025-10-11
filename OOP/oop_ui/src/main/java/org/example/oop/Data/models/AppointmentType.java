package org.example.oop.Data.models;

/**
 * AppointmentType - loại hình lịch hẹn trong hệ thống.
 *
 * Ghi chú cho người duy trì:
 * - Các giá trị biểu diễn mục đích lịch: VISIT (khám thường), TEST (xét nghiệm), SURGERY (phẫu thuật).
 * - Nếu thêm loại lịch mới, cập nhật nơi xử lý lịch, UI và định dạng file nếu cần lưu loại này.
 * - Tránh đổi tên các giá trị hiện có để không làm hỏng dữ liệu đã lưu trước đó.
 */
public enum AppointmentType {
    VISIT,
    TEST,
    SURGERY
}
