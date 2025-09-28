package org.miniboot.app.domain.models;

public class Appointment {
    private int id;
    private String doctorId;
    private String patientName;
    private String startTime;
    private String status;
    public Appointment(int id, String doctorId, String patientName, String startTime, String status) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.startTime = startTime;
        this.status = status;
    }

}
