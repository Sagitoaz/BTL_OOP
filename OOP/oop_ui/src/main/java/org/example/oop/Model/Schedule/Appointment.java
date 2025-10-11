package org.example.oop.Model.Schedule;

import java.time.LocalDateTime;

public class Appointment {
    int id;
    int customerId;
    int doctorId;
    String notes;
    AppointmentType appointmentType;
    AppointmentStatus appointmentStatus;
    LocalDateTime startTime;
    LocalDateTime endTime;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public Appointment(int id, int customerId, int doctorId,
                       String notes,
                       AppointmentType appointmentType,
                       AppointmentStatus appointmentStatus,
                       LocalDateTime startTime,
                       LocalDateTime endTime,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.doctorId = doctorId;
        this.notes = notes;
        this.appointmentType = appointmentType;
        this.appointmentStatus = appointmentStatus;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public static Appointment fromDataString(String line) {
        String[] parts = line.split("\\|");
        int id = Integer.parseInt(parts[0]);
        int customerId = Integer.parseInt(parts[1]);
        int doctorId = Integer.parseInt(parts[2]);
        String notes = parts[3];
        AppointmentType type = AppointmentType.valueOf(parts[4]);
        AppointmentStatus status = AppointmentStatus.valueOf(parts[5]);
        LocalDateTime startTime = LocalDateTime.parse(parts[6]);
        LocalDateTime endTime = LocalDateTime.parse(parts[7]);
        LocalDateTime createdAt = LocalDateTime.parse(parts[8]);
        LocalDateTime updatedAt = LocalDateTime.parse(parts[9]);
        return new Appointment(id, customerId, doctorId, notes, type, status, startTime, endTime, createdAt, updatedAt);
    }

    public void validate() {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start/End time cannot be null");
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        if (doctorId <= 0 || customerId <= 0) {
            throw new IllegalArgumentException("Invalid doctor or customer ID");
        }
    }

    public String toDataString() {
        return id + "|" + customerId + "|" + doctorId + "|" +
                startTime + "|" + endTime + "|" + appointmentStatus.getCode() + "|" +
                appointmentType.getCode() + "|" + notes + "|" +
                createdAt + "|" + updatedAt;
    }

    public int getId() {
        return id;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public AppointmentStatus getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}