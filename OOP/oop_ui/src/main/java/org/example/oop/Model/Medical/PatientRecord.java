package org.example.oop.Model.Medical;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class PatientRecord {
    public enum Gender{
        MALE, FEMALE, OTHER;
    }
    private int id;
    private String namePatient;
    private LocalDate dob;
    private Gender gender;
    private String address;
    private String phoneNumber;
    private String email;
    private List<Prescription> prescriptionList;
    private List<Examination> examinationList;
    private List<MedicalHistory> medicalHistoryList;
    public PatientRecord(int id, String namePatient, LocalDate dob, Gender gender, String address, String phoneNumber, String email) {
        if(id <= 0){
            throw new IllegalArgumentException("id must be > 0");
        }
        this.id = id;
        this.namePatient = namePatient;
        this.dob = dob;
        this.gender = gender;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.prescriptionList = new ArrayList<>();
        this.examinationList = new ArrayList<>();
        this.medicalHistoryList = new ArrayList<>();
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
    public void addPrescription(Prescription prescription){
        if(prescription == null){
            return;
        }
        prescriptionList.add(prescription);
    }
    public List<Prescription> getPrescriptionList(){

        return Collections.unmodifiableList(prescriptionList);
    }
    public void addExamination(Examination examination){
        if(examination == null){
            return;
        }
        examinationList.add(examination);
    }
    public List<Examination> getExaminationList(){
        return Collections.unmodifiableList(examinationList);
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

    // Data id|name|dob|gender|address|phone|email
    public String toDataString(){
        String dobString = (this.dob == null ) ? "": this.dob.toString();
        return String.join("|", String.valueOf(this.id),  toSafeString(this.namePatient), dobString, gender.name(), toSafeString(this.address), toSafeString(this.phoneNumber), toSafeString(this.email));
    }
    // Ham so sanh
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        return this.id == ((PatientRecord)o).id;
    }
    // Doc Data tu file
    // Data id|name|dob|gender|address|phone|email
    public static PatientRecord fromDataString(String line){
        String[] fields = line.split("\\|", -1);
        int id = Integer.parseInt(fields[0]);
        String namePatient = (fields[1].equals("null") || fields[1].isBlank())? null : fields[1];
        LocalDate dob = (fields[2].equals("null") || fields[2].isBlank()) ? null : LocalDate.parse(fields[2]);
        Gender gender = (fields[3].equals("null") || fields[3].isBlank()) ? null : Gender.valueOf(fields[3].toUpperCase());
        String address = (fields[4].equals("null") || fields[4].isBlank()) ? null : fields[4];
        String phoneNumber = (fields[5].equals("null") || fields[5].isBlank()) ? null : fields[5];
        String email = (fields[6].equals("null") || fields[6].isBlank()) ? null : fields[6];
        return new PatientRecord(id, namePatient, dob, gender, address, phoneNumber, email);
    }
    // Ham In ra de debug
    @Override
    public String toString() {
        return "PatientRecord{id=" + id + ", name='" + namePatient + "', gender=" + gender.name() + "}";
    }
    @Override
    public int hashCode(){
        return Objects.hash(id);
    }




}

