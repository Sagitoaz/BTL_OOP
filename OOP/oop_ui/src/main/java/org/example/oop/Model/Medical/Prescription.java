package org.example.oop.Model.Medical;

import java.time.LocalDate;
import java.util.Objects;

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
        if(id <= 0  || patientRecordId <= 0  || doctorId <= 0) {
            throw new IllegalArgumentException("id must be > 0");
        }
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


    public static Prescription fromDataString(String line){
        String[] fields = line.split("\\|", -1);
        int id = Integer.parseInt(fields[0]);
        int recordId = Integer.parseInt(fields[1]);
        PrescriptionType prescriptionType = PrescriptionType.valueOf(fields[2]);
        String details = (fields[3].equals("null") || fields[3].isBlank()) ? null : fields[3];
        String note = (fields[4].equals("null") || fields[4].isBlank()) ? null : fields[4];
        int doctorId = Integer.parseInt(fields[5]);
        LocalDate signedDate = LocalDate.parse(fields[6]);
        return new Prescription(id, recordId, prescriptionType, details, note, doctorId, signedDate);
    }

    @Override
    public String toString() {
        return "Prescription{id=" + id +
                ", recordId=" + patientRecordId +
                ", type=" + prescriptionType.name() +
                ", doctorId=" + doctorId + '\'' +
                ", signedDate=" + signedDate +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return this.id == ((Prescription) o).id;
    }
    @Override
    public int hashCode(){
        return Objects.hash(id);
    }

}
