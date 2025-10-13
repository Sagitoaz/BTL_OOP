package org.example.oop.Model.Schedule;

import java.time.LocalDateTime;

/**
 * Lớp Appointment - đại diện cho một lịch hẹn trong hệ thống phòng khám mắt.
 * Theo database mới: customer_id và doctor_id là int, status có thêm giá trị mới
 */
public class Appointment {
    private int id;
    private int customerId; // int ref to Customers.id
    private int doctorId; // int ref to Employees.id (MUST be role='doctor')
    private AppointmentType appointmentType;
    private String notes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor đầy đủ
     */
    public Appointment(int id, int customerId, int doctorId, AppointmentType appointmentType,
                       String notes, LocalDateTime startTime, LocalDateTime endTime,
                       AppointmentStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.doctorId = doctorId;
        this.appointmentType = appointmentType;
        this.notes = notes;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Constructor cho appointment mới
     */
    public Appointment(int id, int customerId, int doctorId, AppointmentType appointmentType,
                       LocalDateTime startTime, LocalDateTime endTime) {
        this(id, customerId, doctorId, appointmentType, null, startTime, endTime,
             AppointmentStatus.SCHEDULED, LocalDateTime.now(), LocalDateTime.now());
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
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

    /**
     * Chuyển đổi Appointment thành chuỗi để lưu vào file
     * Format: id|customerId|doctorId|appointmentType|notes|startTime|endTime|status|createdAt|updatedAt
     */
    public String toFileFormat() {
        return String.join("|",
                String.valueOf(id),
                String.valueOf(customerId),
                String.valueOf(doctorId),
                appointmentType.getValue(),
                notes != null ? notes : "",
                startTime.toString(),
                endTime.toString(),
                status.getValue(),
                createdAt.toString(),
                updatedAt != null ? updatedAt.toString() : ""
        );
    }

    /**
     * Tạo Appointment từ chuỗi trong file
     * Format: id|customerId|doctorId|appointmentType|notes|startTime|endTime|status|createdAt|updatedAt
     */
    public static Appointment fromFileFormat(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 9) {
            throw new IllegalArgumentException("Invalid appointment format: " + line);
        }

        int id = Integer.parseInt(parts[0]);
        int customerId = Integer.parseInt(parts[1]);
        int doctorId = Integer.parseInt(parts[2]);
        AppointmentType type = AppointmentType.fromValue(parts[3]);
        String notes = parts[4].isEmpty() ? null : parts[4];
        LocalDateTime startTime = LocalDateTime.parse(parts[5]);
        LocalDateTime endTime = LocalDateTime.parse(parts[6]);
        AppointmentStatus status = AppointmentStatus.fromValue(parts[7]);
        LocalDateTime createdAt = LocalDateTime.parse(parts[8]);
        LocalDateTime updatedAt = parts.length > 9 && !parts[9].isEmpty()
                ? LocalDateTime.parse(parts[9]) : null;

        return new Appointment(id, customerId, doctorId, type, notes,
                startTime, endTime, status, createdAt, updatedAt);
    }
}
