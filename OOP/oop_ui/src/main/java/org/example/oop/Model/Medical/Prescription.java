package org.example.oop.Model.Medical;

import java.time.LocalDate;

public class Prescription {
    public enum PrescriptionType {
        EYE_DROPS,
        OINTMENT,
        ORAL_MEDICATION,
        ANTIBIOTIC,
        ANTI_ALLERGY,
        STEROID,
        LUBRICANT,
        GLASSES,
        CONTACT_LENS,
        PROTECTIVE_LENS,
        READING_LENS,
        EYE_WASH,
        COMPRESS,
        SUPPLEMENT,
        POST_SURGERY_CARE,
        DIAGNOSTIC_KIT,
        OTHER
    }
    private int id;
    private int patientRecordId;
    private int doctorId;
    private PrescriptionType prescriptionType;
    private String details;
    private String note;
    private LocalDate signedDate;

    public Prescription(int id,int patientRecordId, PrescriptionType prescriptionType, String details, String note,int doctorId, LocalDate signedDate) {
        this.id = id;
        this.patientRecordId = patientRecordId;
        this.prescriptionType = prescriptionType;
        this.details = details;
        this.note = note;
        this.doctorId = doctorId;
        this.signedDate = signedDate;
    }

    public int getId() {
        return id;
    }
    public int getPatientRecordId() {
        return patientRecordId;
    }
    public int getDoctorId() {
        return doctorId;
    }
    public PrescriptionType getPrescriptionType() {
        return prescriptionType;
    }
    public String getDetails() {
        return details;
    }
    public String getNote() {
        return note;
    }
    public LocalDate getSignedDate() {
        return signedDate;
    }
    public String toSafeString(String s){
        if(s == null){
            return "";
        }
        return s;
    }

    //Data id|recordId|type|details|notes|doctorId|signedDate
    public String toDataString(){
        String signedDateString = "";
        if(signedDate != null){
            signedDateString = signedDate.toString();
        }
        return String.join("|", String.valueOf(id), String.valueOf(patientRecordId), prescriptionType.name(), toSafeString(details),
                toSafeString(note), String.valueOf(doctorId), String.valueOf(signedDateString));
    }

}
