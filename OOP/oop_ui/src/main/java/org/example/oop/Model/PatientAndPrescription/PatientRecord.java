package org.example.oop.Model.PatientAndPrescription;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class PatientRecord {
    public enum Gender{
        NAM, NỮ, KHÁC;
    }
    private int id;
    private String namePatient;
    private int patientId;
    private LocalDate dob;
    private Gender gender;
    private String address;
    private String phoneNumber;
    private String email;
    private String notes;
    private List<SpectaclePrescription> prescriptionList;
    private List<MedicalHistory> medicalHistoryList;
    public PatientRecord(int id, String namePatient,int patientId, LocalDate dob, Gender gender, String address, String phoneNumber, String email, String allergies) {
        if(id <= 0){
            throw new IllegalArgumentException("id must be > 0");
        }
        this.id = id;
        this.namePatient = namePatient;
        this.patientId = patientId;
        this.dob = dob;
        this.gender = gender;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.prescriptionList = new ArrayList<>();
        this.medicalHistoryList = new ArrayList<>();
        this.notes = allergies;
    }
    public int getId(){
        return id;
    }
    public String getNamePatient(){
        return namePatient;
    }
    public LocalDate getDob(){
        return dob;
    }
    public int getAge(){
        if(dob == null){
            return 0;
        }
        return Period.between(dob, LocalDate.now()).getYears();
    }
    public Gender getGender(){
        return gender;
    }
    public String getAddress(){
        return address;
    }
    public String getPhoneNumber(){

        return phoneNumber;
    }
    public String getEmail(){
        return email;
    }
    public String getNotes(){
        return notes;
    }
    public void addPrescription(SpectaclePrescription prescription){
        if(prescription == null){
            return;
        }
        prescriptionList.add(prescription);
    }
    public List<SpectaclePrescription> getPrescriptionList(){

        return Collections.unmodifiableList(prescriptionList);
    }
    public void addMedicalHistory(MedicalHistory medicalHistory){
        if(medicalHistory == null){
            return;
        }
        medicalHistoryList.add(medicalHistory);
    }
    public List<MedicalHistory> getMedicalHistoryList(){
        return Collections.unmodifiableList(medicalHistoryList);
    }
    // Dung de xu li khi in ra an toan
    public String toSafeString(String s){
        if(s == null){
            return "";
        }
        return s;
    }

    // Data id|name|patientId|dob|gender|address|phone|email|note
    public String toDataString(){
        String dobString = (this.dob == null ) ? "": this.dob.toString();
        return String.join("|", String.valueOf(this.id),  toSafeString(this.namePatient), String.valueOf(patientId), dobString, gender.name(), toSafeString(this.address), toSafeString(this.phoneNumber), toSafeString(this.email));
    }
    // Ham so sanh
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        return this.id == ((PatientRecord)o).id;
    }
    // Doc Data tu file
    // Data id|name|patientId|dob|gender|address|phone|email|note
    public static PatientRecord fromDataString(String line){
        String[] fields = line.split("\\|", -1);
        int id = Integer.parseInt(fields[0]);
        String namePatient = (fields[1].equalsIgnoreCase("null") || fields[1].isBlank())? null : fields[1];
        int patientId = Integer.parseInt(fields[2]);
        LocalDate dob = null;
        try{
            dob = (fields[2].equalsIgnoreCase("null") || fields[2].isBlank()) ? null : LocalDate.parse(fields[2]);
        }
        catch(Exception e){
        }
        Gender gender = (fields[3].equalsIgnoreCase("null") || fields[3].isBlank()) ? Gender.KHÁC : Gender.valueOf(fields[3].toUpperCase());
        String address = (fields[4].equalsIgnoreCase("null") || fields[4].isBlank()) ? null : fields[4];
        String phoneNumber = (fields[5].equalsIgnoreCase("null") || fields[5].isBlank()) ? null : fields[5];
        String email = (fields[6].equalsIgnoreCase("null") || fields[6].isBlank()) ? null : fields[6];
        String note = (fields[7].equalsIgnoreCase("null") || fields[7].isBlank()) ? null : fields[7];

        return new PatientRecord(id, namePatient,patientId, dob, gender, address, phoneNumber, email, note);
    }
    // Ham In ra de debug
    @Override
    public String toString() {
        return id + "." + namePatient;
    }
    @Override
    public int hashCode(){
        return Objects.hash(id);
    }




}

