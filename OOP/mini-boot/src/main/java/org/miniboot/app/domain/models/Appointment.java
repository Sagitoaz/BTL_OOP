package org.miniboot.app.domain.models;

public class Appointment {
    private int id;
    private int doctorId;
    private String patientName;
    private String startTime;  // tạm String cho đơn giản
    private String date;       // YYYY-MM-DD
    private String status = "scheduled"; // mặc định

    //constructor mặc định
    public Appointment() {
    }

    //constructor tiện lợi
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

    public void setId(int id) {
        this.id = id;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
