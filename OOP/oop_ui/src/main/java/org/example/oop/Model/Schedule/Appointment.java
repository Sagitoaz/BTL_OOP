package org.example.oop.Model.Schedule;

import java.time.LocalDateTime;

public class Appointment {
    int id;
    int customerId;
    int doctorID;
    String notes;
    AppointmentType appointmentType;
    AppointmentStatus appointmentStatus;
    LocalDateTime startTime;
    LocalDateTime endTime;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}