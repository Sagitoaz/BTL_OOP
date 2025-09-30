package org.miniboot.app.domain.models;

public class Appointment {
    private int id;
    private int doctorId;
    private String patientName;
    private String startTime;  // tạm String cho đơn giản
    private String date;       // YYYY-MM-DD
    private String status = "scheduled"; // mặc định

    public Appointment(int id, int doctorId, String patientName, String startTime, String date) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.startTime = startTime;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
