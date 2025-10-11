package org.example.oop.Data.models;

/**
 * AppointmentStatus - trạng thái của một lịch hẹn trong hệ thống.
 *
 * Ghi chú cho người duy trì:
 * - Các giá trị biểu diễn vòng đời của lịch hẹn: đã lên lịch, xác nhận, check-in, đang tiến hành, hoàn thành, hủy, vắng mặt.
 * - Trạng thái được dùng để điều phối luồng xử lý trong UI và backend (ví dụ, chỉ cho phép check-in khi status = SCHEDULED hoặc CONFIRMED).
 * - Nếu bổ sung trạng thái mới, cập nhật cả chỗ switch/if liên quan và nơi lưu/đọc file để tránh lỗi parse.
 * - Không đổi tên các giá trị hiện có nếu dữ liệu đã được lưu (sẽ phá vỡ việc đọc dữ liệu cũ).
 */
public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    CHECKED_IN,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
