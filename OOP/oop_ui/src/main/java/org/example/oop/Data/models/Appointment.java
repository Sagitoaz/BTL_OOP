package org.example.oop.Data.models;

import java.time.LocalDateTime;

/**
 * Lớp Appointment - đại diện cho một lịch hẹn trong hệ thống phòng khám mắt.
 *
 * Ghi chú quan trọng dành cho người duy trì:
 * - Trường startTime/endTime dùng LocalDateTime để biểu diễn thời điểm bắt đầu/kết thúc.
 * - status, appointmentType là enum; khi thay đổi enum cần đồng bộ với file dữ liệu.
 * - createdAt/updatedAt được sử dụng để audit; khi parse từ file cần đảm bảo format ISO-8601.
 *
 * Định dạng lưu file:
 * - toFileFormat() trả về chuỗi phân cách bởi '|' theo thứ tự: id|customerId|doctorId|appointmentType|notes|startTime|endTime|status|createdBy|updatedBy|createdAt|updatedAt
 * - fromFileFormat() giả sử file có đủ 12 phần và sử dụng LocalDateTime.parse cho start/end/created/updated.
 *
 * Lưu ý khi sửa đổi:
 * - Nếu thay đổi thứ tự hoặc thêm trường, hãy cập nhật đồng thời toFileFormat() và fromFileFormat().
 * - Xử lý lỗi parse (ArrayIndexOutOfBoundsException, DateTimeParseException, IllegalArgumentException) khi đọc file thực tế.
 */
public class Appointment {
    private int id;
    private String customerId;
    private String doctorId;
    private AppointmentType appointmentType;
    private String notes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AppointmentStatus status;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor khởi tạo một Appointment.
     * - Thường được gọi khi đọc dữ liệu từ file/DB hoặc khi tạo mới trong UI.
     */
    public Appointment(int id, String customerId, String doctorId, AppointmentType appointmentType,
                       String notes, LocalDateTime startTime, LocalDateTime endTime,
                       AppointmentStatus status, String createdBy, String updatedBy,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.doctorId = doctorId;
        this.appointmentType = appointmentType;
        this.notes = notes;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Convert to file format: id|customerId|doctorId|appointmentType|notes|startTime|endTime|status|createdBy|updatedBy|createdAt|updatedAt
    public String toFileFormat() {
        return String.join("|",
                String.valueOf(id), customerId, doctorId, appointmentType.name(), notes,
                startTime.toString(), endTime.toString(), status.name(),
                createdBy, updatedBy, createdAt.toString(), updatedAt.toString()
        );
    }

    // Parse from file format
    public static Appointment fromFileFormat(String line) {
        String[] parts = line.split("\\|");
        return new Appointment(
                Integer.parseInt(parts[0]), parts[1], parts[2], AppointmentType.valueOf(parts[3]), parts[4],
                LocalDateTime.parse(parts[5]), LocalDateTime.parse(parts[6]), AppointmentStatus.valueOf(parts[7]),
                parts[8], parts[9], LocalDateTime.parse(parts[10]), LocalDateTime.parse(parts[11])
        );
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", customerId='" + customerId + '\'' +
                ", doctorId='" + doctorId + '\'' +
                ", appointmentType=" + appointmentType +
                ", notes='" + notes + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status=" + status +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
