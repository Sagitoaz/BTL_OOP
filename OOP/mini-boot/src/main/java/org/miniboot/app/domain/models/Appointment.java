package org.miniboot.app.domain.models;

import java.time.LocalDateTime;

/**
 * Lớp Appointment - đại diện cho một lịch hẹn trong hệ thống phòng khám mắt.
 * Theo database mới: customer_id và doctor_id là int, dùng ENUM cho type và status
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
     * Constructor mặc định (cần cho Jackson/Gson)
     */
    public Appointment() {
        this.status = AppointmentStatus.SCHEDULED;
    }

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
        this.status = status != null ? status : AppointmentStatus.SCHEDULED;
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
        // Auto-set nếu null
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        // Auto-set nếu null
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", doctorId=" + doctorId +
                ", appointmentType=" + appointmentType +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status=" + status +
                '}';
    }
}
