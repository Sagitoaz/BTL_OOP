package org.example.oop.Model.Medical;

import java.time.LocalDate;
import java.util.Objects;

public class MedicalHistory {
    public enum Status{
        ACTIVE, RESOLVED, ONGOING, RECURRENT, UNKNOWN
    }
    private int id;
    private int patientRecordId;
    private String  condition;
    private LocalDate date;
    private String notes;
    private Status status;
    public MedicalHistory(int id, int patientRecordId, String condition, LocalDate date, String notes, Status status) {
        if(id <= 0){

            throw new IllegalArgumentException("id must > 0");
        }
        this.id = id;
        this.patientRecordId = patientRecordId;
        this.condition = condition;
        this.date = date;
        this.notes = notes;
        this.status = status;

    }
    public int getId() {
        return id;
    }
    public int getPatientRecordId() {
        return patientRecordId;
    }
    public String getCondition() {
        return condition;
    }
    public LocalDate getDate() {
        return date;
    }
    public String getNotes() {
        return notes;
    }
    public Status getStatus() {
        return status;
    }
    //Data file id|patientId|condition|date|notes|status
    public String toDataString(){
        String dateString = "";
        if(date != null){
            dateString = date.toString();
        }
        return String.join("|", String.valueOf(id), String.valueOf(patientRecordId), condition == null ? "":condition, dateString,notes == null ?"":notes ,status.name());

    }
    public static MedicalHistory fromDataString(String data){
        String[] fields = data.split("\\|", -1);
        int id = Integer.parseInt(fields[0]);
        int patientRecordId = Integer.parseInt(fields[1]);
        String condition = (fields[2].equals("null") || fields[2].isBlank()) ? null : fields[2];
        LocalDate date = (fields[3].equals("null")|| fields[3].isBlank()) ? null : LocalDate.parse(fields[3]);
        String notes = (fields[4].equals("null") || fields[4].isBlank()) ? null : fields[4];
        Status status = (fields[5].equals("null") || fields[5].isBlank()) ? Status.UNKNOWN :Status.valueOf(fields[5].toUpperCase());
        return new MedicalHistory(id, patientRecordId, condition, date, notes, status);

    }
    @Override
    public String toString(){
        return "MedicalHistory{ id:" +id + ", patientRecordId: " + patientRecordId + ", condition: " + condition +"}";
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return  id == ((MedicalHistory) o).id;
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
