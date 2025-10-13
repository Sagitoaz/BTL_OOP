package org.miniboot.app.domain.models;

import java.time.LocalDateTime;

/**
 * Appointment model - lịch hẹn
 * Theo database mới: customer_id và doctor_id là int, thêm nhiều trường mới
 */
public class Appointment {
    private int id;
    private int customerId; // ref to Customers.id
    private int doctorId; // ref to Employees.id (role='doctor')
    private String appointmentType; // "visit", "test", "surgery"
    private String notes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // "scheduled", "confirmed", "checked_in", etc.
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Legacy field for backward compatibility
    private String patientName;
    private String date;

    public Appointment() {
        this.status = "scheduled";
    }

    public Appointment(int id, int customerId, int doctorId, String appointmentType,
                       String notes, LocalDateTime startTime, LocalDateTime endTime,
                       String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.doctorId = doctorId;
        this.appointmentType = appointmentType;
        this.notes = notes;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status != null ? status : "scheduled";
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

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    // Legacy fields for backward compatibility
    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
